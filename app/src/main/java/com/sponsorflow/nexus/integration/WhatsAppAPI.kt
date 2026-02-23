/*
 * SponsorFlow Nexus v1.0 - WhatsApp Business API
 * Plan: ENTERPRISE (Opcional)
 * CORREGIDO: Version actualizada a v1.0
 */
package com.sponsorflow.nexus.integration

import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.core.result.AppError
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

data class WhatsAppConfig(
    val phoneNumberId: String,
    val accessToken: String,
    val businessAccountId: String
)

class WhatsAppAPI(private val config: WhatsAppConfig) {

    // CORREGIDO: Timeouts configurados
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    private val baseUrl = "https://graph.facebook.com/v18.0"

    /**
     * Envía mensaje de WhatsApp
     * CORREGIDO: withContext(Dispatchers.IO) para llamada bloqueante
     */
    suspend fun sendMessage(to: String, message: String): AppResult<String> = withContext(Dispatchers.IO) {
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
