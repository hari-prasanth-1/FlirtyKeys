package com.flirtykeys.app

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class FloatingService : Service() {
    private lateinit var wm: WindowManager
    private var bubble: TextView? = null
    private var panel: View? = null
    private val selected = linkedSetOf<String>()
    private var activeCategory = "Flirty"
    private val categories = listOf("Cute", "Flirty", "Romantic", "Funny", "Smooth", "Bold", "Seductive", "Teasing", "Starters", "Good morning", "Good night", "Compliments")
    private val lines = mapOf(
        "Cute" to listOf("You just made my day a little brighter.", "Is it your smile, or did the room just get warmer?", "I think my favourite notification would be from you.", "Talking to you is becoming my favourite habit."),
        "Flirty" to listOf("Are you always this charming, or is it just with me?", "I was having a normal day until you crossed my mind.", "Should I keep flirting, or are you already smiling?", "You make it very hard to play it cool."),
        "Romantic" to listOf("I like how easily you make me smile.", "Some conversations make the whole day better. This is one.", "I'd like to know the little things that make you happy.", "You have a way of staying on my mind."),
        "Funny" to listOf("Are you a magician? Because my attention disappeared.", "I had a clever opener, but then you distracted me.", "Do you come with a user manual? You're quite interesting.", "I blame you for this ridiculous smile."),
        "Smooth" to listOf("I was hoping we'd get a chance to talk.", "You seem interesting. Mind if I find out why?", "I like your vibe. What's something you're passionate about?", "This conversation has my full attention."),
        "Bold" to listOf("I think we should turn this chat into a coffee date.", "I'll be honest: I'd really like to get to know you.", "You caught my attention. Can I keep it for a while?", "No cheesy line today. Just wanted to say you're lovely."),
        "Seductive" to listOf("That smile of yours is a little distracting.", "There's something about your energy I find irresistible.", "I like the tension when we're teasing each other.", "If you're comfortable, I'd love to get a little closer."),
        "Teasing" to listOf("You always this hard to impress?", "I have a feeling you're trouble—in the fun way.", "Was that your attempt to make me smile? It worked.", "Careful, I might get used to your attention."),
        "Starters" to listOf("What's one thing that instantly makes you happy?", "What's your ideal way to spend a free evening?", "What's something you've always wanted to try?", "What's the best part of your week so far?"),
        "Good morning" to listOf("Good morning! Hope today gives you plenty of reasons to smile.", "Morning! Just sending a little positive energy your way.", "Hope your coffee is good and your day is even better."),
        "Good night" to listOf("Sleep well. Hope tomorrow treats you kindly.", "Good night! Save a little smile for me tomorrow.", "Wishing you peaceful dreams and a lovely morning."),
        "Compliments" to listOf("You have a really lovely way of expressing yourself.", "Your smile has a way of making people feel welcome.", "I genuinely enjoy your sense of humour.", "You have a great energy about you.")
    )

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        showBubble()
    }

    private fun params(width: Int, height: Int, gravity: Int = Gravity.TOP or Gravity.START): WindowManager.LayoutParams =
        WindowManager.LayoutParams(width, height, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply { this.gravity = gravity; x = 24; y = 280 }

    private fun showBubble() {
        if (bubble != null) return
        val b = TextView(this).apply {
            text = "💘"; textSize = 25f; gravity = Gravity.CENTER
            setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(160, 65, 190)); setPadding(18, 12, 18, 12)
            setOnClickListener { if (panel == null) showPanel() else closePanel() }
            setOnTouchListener(object : View.OnTouchListener {
                var sx = 0f; var sy = 0f; var ox = 0; var oy = 0; var moved = false
                override fun onTouch(v: View, e: MotionEvent): Boolean {
                    val lp = v.layoutParams as WindowManager.LayoutParams
                    when (e.action) {
                        MotionEvent.ACTION_DOWN -> { sx=e.rawX; sy=e.rawY; ox=lp.x; oy=lp.y; moved=false; return true }
                        MotionEvent.ACTION_MOVE -> { val dx=(e.rawX-sx).toInt(); val dy=(e.rawY-sy).toInt(); if (kotlin.math.abs(dx)+kotlin.math.abs(dy)>8) moved=true; lp.x=ox+dx; lp.y=oy+dy; wm.updateViewLayout(v,lp); return true }
                        MotionEvent.ACTION_UP -> { if (!moved) v.performClick(); return true }
                    }; return false
                }
            })
        }
        bubble = b
        wm.addView(b, params(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT))
    }

    private fun showPanel() {
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(18,16,18,14); setBackgroundColor(Color.rgb(30,25,38)) }
        val header = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        header.addView(label("💘 FlirtyKeys", 20f, Color.WHITE, true), LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(Button(this).apply { text="×"; setOnClickListener { closePanel() } })
        root.addView(header)
        val cats = HorizontalScrollView(this)
        val catRow = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        categories.forEach { cat -> catRow.addView(Button(this).apply { text=cat; textSize=11f; setOnClickListener { activeCategory=cat; selected.clear(); closePanel(); showPanel() } }) }
        cats.addView(catRow); root.addView(cats)
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        (lines[activeCategory] ?: emptyList()).forEach { line ->
            val row = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(4,8,4,8) }
            val check = CheckBox(this).apply { text=line; textSize=13f; setTextColor(Color.WHITE); isChecked=selected.contains(line); setOnCheckedChangeListener { _, checked -> if (checked) selected.add(line) else selected.remove(line) } }
            row.addView(check, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(Button(this).apply { text="Copy"; setOnClickListener { copy(listOf(line)) } })
            list.addView(row)
        }
        scroll.addView(list); root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val actions = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        actions.addView(Button(this).apply { text="Select all"; setOnClickListener { selected.addAll(lines[activeCategory] ?: emptyList()); closePanel(); showPanel() } }, LinearLayout.LayoutParams(0,-2,1f))
        actions.addView(Button(this).apply { text="Copy selected"; setOnClickListener { if (selected.isEmpty()) Toast.makeText(this@FloatingService,"Select at least one line",Toast.LENGTH_SHORT).show() else copy(selected.toList()) } }, LinearLayout.LayoutParams(0,-2,1f))
        root.addView(actions)
        root.addView(label("${selected.size} selected · Copy, then paste into your chat", 11f, Color.LTGRAY, false))
        val lp = params((resources.displayMetrics.widthPixels * 0.88f).toInt(), (resources.displayMetrics.heightPixels * 0.68f).toInt()).apply { x=20; y=100 }
        panel = root
        wm.addView(root, lp)
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply { this.text=text; textSize=size; setTextColor(color); if (bold) setTypeface(null, Typeface.BOLD); setPadding(8,10,8,10) }
    private fun copy(items: List<String>) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("FlirtyKeys messages", items.joinToString("\n")))
        Toast.makeText(this, "${items.size} message(s) copied. Paste into your chat.", Toast.LENGTH_LONG).show()
    }
    private fun closePanel() { panel?.let { try { wm.removeView(it) } catch (_: Exception) {} }; panel=null }
    override fun onDestroy() { closePanel(); bubble?.let { try { wm.removeView(it) } catch (_: Exception) {} }; bubble=null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
