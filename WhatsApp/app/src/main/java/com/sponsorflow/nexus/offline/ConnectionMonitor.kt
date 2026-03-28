/*
 * SponsorFlow Nexus v1.0 - Connection Monitor (Estabilidad 30 min)
 * CORREGIDO: Thread-safe con AtomicLong, CopyOnWriteArrayList
 */
package com.sponsorflow.nexus.offline

import android.content.Context
import com.sponsorflow.nexus.network.NetworkHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.Request
import org.json.JSONException
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

object ConnectionMonitor {
    
    private val client = NetworkHelper.createClient()
    private var scope: CoroutineScope? = null
    
    // Estado del servidor
    private val _serverStatus = MutableStateFlow<ServerStatus>(ServerStatus.Unknown)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()
    
    // CORREGIDO: Thread-safe con AtomicLong y CopyOnWriteArrayList
    private val connectionHistory = CopyOnWriteArrayList<Long>()
    private val firstStableTime = AtomicLong(0)
    private val isStable = AtomicBoolean(false)
    private val monitorLock = Object()
    
    // Configuración
    private const val PING_INTERVAL_MS = 2 * 60 * 1000L // 2 minutos
    private const val STABILITY_DURATION_MS = 30 * 60 * 1000L // 30 minutos
    private const val PING_TIMEOUT_MS = 10 * 1000L // 10 segundos
    
    // Endpoint para ping
    private var serverUrl: String = ""
    private var pingEndpoint: String = "/api/health"
    
    // Iniciar monitoreo - CORREGIDO: recreate scope si es necesario
    fun startMonitoring(context: Context, serverUrl: String) {
        this.serverUrl = serverUrl
        
        // Cancelar scope anterior si existe
        scope?.cancel()
        
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        
        scope?.launch {
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
            
            // Timeouts configurados en el cliente
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
        } catch (e: IOException) {
            onServerOffline()
        } catch (e: HttpException) {
            onServerOffline()
        } catch (e: JSONException) {
            onServerOffline()
        } catch (e: SecurityException) {
            onServerOffline()
        } catch (e: Exception) {
            onServerOffline()
        }
    }
    
    // Servidor en línea
    private fun onServerOnline(timestamp: Long) {
        synchronized(monitorLock) {
            connectionHistory.add(timestamp)
            
            // Limpiar historial antiguo (más de 1 hora)
            val oneHourAgo = timestamp - 60 * 60 * 1000
            connectionHistory.removeAll { it < oneHourAgo }
            
            // Verificar estabilidad (conexiones consistentes)
            if (firstStableTime.get() == 0L) {
                firstStableTime.set(timestamp)
            }
            
            val stableDuration = timestamp - firstStableTime.get()
            
            if (stableDuration >= STABILITY_DURATION_MS) {
                isStable.set(true)
                _serverStatus.value = ServerStatus.Stable(
                    since = firstStableTime.get(),
                    durationMinutes = stableDuration / 60000
                )
            } else {
                _serverStatus.value = ServerStatus.Online(
                    stableFor = stableDuration / 60000,
                    needsMinutes = (STABILITY_DURATION_MS - stableDuration) / 60000
                )
            }
        }
    }
    
    // Servidor fuera de línea
    private fun onServerOffline() {
        synchronized(monitorLock) {
            firstStableTime.set(0)
            isStable.set(false)
            // No limpiamos connectionHistory aquí - se limpia en onServerOnline
            _serverStatus.value = ServerStatus.Offline
        }
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
        scope?.cancel()
        scope = null
    }
    
    // Resetear estabilidad (después de sincronización fallida)
    fun resetStability() {
        synchronized(monitorLock) {
            firstStableTime.set(0)
            isStable.set(false)
            connectionHistory.clear()
        }
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