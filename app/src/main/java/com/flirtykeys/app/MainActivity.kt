package com.flirtykeys.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("keyboard_theme", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 30, 28, 24)
            setBackgroundColor(Color.rgb(25, 20, 32))
        }
        root.addView(TextView(this).apply {
            text = "💘 FlirtyKeys Keyboard"
            textSize = 25f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "A customizable keyboard with pickup lines, Thanglish and themes."
            textSize = 15f; setTextColor(Color.LTGRAY); gravity = Gravity.CENTER
            setPadding(0, 12, 0, 24)
        })
        root.addView(action("1. Enable FlirtyKeys keyboard") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        })
        root.addView(action("2. Choose FlirtyKeys keyboard") {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showInputMethodPicker()
        })
        root.addView(action("Choose background image") {
            val pick = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE); type = "image/*"
            }
            startActivityForResult(pick, 42)
        })
        root.addView(action("Choose keyboard colour") {
            val names = arrayOf("Midnight purple", "Ocean blue", "Rose pink", "Dark")
            val values = intArrayOf(0xFF302039.toInt(), 0xFF123B55.toInt(), 0xFF592D45.toInt(), 0xFF171717.toInt())
            AlertDialog.Builder(this).setTitle("Keyboard theme")
                .setItems(names) { _, which -> prefs.edit().putInt("bg_color", values[which]).apply() }.show()
        })
        root.addView(TextView(this).apply {
            text = "Background brightness"
            textSize = 16f; setTextColor(Color.WHITE); setPadding(4, 18, 4, 4)
        })
        val brightness = SeekBar(this).apply {
            max = 100; progress = prefs.getInt("brightness", 65)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                    if (fromUser) prefs.edit().putInt("brightness", value).apply()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        root.addView(brightness)
        root.addView(action("Remove background image") { prefs.edit().remove("background_uri").apply() })
        root.addView(TextView(this).apply {
            text = "Privacy: FlirtyKeys does not collect or send what you type. Text is inserted only after you tap a key or a line."
            textSize = 13f; setTextColor(Color.LTGRAY); setPadding(4, 24, 4, 8)
        })
        setContentView(root)
    }

    private fun action(title: String, click: () -> Unit) = Button(this).apply {
        text = title; isAllCaps = false; setOnClickListener { click() }
    }

    @Deprecated("Deprecated by Android")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 42 && resultCode == RESULT_OK) {
            data?.data?.let { uri: Uri ->
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    prefs.edit().putString("background_uri", uri.toString()).apply()
                } catch (_: Exception) { }
            }
        }
    }
}
