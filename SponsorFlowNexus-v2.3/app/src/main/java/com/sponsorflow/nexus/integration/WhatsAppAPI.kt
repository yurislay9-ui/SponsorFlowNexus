/*
 * SponsorFlow Nexus v2.3 - WhatsApp Business API
 * Plan: ENTERPRISE (Opcional)
 */
package com.sponsorflow.nexus.integration

import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.core.result.AppError
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import com.google.gson.Gson

data class WhatsAppConfig(
    val phoneNumberId: String,
    val accessToken: String,
    val businessAccountId: String
)

class WhatsAppAPI(private val config: WhatsAppConfig) {

    private val client = OkHttpClient.Builder().build()
    private val gson = Gson()
    private val baseUrl = "https://graph.facebook.com/v18.0"

    suspend fun sendMessage(to: String, message: String): AppResult<String> = try {
        val url = "$baseUrl/${config.phoneNumberId}/messages"
        val body = gson.toJson(mapOf(
            "messaging_product" to "whatsapp",
            "to" to to,
            "type" to "text",
            "text" to mapOf("body" to message)
        ))
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${config.accessToken}")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        
        if (response.isSuccessful) {
            AppResult.Success("Mensaje enviado")
        } else {
            AppResult.Error(AppError.NetworkError(response.code))
        }
    } catch (e: Exception) {
        AppResult.Error(AppError.fromException(e))
    }

    suspend fun sendTemplate(to: String, templateName: String, lang: String = "es"): AppResult<String> = try {
        val url = "$baseUrl/${config.phoneNumberId}/messages"
        val body = gson.toJson(mapOf(
            "messaging_product" to "whatsapp",
            "to" to to,
            "type" to "template",
            "template" to mapOf(
                "name" to templateName,
                "language" to mapOf("code" to lang)
            )
        ))
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${config.accessToken}")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        
        if (response.isSuccessful) {
            AppResult.Success("Template enviado")
        } else {
            AppResult.Error(AppError.NetworkError(response.code))
        }
    } catch (e: Exception) {
        AppResult.Error(AppError.fromException(e))
    }
}