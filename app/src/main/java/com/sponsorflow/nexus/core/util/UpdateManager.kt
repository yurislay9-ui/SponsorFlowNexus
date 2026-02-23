/*
 * SponsorFlow Nexus v2.4 - Update Manager
 * CORREGIDO: withContext(Dispatchers.IO) para llamadas de red
 */
package com.sponsorflow.nexus.core.util

import android.content.Context
import android.util.Log
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val changelog: String,
    val isMandatory: Boolean
)

class UpdateManager(private val context: Context) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private val comparator = VersionComparator()
    private val gson = Gson()
    
    // Verificar actualización disponible
    // CORREGIDO: withContext(Dispatchers.IO)
    suspend fun checkUpdate(
        releaseUrl: String,
        currentVersion: String
    ): AppResult<UpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(releaseUrl)
                .build()
            
            val response = client.newCall(request).execute()
            
            when (response.code) {
                404 -> {
                    Log.w("Nexus", "Release no encontrado: $releaseUrl")
                    AppResult.Success(null) // No hay actualización disponible
                }
                in 200..299 -> {
                    val json = response.body?.string() ?: ""
                    parseGitHubRelease(json, currentVersion)
                }
                else -> {
                    AppResult.Error(AppError.NetworkError(response.code))
                }
            }
            
        } catch (e: Exception) {
            Log.e("Nexus", "Error verificando actualización: ${e.message}")
            AppResult.Error(AppError.fromException(e))
        }
    }
    
    // Parsear respuesta de GitHub API
    private fun parseGitHubRelease(json: String, currentVersion: String): AppResult<UpdateInfo?> {
        return try {
            val obj = gson.fromJson(json, JsonObject::class.java)
            
            val tagName = obj.get("tag_name")?.asString ?: ""
            val version = tagName.removePrefix("v").removePrefix("V")
            
            // Verificar si es versión nueva
            if (!comparator.needsUpdate(currentVersion, version)) {
                return AppResult.Success(null)
            }
            
            val assets = obj.getAsJsonArray("assets")
            val apkAsset = assets?.firstOrNull {
                it.asJsonObject.get("name")?.asString?.endsWith(".apk") == true
            }
            
            val downloadUrl = apkAsset?.asJsonObject?.get("browser_download_url")?.asString ?: ""
            val body = obj.get("body")?.asString ?: ""
            
            AppResult.Success(UpdateInfo(
                version = version,
                downloadUrl = downloadUrl,
                changelog = body,
                isMandatory = comparator.isMajorUpdate(currentVersion, version)
            ))
            
        } catch (e: Exception) {
            Log.e("Nexus", "Error parseando release: ${e.message}")
            AppResult.Success(null) // Error silencioso, no bloquear app
        }
    }
    
    // Verificar versión mínima requerida
    fun checkMinVersion(current: String, minimum: String): VersionResult {
        return comparator.compare(current, minimum)
    }
    
    // Verificar si actualización es obligatoria
    fun isMandatoryUpdate(current: String, minimum: String): Boolean {
        return comparator.isMajorUpdate(current, minimum)
    }
}
