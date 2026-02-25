/*
 * SponsorFlow Nexus v1.0 - Resource Download Manager
 */
package com.sponsorflow.nexus.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class ResourceDownloadManager(private val context: Context) {

    // CORREGIDO: Timeouts configurados explícitamente
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun downloadModel(
        url: String,
        fileName: String,
        onProgress: (Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) return@withContext false
            
            val body = response.body ?: return@withContext false
            val contentLength = body.contentLength()
            
            val outputFile = File(context.filesDir, fileName)
            FileOutputStream(outputFile).use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var totalBytesRead = 0L
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        if (contentLength > 0) {
                            val progress = ((totalBytesRead * 100) / contentLength).toInt()
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun isModelDownloaded(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return file.exists()
    }
    
    fun deleteModel(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) file.delete() else false
    }
}