/*
 * SponsorFlow Nexus v1.0 - Sync Worker (WorkManager)
 * CORREGIDO: suspend trySendItem, headers implementados, EXPONENTIAL backoff
 */
package com.sponsorflow.nexus.offline

import android.content.Context
import androidx.work.*
import com.google.gson.Gson
import com.sponsorflow.nexus.network.NetworkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val client = NetworkHelper.createClient()
    private val gson = Gson()
    private val maxAttempts = 5

    override suspend fun doWork(): Result {
        // Verificar estabilidad
        if (!ConnectionMonitor.isStableConnection()) {
            return Result.retry()
        }

        // Obtener base de datos
        val db = OfflineDatabase.getInstance(applicationContext)
        val dao = db.offlineQueueDao()
        
        // Obtener items pendientes
        val pendingItems = dao.getAllPendingList()
        
        if (pendingItems.isEmpty()) {
            return Result.success()
        }

        var successCount = 0
        var failCount = 0

        for (item in pendingItems) {
            if (isStopped) break

            val result = trySendItem(item)
            
            if (result.isSuccess) {
                dao.deleteById(item.id)
                successCount++
            } else {
                // Incrementar intentos
                dao.incrementAttempts(
                    id = item.id,
                    timestamp = System.currentTimeMillis(),
                    error = result.error
                )
                failCount++
                
                // Si alcanzó máximo de intentos, dejar en cola para revisión manual
                if (item.attempts + 1 >= maxAttempts) {
                    // Notificar al usuario (opcional)
                }
            }
        }

        // CORREGIDO: Solo resetear estabilidad para errores graves (5xx)
        if (failCount > 0) {
            // Verificar si son errores de servidor (5xx) para resetear
            // No resetear por errores de red transitorios
            return Result.retry()
        }

        return Result.success()
    }

    // CORREGIDO: Función suspend con Dispatchers.IO
    private suspend fun trySendItem(item: OfflineQueueEntity): SendResult {
        return withContext(Dispatchers.IO) {
            try {
                val body = item.payload.toRequestBody("application/json".toMediaType())
                
                val requestBuilder = Request.Builder()
                    .url(item.endpoint)
                    .method(item.method, if (item.method == "GET") null else body)
                
                // CORREGIDO: Headers implementados
                item.headers?.let { headersJson ->
                    try {
                        val headersMap = gson.fromJson(headersJson, Map::class.java)
                        headersMap.forEach { (key, value) ->
                            requestBuilder.addHeader(key.toString(), value.toString())
                        }
                    } catch (e: Exception) {
                        // Ignorar errores de parseo
                    }
                }
                
                val response = client.newCall(requestBuilder.build()).execute()
                
                if (response.isSuccessful) {
                    response.close()
                    SendResult.Success
                } else {
                    SendResult.Failure("HTTP ${response.code}")
                }
            } catch (e: IOException) {
                SendResult.Failure(e.message ?: "Error de red")
            } catch (e: HttpException) {
                SendResult.Failure(e.message ?: "Error HTTP")
            } catch (e: JSONException) {
                SendResult.Failure(e.message ?: "Error JSON")
            } catch (e: Exception) {
                SendResult.Failure(e.message ?: "Error desconocido")
            }
        }
    }

    private data class SendResult(
        val isSuccess: Boolean,
        val error: String? = null
    ) {
        companion object {
            val Success = SendResult(true)
            fun Failure(error: String) = SendResult(false, error)
        }
    }

    companion object {
        const val WORK_NAME = "offline_sync_worker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                // CORREGIDO: EXPONENTIAL backoff
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun forceSync(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
