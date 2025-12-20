package com.example.smartshop.domain.model

data class ChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class GroqResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ChatMessage
)