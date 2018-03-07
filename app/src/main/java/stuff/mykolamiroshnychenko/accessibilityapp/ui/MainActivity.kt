package stuff.mykolamiroshnychenko.accessibilityapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlin.mykolamiroshnychenko.accessibilityapp.R
import android.view.View
import android.widget.ImageView
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val accessibility = findViewById(R.id.accessibility) as Button
        val m: MainActivity = this@MainActivity
        android.util.Log.i("class","outside");
        accessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
        var displayover = findViewById(R.id.displayover) as Button
        displayover.setOnClickListener {
            if(!android.provider.Settings.canDrawOverlays(m)){
              intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:" + getPackageName()));
              startActivityForResult(intent, 0);
            }
        }
    }
}
