package com.flirtykeys.app

import android.inputmethodservice.InputMethodService
import android.graphics.Color
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class FlirtyKeyboardService : InputMethodService() {
    private val prefs by lazy { getSharedPreferences("keyboard_theme", Context.MODE_PRIVATE) }
    private var shifted = false
    private var symbols = false
    private var lineArea: LinearLayout? = null
    private var root: LinearLayout? = null
    private var activeCategory = "Flirty"

    private val categories = listOf("Flirty", "Cute", "Romantic", "Funny", "Smooth", "Bold", "Teasing", "Starters", "Compliments", "Good morning", "Good night", "Thanglish")
    private val lines = mapOf(
        "Flirty" to listOf("Are you always this charming, or is it just with me?", "I was having a normal day until you crossed my mind.", "Should I keep flirting, or are you already smiling?", "You make it hard to play it cool.", "Can I be the reason you smile today?", "I think we have unfinished conversations to start.", "Your messages are becoming my favourite notification.", "Is it too soon to say I like your vibe?", "I was going to play it cool, but then you texted.", "One coffee with you sounds like a very good plan."),
        "Cute" to listOf("You just made my day brighter.", "Your smile should come with a warning: highly distracting.", "Talking to you is my favourite little habit.", "If happiness had a notification, it would be your name.", "You make ordinary moments feel special.", "Can I save a little space in your day?", "Your laugh is stuck in my head—in a good way.", "You are my favourite plot twist.", "I hope your day is as lovely as your smile.", "A tiny hello from me, just to make you smile."),
        "Romantic" to listOf("I like how easily you make me smile.", "Some conversations make the whole day better. This is one.", "I'd love to learn all the little things that make you happy.", "You have a way of staying on my mind.", "If I could choose a moment to repeat, it would be this conversation.", "Your presence makes even silence comfortable.", "I don't need a perfect day, just a little time with you.", "Every message from you feels like a warm pause.", "I hope we make more memories together.", "You make my world feel a little softer."),
        "Funny" to listOf("Are you Wi-Fi? Because I'm feeling a connection.", "I had a clever opener, but you distracted me.", "Do you come with a user manual? You're interesting.", "I blame you for this ridiculous smile.", "Are you a keyboard? Because you're just my type.", "I was productive until your notification arrived.", "My phone says low battery, but your chat keeps me charged.", "Are we a software update? Because things just got better.", "I need a map; I got lost in this conversation.", "Is your name Google? You have everything I'm searching for."),
        "Smooth" to listOf("I was hoping we'd get a chance to talk.", "You seem interesting. Mind if I find out why?", "I like your vibe. What's something you're passionate about?", "This conversation has my full attention.", "What's one thing I should know about you?", "I would rather hear your story than use a cheesy line.", "You make getting to know someone feel easy.", "Tell me something that always makes you smile.", "I like where this conversation is going.", "Can we continue this over coffee sometime?"),
        "Bold" to listOf("Let's turn this chat into a coffee date.", "I'll be honest: I'd really like to know you.", "You caught my attention. Can I keep it for a while?", "No cheesy line today. I just think you're lovely.", "I'd like to take you out, if you're interested.", "I think we should give this chemistry a chance.", "Can I steal an hour of your time this weekend?", "I like you. Simple, honest, and no guessing.", "Would you say yes to a spontaneous coffee?", "I want to know you beyond this chat."),
        "Teasing" to listOf("You always this hard to impress?", "I have a feeling you're trouble—in the fun way.", "Was that your attempt to make me smile? It worked.", "Careful, I might get used to your attention.", "Are you trying to distract me? Mission accomplished.", "You seem to enjoy keeping me curious.", "I was winning at being calm until you showed up.", "Should I pretend I didn't reread your message?", "You're a little too good at this flirting thing.", "I see that smile. Don't deny it."),
        "Starters" to listOf("What's one thing that instantly makes you happy?", "What's your ideal way to spend a free evening?", "What's something you've always wanted to try?", "What's the best part of your week so far?", "Tea or coffee—and why?", "What's a song you never skip?", "If we could travel anywhere tomorrow, where would you go?", "What's your most random hidden talent?", "What's your idea of a perfect weekend?", "What's one small thing that makes your day better?"),
        "Compliments" to listOf("You have a lovely way of expressing yourself.", "Your smile makes people feel welcome.", "I genuinely enjoy your sense of humour.", "You have such a great energy.", "Your confidence looks good on you.", "You make conversations feel effortless.", "You have a really thoughtful way of seeing things.", "Your style is completely your own.", "You have a laugh that's hard to forget.", "I like how genuinely you are yourself."),
        "Good morning" to listOf("Good morning! Hope today gives you reasons to smile.", "Morning! Sending a little positive energy your way.", "Hope your coffee is good and your day is even better.", "A new day, and you were my first happy thought.", "Rise and shine. Have a lovely day!"),
        "Good night" to listOf("Sleep well. Hope tomorrow treats you kindly.", "Good night! Save a little smile for me tomorrow.", "Wishing you peaceful dreams and a lovely morning.", "Before you sleep, remember someone is wishing you well.", "Sweet dreams. Talk to you tomorrow?"),
        "Thanglish" to listOf("Un smile paatha, en day automatic-ah nalla aagidum.", "Nee online varumbodhu dhaan en phone-ku meaning varudhu.", "Un kooda pesina time eppadi pogudhune theriyala.", "Coffee vida un kooda conversation dhaan more refreshing.", "Un voice kekka oru reason thedittu irukken.", "Un message vandha naan konjam extra happy aagiduven.", "First chat-la ye un mela interest vandhuduchu.", "Un kooda long drive pona, conversation super-ah irukkum.", "Unna impress panna try panren; aana nee already enna impress pannita.", "Un smile-ku copyright irukka? Daily paakanum pola irukku.", "Un kitta pesuradhu habit aagiduchu… complaint panna poriya?", "Namma rendu perum coffee-ku polaama?", "Nee sirikkumbodhu background music venum pola irukku.", "Un vibe romba nalla irukku; innum konjam therinjikalaama?", "Good night solla vandhen… aana innum pesa thonudhu.", "Un reply vandha udane face-la smile vandhudum.", "Nee pesina, boring day kooda super-ah maaridum.", "Namma chat continue pannalama? Innum konjam pesa aasai.", "Unna paatha odane words-e varala.", "Unakku theriyuma, nee en favourite notification." )
    )

    override fun onCreateInputView(): View {
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(4, 5, 4, 5) }
        root = container
        applyTheme(container)

        val toolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        toolbar.addView(tool("☰") { showCategoryPicker() })
        toolbar.addView(tool("😊") { commit("😊") })
        toolbar.addView(tool("📋") { Toast.makeText(this@FlirtyKeyboardService, "Clipboard history is not enabled yet. Use your phone clipboard.", Toast.LENGTH_SHORT).show() })
        toolbar.addView(tool("💘 Lines") { showLines() })
        toolbar.addView(tool("⚙") { requestSettings() })
        container.addView(toolbar)

        lineArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        container.addView(lineArea)
        addLineStrip()

        val keyRows = if (symbols) listOf(
            listOf("1","2","3","4","5","6","7","8","9","0"),
            listOf("@","#","$","%","&","-","+","(",")","/"),
            listOf("*","\"","' ",":",";","!","?"),
            listOf("ABC"," ,","😊","SPACE","⌫","↵")
        ) else listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l"),
            listOf("⇧","z","x","c","v","b","n","m","⌫"),
            listOf("?123",",","😊","SPACE",".","↵")
        )
        keyRows.forEachIndexed { index, row -> addKeyRow(container, row, index) }
        return container
    }

    private fun addKeyRow(parent: LinearLayout, keys: List<String>, rowIndex: Int) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        keys.forEach { key ->
            val weight = if (key in listOf("SPACE")) 4f else if (key in listOf("⇧","⌫","↵","?123","ABC")) 1.45f else 1f
            val label = when (key) { "SPACE" -> "English"; "↵" -> "↵"; else -> key }
            val b = Button(this).apply {
                text = if (key.length == 1 && key[0].isLetter()) (if (shifted) key.uppercase() else key) else label
                textSize = if (key == "SPACE") 11f else 16f
                isAllCaps = false
                setTextColor(Color.WHITE)
                background = keyBackground()
                setPadding(0, 0, 0, 0)
                setOnClickListener { handleKey(key) }
            }
            row.addView(b, LinearLayout.LayoutParams(0, dp(45), weight).apply { setMargins(2, 2, 2, 2) })
        }
        parent.addView(row)
    }

    private fun handleKey(key: String) {
        when (key) {
            "⇧" -> { shifted = !shifted; onCreateInputView().also { setInputView(it) } }
            "?123" -> { symbols = true; setInputView(onCreateInputView()) }
            "ABC" -> { symbols = false; setInputView(onCreateInputView()) }
            "⌫" -> currentInputConnection?.deleteSurroundingText(1, 0)
            "↵" -> currentInputConnection?.commitText("\n", 1)
            "SPACE" -> currentInputConnection?.commitText(" ", 1)
            "😊" -> currentInputConnection?.commitText("😊", 1)
            else -> currentInputConnection?.commitText(if (shifted) key.uppercase() else key, 1)
        }
        if (shifted && key.length == 1) shifted = false
    }

    private fun showCategoryPicker() {
        val area = lineArea ?: return
        area.removeAllViews()
        val scroller = HorizontalScrollView(this)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        categories.forEach { category -> row.addView(tool(category) { activeCategory = category; addLineStrip() }) }
        scroller.addView(row); area.addView(scroller)
    }

    private fun addLineStrip() {
        val area = lineArea ?: return
        area.removeAllViews()
        val scroll = HorizontalScrollView(this)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        (lines[activeCategory] ?: emptyList()).forEach { line ->
            row.addView(tool(line) { commit(line + " "); Toast.makeText(this@FlirtyKeyboardService, "Line inserted", Toast.LENGTH_SHORT).show() })
        }
        scroll.addView(row)
        area.addView(scroll)
    }

    private fun showLines() { showCategoryPicker() }
    private fun requestSettings() {
        Toast.makeText(this, "Open FlirtyKeys app to change themes and brightness.", Toast.LENGTH_LONG).show()
        try { val launch = packageManager.getLaunchIntentForPackage(packageName); if (launch != null) startActivity(launch.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) {}
    }
    private fun commit(text: String) { currentInputConnection?.commitText(text, 1) }

    private fun tool(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text; textSize = 12f; isAllCaps = false; setTextColor(Color.WHITE)
        background = keyBackground(); setPadding(dp(8), dp(2), dp(8), dp(2)); setOnClickListener { action() }
    }

    private fun keyBackground(): GradientDrawable = GradientDrawable().apply {
        val color = prefs.getInt("bg_color", 0xFF302039.toInt())
        setColor((color and 0x00FFFFFF) or 0xCC000000.toInt())
        cornerRadius = dp(7).toFloat()
        setStroke(dp(1), 0x55FFFFFF)
    }

    private fun applyTheme(view: LinearLayout) {
        val color = prefs.getInt("bg_color", 0xFF302039.toInt())
        view.setBackgroundColor(color)
        val uri = prefs.getString("background_uri", null)
        if (uri != null) {
            try {
                contentResolver.openInputStream(android.net.Uri.parse(uri))?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    val drawable = BitmapDrawable(resources, bitmap)
                    drawable.alpha = (prefs.getInt("brightness", 65) * 255 / 100)
                    view.background = drawable
                }
            } catch (_: Exception) { }
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}