/*
 * SponsorFlow Nexus v2.3 - Cloud AI Provider
 * Opcional para todos los planes (usuario paga directamente)
 */
package com.sponsorflow.nexus.ai

import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.core.result.AppError
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import com.google.gson.Gson

enum class CloudProvider {
    OPENAI, GEMINI, ANTHROPIC, OPENROUTER
}

data class CloudAIConfig(
    val provider: CloudProvider,
    val apiKey: String,
    val model: String = "gpt-3.5-turbo"
)

class CloudAIProvider(private val config: CloudAIConfig) {

    private val client = OkHttpClient.Builder().build()
    private val gson = Gson()

    suspend fun generateResponse(prompt: String): AppResult<String> = try {
        val url = getApiUrl()
        val body = buildRequestBody(prompt)
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        
        if (response.isSuccessful) {
            val json = response.body?.string() ?: ""
            val result = parseResponse(json)
            AppResult.Success(result)
        } else {
            AppResult.Error(AppError.NetworkError(response.code))
        }
    } catch (e: Exception) {
        AppResult.Error(AppError.fromException(e))
    }

    private fun getApiUrl(): String = when (config.provider) {
        CloudProvider.OPENAI -> "https://api.openai.com/v1/chat/completions"
        CloudProvider.GEMINI -> "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"
        CloudProvider.ANTHROPIC -> "https://api.anthropic.com/v1/messages"
        CloudProvider.OPENROUTER -> "https://openrouter.ai/api/v1/chat/completions"
    }

    private fun buildRequestBody(prompt: String): String {
        return gson.toJson(mapOf(
            "model" to config.model,
            "messages" to listOf(mapOf("role" to "user", "content" to prompt))
        ))
    }

    private fun parseResponse(json: String): String {
        val obj = gson.fromJson(json, Map::class.java)
        val choices = obj["choices"] as? List<*> ?: return ""
        val first = choices.firstOrNull() as? Map<*, *> ?: return ""
        val message = first["message"] as? Map<*, *> ?: return ""
        return message["content"] as? String ?: ""
    }
}