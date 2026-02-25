/*
 * SponsorFlow Nexus v1.0 - Offline Queue Manager
 * CORREGIDO: tryDirectSend implementado, init con flag de inicialización
 */
package com.sponsorflow.nexus.offline

import android.content.Context
import com.google.gson.Gson
import com.sponsorflow.nexus.network.NetworkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object OfflineQueueManager {
    
    private val gson = Gson()
    private val client = NetworkHelper.createClient()
    private lateinit var dao: OfflineQueueDao
    private var serverUrl: String = ""
    
    // CORREGIDO: Flag para evitar múltiples inicializaciones
    @Volatile private var isInitialized = false
    
    // Inicializar - idempotente
    fun init(context: Context, serverUrl: String) {
        if (isInitialized) return
        
        this.serverUrl = serverUrl
        dao = OfflineDatabase.getInstance(context).offlineQueueDao()
        
        // Iniciar monitoreo
        ConnectionMonitor.startMonitoring(context, serverUrl)
        
        // Programar sincronización
        SyncWorker.schedule(context)
        
        isInitialized = true
    }
    
    // Enviar petición (con cola offline)
    suspend fun enqueue(
        type: String,
        payload: Any,
        endpoint: String,
        method: String = "POST",
        headers: Map<String, String>? = null,
        priority: Int = 2
    ): QueueResult {
        val payloadJson = gson.toJson(payload)
        val headersJson = headers?.let { gson.toJson(it) }
        
        val fullEndpoint = if (endpoint.startsWith("http")) endpoint else "$serverUrl$endpoint"
        
        val item = OfflineQueueEntity(
            type = type,
            payload = payloadJson,
            endpoint = fullEndpoint,
            method = method,
            headers = headersJson,
            priority = priority
        )
        
        // Intentar enviar directamente primero
        if (ConnectionMonitor.isStableConnection()) {
            val sent = tryDirectSend(item)
            if (sent) {
                return QueueResult.SentDirectly
            }
        }
        
        // Si falla, guardar en cola
        dao.insert(item)
        return QueueResult.QueuedLocally
    }
    
    // CORREGIDO: Implementar lógica de envío directo
    private suspend fun tryDirectSend(item: OfflineQueueEntity): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = item.payload.toRequestBody("application/json".toMediaType())
                
                val requestBuilder = Request.Builder()
                    .url(item.endpoint)
                    .method(item.method, requestBody)
                
                // Agregar headers si existen
                item.headers?.let { headersJson ->
                    try {
                        val headersMap = gson.fromJson(headersJson, Map::class.java)
                        headersMap.forEach { (key, value) ->
                            requestBuilder.addHeader(key.toString(), value.toString())
                        }
                    } catch (e: JSONException) {
                        // Ignorar errores de parseo de headers
                    }
                }
                
                val response = client.newCall(requestBuilder.build()).execute()
                
                response.isSuccessful.also {
                    response.close()
                }
            } catch (e: IOException) {
                false
            } catch (e: HttpException) {
                false
            } catch (e: JSONException) {
                false
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // Obtener conteo de items en cola
    fun getQueueCount(): Flow<Int> = dao.getCount()
    
    // Obtener items pendientes
    fun getPendingItems(): Flow<List<OfflineQueueEntity>> = dao.getAllPending()
    
    // Obtener items fallidos
    suspend fun getFailedItems(): List<OfflineQueueEntity> = dao.getFailedItems(5)
    
    // Limpiar cola (solo para admin)
    suspend fun clearQueue() {
        dao.deleteAll()
    }
    
    // Reintentar item específico
    suspend fun retryItem(id: Long) {
        val item = dao.getAllPendingList().find { it.id == id } ?: return
        // Resetear intentos
        dao.update(item.copy(attempts = 0, lastError = null))
        // Forzar sincronización
    }
    
    // Forzar sincronización manual
    fun forceSync(context: Context) {
        SyncWorker.forceSync(context)
    }
    
    // Estado del servidor
    fun getServerStatus() = ConnectionMonitor.serverStatus
    
    // Verificar si está listo para sincronizar
    fun isReadyToSync(): Boolean = ConnectionMonitor.isStableConnection()
}

sealed class QueueResult {
    object SentDirectly : QueueResult()
    object QueuedLocally : QueueResult()
    data class Error(val message: String) : QueueResult()
    
    fun getDisplayMessage(): String = when (this) {
        is SentDirectly -> "Enviado correctamente"
        is QueuedLocally -> "Procesado localmente"
        is Error -> "Error: $message"
    }
}
