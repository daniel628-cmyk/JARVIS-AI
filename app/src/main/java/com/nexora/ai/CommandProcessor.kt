package com.nexora.ai

import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommandProcessor(
    private val context: Context,
    private val aiProvider: AiProvider
) {

    private class SupportedApp(
        val displayName: String,
        val keyword: String,
        val packageName: String
    )

    private val supportedApps = listOf(
        SupportedApp("YouTube", "youtube", "com.google.android.youtube"),
        SupportedApp("Chrome", "chrome", "com.android.chrome"),
        SupportedApp("Gmail", "gmail", "com.google.android.gm"),
        SupportedApp("Maps", "maps", "com.google.android.apps.maps"),
        SupportedApp("WhatsApp", "whatsapp", "com.whatsapp"),
        SupportedApp("Spotify", "spotify", "com.spotify.music"),
        SupportedApp("Settings", "settings", "com.android.settings")
        
    )

    private val greetingPattern = Regex("\\b(hello|hi|hey)\\b")
    private val timePattern = Regex("\\btime\\b")

    fun process(command: String, onResult: (String) -> Unit) {
        val text = command.lowercase(Locale.getDefault()).trim()

        when {
            text.isEmpty() ->
                onResult(context.getString(R.string.reply_no_speech))

            greetingPattern.containsMatchIn(text) ->
                onResult(context.getString(R.string.reply_hello))

            text.contains("your name") || text.contains("who are you") ->
                onResult(context.getString(R.string.reply_name))

            timePattern.containsMatchIn(text) ->
                onResult(currentTimeReply())

            text.contains("open") -> {
                val app = supportedApps.firstOrNull { text.contains(it.keyword) }
                if (app != null) {
                    onResult(openApp(app))
                } else {
                    aiProvider.reply(command, onResult)
                }
            }

            else ->
                aiProvider.reply(command, onResult)
        }
    }

    private fun currentTimeReply(): String {
        val formatted = SimpleDateFormat("hh:mm a", Locale.getDefault())
            .format(Date())
        return context.getString(R.string.reply_time, formatted)
    }

    private fun openApp(app: SupportedApp): String {
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(app.packageName)
            ?: return context.getString(R.string.reply_not_installed, app.displayName)

        return try {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            context.getString(R.string.reply_opening, app.displayName)
        } catch (e: Exception) {
            context.getString(R.string.reply_not_installed, app.displayName)
        }
    }
}
