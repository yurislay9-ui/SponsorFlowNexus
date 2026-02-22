/*
 * SponsorFlow Nexus v2.3 - Offline Queue Manager
 */
package com.sponsorflow.nexus.offline

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object OfflineQueueManager {
    
    private val gson = Gson()
    private lateinit var dao: OfflineQueueDao
    private var serverUrl: String = ""
    
    // Inicializar
    fun init(context: Context, serverUrl: String) {
        this.serverUrl = serverUrl
        dao = OfflineDatabase.getInstance(context).offlineQueueDao()
        
        // Iniciar monitoreo
        ConnectionMonitor.startMonitoring(context, serverUrl)
        
        // Programar sincronización
        SyncWorker.schedule(context)
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
        
        val item = OfflineQueueEntity(
            type = type,
            payload = payloadJson,
            endpoint = if (endpoint.startsWith("http")) endpoint else "$serverUrl$endpoint",
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
    
    // Intento de envío directo
    private suspend fun tryDirectSend(@Suppress("UNUSED_PARAMETER") item: OfflineQueueEntity): Boolean {
        // Lógica de envío directo
        return false // Por defecto, encolar
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