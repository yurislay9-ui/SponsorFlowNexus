/*
 * SponsorFlow Nexus v2.3 - Sync Worker (WorkManager)
 */
package com.sponsorflow.nexus.offline

import android.content.Context
import androidx.work.*
import com.sponsorflow.nexus.network.NetworkHelper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val client = NetworkHelper.createClient()
    private val maxAttempts = 5

    override suspend fun doWork(): Result {
        // Verificar estabilidad
        if (!ConnectionMonitor.isStableConnection()) {
            // Reintentar en 15 minutos
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

        // Si hubo algún fallo, resetear estabilidad
        if (failCount > 0) {
            ConnectionMonitor.resetStability()
            return Result.retry()
        }

        return Result.success()
    }

    private fun trySendItem(item: OfflineQueueEntity): SendResult {
        return try {
            val body = item.payload.toRequestBody("application/json".toMediaType())
            
            val requestBuilder = Request.Builder()
                .url(item.endpoint)
                .method(item.method, if (item.method == "GET") null else body)
            
            // Headers adicionales
            item.headers?.let { 
                // Parsear headers JSON y agregar
            }
            
            val response = client.newCall(requestBuilder.build()).execute()
            
            if (response.isSuccessful) {
                response.close()
                SendResult.Success
            } else {
                SendResult.Failure("HTTP ${response.code}")
            }
        } catch (e: Exception) {
            SendResult.Failure(e.message ?: "Error desconocido")
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
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    5, TimeUnit.MINUTES
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