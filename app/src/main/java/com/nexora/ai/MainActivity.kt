
package com.nexora.ai

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var statusText: TextView
    private lateinit var responseText: TextView
    private lateinit var talkButton: Button
    private lateinit var commandProcessor: CommandProcessor

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        responseText = findViewById(R.id.responseText)
        talkButton = findViewById(R.id.talkButton)

        commandProcessor = CommandProcessor(
            this,
            OfflineAiProvider(getString(R.string.reply_fallback))
        )

        tts = TextToSpeech(this, this)

        talkButton.setOnClickListener {
            startListening()
        }
    }

    private fun startListening() {
        tts?.stop()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        try {
            startActivityForResult(intent, REQUEST_SPEECH)
            statusText.text = getString(R.string.status_listening)
        } catch (e: Exception) {
            statusText.text = getString(R.string.status_unavailable)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        statusText.text = getString(R.string.status_online)

        if (requestCode != REQUEST_SPEECH || resultCode != RESULT_OK) {
            return
        }

        val spoken = data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            .orEmpty()

        responseText.text = getString(R.string.you_prefix, spoken)

        commandProcessor.process(spoken) { answer ->
            runOnUiThread {
                showAnswer(answer)
            }
        }
    }

    private fun showAnswer(answer: String) {
        responseText.text = getString(R.string.assistant_prefix, answer)
        speak(answer)
    }

    private fun speak(text: String) {
        if (ttsReady) {
            tts?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "NEXORA_RESPONSE"
            )
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)

            ttsReady = result != null &&
                result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED

            if (ttsReady) {
                tts?.setSpeechRate(1.15f)
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_SPEECH = 100
    }
}
