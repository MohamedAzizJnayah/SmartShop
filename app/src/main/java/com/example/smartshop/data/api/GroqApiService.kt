package com.example.smartshop.data.api

import com.example.smartshop.domain.model.ChatMessage
import com.example.smartshop.domain.model.GroqResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GroqApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val apiKey = "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"
    private val apiUrl = "https://api.groq.com/openai/v1/chat/completions"

    fun sendMessage(messages: List<ChatMessage>): Result<ChatMessage> = try {
        // Filter only role and content for API request
        val apiMessages = messages.map { msg ->
            mapOf(
                "role" to msg.role,
                "content" to msg.content
            )
        }

        val requestBody = mapOf(
            "model" to "openai/gpt-oss-120b",  // ✅ CHANGED - Use current supported model
            "messages" to apiMessages,
            "max_tokens" to 1024,
            "temperature" to 0.7
        )

        val json = gson.toJson(requestBody)
        val body = json.toRequestBody("application/json".toMediaType())

        val httpRequest = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        val response = client.newCall(httpRequest).execute()
        val responseBody = response.body?.string() ?: ""

        if (response.isSuccessful) {
            try {
                val groqResponse = gson.fromJson(responseBody, GroqResponse::class.java)
                val assistantMessage = groqResponse.choices.firstOrNull()?.message
                    ?: return Result.failure(Exception("Pas de réponse de l'API"))

                Result.success(assistantMessage)
            } catch (e: Exception) {
                Result.failure(Exception("Erreur de parsing: ${e.message}"))
            }
        } else {
            Result.failure(Exception("Erreur API ${response.code}: $responseBody"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}