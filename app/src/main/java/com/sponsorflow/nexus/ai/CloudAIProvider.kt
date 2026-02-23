/*
 * SponsorFlow Nexus v1.0 - Cloud AI Provider
 * Opcional para todos los planes (usuario paga directamente)
 * CORREGIDO: Version actualizada a v1.0
 */
package com.sponsorflow.nexus.ai

import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.core.result.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

enum class CloudProvider {
    OPENAI, GEMINI, ANTHROPIC, OPENROUTER
}

data class CloudAIConfig(
    val provider: CloudProvider,
    val apiKey: String,
    val model: String = "gpt-3.5-turbo"
)

class CloudAIProvider(private val config: CloudAIConfig) {

    // CORREGIDO: Timeouts configurados
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()

    /**
     * Genera respuesta usando provider cloud
     * CORREGIDO: withContext(Dispatchers.IO) para llamada bloqueante
     */
    suspend fun generateResponse(prompt: String): AppResult<String> = withContext(Dispatchers.IO) {
        try {
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
