/*
 * SponsorFlow Nexus v2.3 - Connection Monitor (Estabilidad 30 min)
 */
package com.sponsorflow.nexus.offline

import android.content.Context
import com.sponsorflow.nexus.network.NetworkHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.Request
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

object ConnectionMonitor {
    
    private val client = NetworkHelper.createClient()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Estado del servidor
    private val _serverStatus = MutableStateFlow<ServerStatus>(ServerStatus.Unknown)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()
    
    // Timestamps para calcular estabilidad
    private val connectionHistory = mutableListOf<Long>()
    private var firstStableTime: Long = 0
    private val isStable = AtomicBoolean(false)
    
    // Configuración
    private const val PING_INTERVAL_MS = 2 * 60 * 1000L // 2 minutos
    private const val STABILITY_DURATION_MS = 30 * 60 * 1000L // 30 minutos
    private const val PING_TIMEOUT_MS = 10 * 1000L // 10 segundos
    
    // Endpoint para ping
    private var serverUrl: String = ""
    private var pingEndpoint: String = "/api/health"
    
    // Iniciar monitoreo
    fun startMonitoring(@Suppress("UNUSED_PARAMETER") context: Context, serverUrl: String) {
        this.serverUrl = serverUrl
        
        scope.launch {
            while (isActive) {
                checkServerConnection()
                delay(PING_INTERVAL_MS)
            }
        }
    }
    
    // Verificar conexión al servidor
    private suspend fun checkServerConnection() {
        try {
            val request = Request.Builder()
                .url("$serverUrl$pingEndpoint")
                .header("X-Ping", "true")
                .get()
                .build()
            
            val response = withTimeoutOrNull(PING_TIMEOUT_MS) {
                client.newCall(request).execute()
            }
            
            val now = System.currentTimeMillis()
            
            if (response != null && response.isSuccessful) {
                response.close()
                onServerOnline(now)
            } else {
                onServerOffline()
            }
        } catch (e: Exception) {
            onServerOffline()
        }
    }
    
    // Servidor en línea
    private fun onServerOnline(timestamp: Long) {
        connectionHistory.add(timestamp)
        
        // Limpiar historial antiguo (más de 1 hora)
        val oneHourAgo = timestamp - 60 * 60 * 1000
        connectionHistory.removeAll { it < oneHourAgo }
        
        // Verificar estabilidad (conexiones consistentes)
        if (firstStableTime == 0L) {
            firstStableTime = timestamp
        }
        
        val stableDuration = timestamp - firstStableTime
        
        if (stableDuration >= STABILITY_DURATION_MS) {
            isStable.set(true)
            _serverStatus.value = ServerStatus.Stable(
                since = firstStableTime,
                durationMinutes = stableDuration / 60000
            )
        } else {
            _serverStatus.value = ServerStatus.Online(
                stableFor = stableDuration / 60000,
                needsMinutes = (STABILITY_DURATION_MS - stableDuration) / 60000
            )
        }
    }
    
    // Servidor fuera de línea
    private fun onServerOffline() {
        firstStableTime = 0
        isStable.set(false)
        _serverStatus.value = ServerStatus.Offline
    }
    
    // Verificar si está estable (listo para sincronizar)
    fun isStableConnection(): Boolean = isStable.get()
    
    // Forzar verificación inmediata
    suspend fun forceCheck(): ServerStatus {
        checkServerConnection()
        return _serverStatus.value
    }
    
    // Detener monitoreo
    fun stopMonitoring() {
        scope.cancel()
    }
    
    // Resetear estabilidad (después de sincronización fallida)
    fun resetStability() {
        firstStableTime = 0
        isStable.set(false)
        connectionHistory.clear()
    }
}

// Estados del servidor
sealed class ServerStatus {
    object Unknown : ServerStatus()
    object Offline : ServerStatus()
    
    data class Online(
        val stableFor: Long,      // minutos estable
        val needsMinutes: Long    // minutos para ser estable
    ) : ServerStatus()
    
    data class Stable(
        val since: Long,          // timestamp inicio estabilidad
        val durationMinutes: Long // duración estable
    ) : ServerStatus()
    
    fun isReady(): Boolean = this is Stable
    
    fun getDisplayText(): String = when (this) {
        is Unknown -> "Estado: Desconocido"
        is Offline -> "Estado: Offline"
        is Online -> "Estado: Online (${stableFor}min, falta ${needsMinutes}min)"
        is Stable -> "Estado: Estable (${durationMinutes}min)"
    }
}