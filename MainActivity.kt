package com.example.dotstatus

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(48, 48, 48, 48)
        }
        root.addView(TextView(this).apply { text = "Dot Status"; textSize = 26f; gravity = Gravity.CENTER })
        root.addView(Button(this).apply {
            text = "Start"
            setOnClickListener {
                requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.POST_NOTIFICATIONS), 1)
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                } else startForegroundService(Intent(this@MainActivity, OverlayService::class.java))
            }
        })
        root.addView(Button(this).apply {
            text = "Stop"
            setOnClickListener { stopService(Intent(this@MainActivity, OverlayService::class.java)) }
        })
        setContentView(root)
    }
}
