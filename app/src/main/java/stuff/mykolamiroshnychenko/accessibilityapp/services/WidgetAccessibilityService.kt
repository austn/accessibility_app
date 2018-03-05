package stuff.mykolamiroshnychenko.accessibilityapp.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageView
import android.os.Bundle
import java.nio.file.Path

import java.util.ArrayDeque
import android.accessibilityservice.GestureDescription

import kotlin.mykolamiroshnychenko.accessibilityapp.R
import timber.log.Timber

class WidgetAccessibilityService : AccessibilityService() {

    private var mWindowManager: WindowManager? = null
    private var widgetView: View? = null
    internal var params: WindowManager.LayoutParams? = null
    private var i = 0

    private val listener = object : View.OnTouchListener {
        private var lastAction: Int = 0
        private var initialX: Int = 0
        private var initialY: Int = 0
        private var initialTouchX: Float = 0.toFloat()
        private var initialTouchY: Float = 0.toFloat()


        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                    //remember the initial position.
                    initialX = params!!.x
                    initialY = params!!.y

                    //get the touch location
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY

                    lastAction = event.action
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    performScroll()
                    lastAction = event.action
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    //Calculate the X and Y coordinates of the view.
                    var x = initialX + (event.rawX - initialTouchX).toInt()
                    var y = initialY + (event.rawY - initialTouchY).toInt()
                    params!!.x = x
                    //params!!.y = y

                    android.util.Log.i("x move",""+x.toString());
                    android.util.Log.i("y move",""+y.toString());

                    //Update the layout with new X & Y coordinate
                    mWindowManager!!.updateViewLayout(widgetView, params)
                    lastAction = event.action
                    return true
                }
            }
            return false
        }

    }


    override fun onAccessibilityEvent(event: AccessibilityEvent) {

    }

    override fun onInterrupt() {

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Timber.d(" WidgetAccessibilityService onServiceConnected")
        configureTheUI()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun configureTheUI() {
        setUpWindowParams()
        setUpWidgetInWindow()
        setUpInteractionListeners()
    }


    private fun setUpWidgetInWindow() {
        widgetView = LayoutInflater.from(this).inflate(R.layout.bubble_layout, null)
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(widgetView, params)
    }


    private fun setUpInteractionListeners() {
        val chatHeadImage = widgetView!!.findViewById<View>(R.id.chat_head_profile_iv) as ImageView
        chatHeadImage.setOnTouchListener(listener)
    }

    private fun setUpWindowParams() {
        var flags = WindowManager.LayoutParams.TYPE_PHONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
        //Add the view to the window.
        params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                flags,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)

        params!!.gravity = Gravity.BOTTOM or Gravity.LEFT        //Initially view will be added to top-left corner
        params!!.x = 0
        params!!.y = 0
     }
     private fun performScroll(){
       i = 0;
       scroll();
     }
     private fun scroll() {
        //val scrollable = findScrollableNode(rootInActiveWindow);
        /*
        I thought this would work it doesn't.  Not sure why. From here https://stackoverflow.com/q/15557902
        val a = Bundle();
        a.putInt("ACTION_ARGUMENT_MOVE_WINDOW_X",0); https://github.com/aosp-mirror/platform_frameworks_base/blob/master/packages/SystemUI/src/com/android/systemui/pip/phone/PipAccessibilityInteractionConnection.java#L94
        a.putInt("ACTION_ARGUMENT_COLUMN_INT",0);  // ACTION_SCROLL_UP doesn't work either
        scrollable?.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_TO_POSITION.id,a);
        Scrollable elements funs allow you to call toScroll but I don't believe this are available through Accessibility APIs.
        https://developer.android.com/reference/android/widget/ScrollView.html#computeScroll()
        Because the above didn't work I did plan B and trigured an actual gesture that repeats based on the center of screen.
        This is the same way "Automatic Scroll" works. */
        val d : android.util.DisplayMetrics = getResources().getDisplayMetrics();
        android.util.Log.i("x width",""+d.widthPixels.toString());  // 1440 pixel
        android.util.Log.i("y height",""+d.heightPixels.toString());// 2392 pixel
        val x = d.widthPixels / 2;
        val y = d.heightPixels /2;
        val swipePath = android.graphics.Path(); // from here https://stackoverflow.com/q/44420320
        swipePath.moveTo(x.toFloat(), (y-y/2).toFloat());
        swipePath.lineTo(x.toFloat(), (y+y/2).toFloat());
        val gestureBuilder = GestureDescription.Builder();
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, 0.0.toLong(), 20.0.toLong(),false));
        //gestureBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, 0.0.toLong(), 20.0.toLong(),false));
        dispatchGesture(gestureBuilder.build(), object:android.accessibilityservice.AccessibilityService.GestureResultCallback() {
                                                  override fun onCompleted(gestureDescription:android.accessibilityservice.GestureDescription) {
                                                     android.util.Log.i("onCompleted",""+i.toString());
                                                     if(i++<50)scroll();
                                                  }
                                                  override fun onCancelled(gestureDescription:android.accessibilityservice.GestureDescription) {
                                                     //if(i++<3)performScroll();
                                                     android.util.Log.i("onCancelled","onCancelled");
                                                  }

                                                }, null);
    }

    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val deque = ArrayDeque<AccessibilityNodeInfo>()
        deque.add(root)
        while (!deque.isEmpty()) {
            val node = deque.removeFirst()
            if (node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD)) {
                return node
            }
            for (i in 0 until node.childCount) {
                deque.addLast(node.getChild(i))
            }
        }
        return null
    }
}