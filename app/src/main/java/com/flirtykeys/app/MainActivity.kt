package com.flirtykeys.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.view.Gravity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(25, 20, 32))
        }
        root.addView(TextView(this).apply {
            text = "💘 FlirtyKeys\n\nYour pickup-line companion for Gboard"
            textSize = 24f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "Allow display over other apps, then start the floating button. Your existing keyboard stays unchanged."
            textSize = 15f; setTextColor(Color.LTGRAY); gravity = Gravity.CENTER; setPadding(0, 24, 0, 24)
        })
        root.addView(Button(this).apply {
            text = "1. Grant overlay permission"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
        })
        root.addView(Button(this).apply {
            text = "2. Start FlirtyKeys floating button"
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    startService(Intent(this@MainActivity, FloatingService::class.java))
                } else {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }
            }
        })
        root.addView(Button(this).apply {
            text = "Stop floating button"
            setOnClickListener { stopService(Intent(this@MainActivity, FloatingService::class.java)) }
        })
        setContentView(root)
    }
}
