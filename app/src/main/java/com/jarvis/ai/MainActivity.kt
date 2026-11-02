package com.jarvis.ai

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.*
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var status: TextView
    private lateinit var response: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(10, 10, 15))
        }

        val title = TextView(this).apply {
            text = "NEXORA AI"
            textSize = 32f
            setTextColor(Color.rgb(255, 50, 50))
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "SYSTEM ONLINE"
            textSize = 16f
            setTextColor(Color.GREEN)
            gravity = Gravity.CENTER
        }

        response = TextView(this).apply {
            text = "Good evening. I am NEXORA AI."
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val mic = Button(this).apply {
            text = "🎙  TALK TO NEXORA AI"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(160, 20, 20))
            setOnClickListener {
                startListening()
            }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(response)

        layout.addView(
            mic,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                150
            )
        )

        setContentView(layout)
    }

    private fun startListening() {
        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )

        try {
            startActivityForResult(intent, 100)
            status.text = "LISTENING..."
        } catch (e: Exception) {
            status.text = "VOICE SERVICE UNAVAILABLE"
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val matches = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )

            val spoken = matches?.firstOrNull() ?: ""

            response.text = "You: $spoken"

            processCommand(spoken)
        }
    }

    private fun processCommand(command: String) {
        val text = command.lowercase(Locale.getDefault())

        val answer = when {
            text.contains("hello") ->
                "Hello. How can I assist you?"

            text.contains("your name") ->
                "I am NEXORA AI, your personal assistant."

            text.contains("time") ->
                "The current time is ${
                    java.text.SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(java.util.Date())
                }"

            text.contains("open youtube") -> {
                val intent = packageManager
                    .getLaunchIntentForPackage(
                        "com.google.android.youtube"
                    )

                if (intent != null) {
                    startActivity(intent)
                    "Opening YouTube."
                } else {
                    "YouTube is not installed."
                }
            }

            else ->
                "I heard you. My AI brain will be connected in the next version."
        }

        response.text = "NEXORA AI: $answer"
        speak(answer)
    }

    private fun speak(text: String) {
        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "NEXORA_RESPONSE"
        )
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
