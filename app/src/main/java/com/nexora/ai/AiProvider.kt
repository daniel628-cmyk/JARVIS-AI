package com.nexora.ai

/**
 * Contract for anything that can answer a prompt.
 * A future online AI provider will implement this interface.
 * The reply must be delivered by calling onResult exactly once.
 */
interface AiProvider {
    fun reply(prompt: String, onResult: (String) -> Unit)
}

/**
 * Placeholder provider used until a real AI model is connected.
 * It always answers with the same fixed message.
 */
class OfflineAiProvider(private val message: String) : AiProvider {
    override fun reply(prompt: String, onResult: (String) -> Unit) {
        onResult(message)
    }
}
