/*
 * SponsorFlow Nexus v2.3 - Server Status View (Debug UI)
 */
package com.sponsorflow.nexus.offline

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ServerStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val queueCountText: TextView
    private val serverStatusText: TextView
    
    private var isDebugMode: Boolean = false
    private var isAdminMode: Boolean = false

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setPadding(16, 8, 16, 8)
        
        queueCountText = TextView(context).apply {
            textSize = 12f
        }
        serverStatusText = TextView(context).apply {
            textSize = 12f
        }
        
        addView(queueCountText)
        addView(serverStatusText)
    }
    
    // Configurar visibilidad
    fun setMode(debugMode: Boolean, adminMode: Boolean = false) {
        this.isDebugMode = debugMode
        this.isAdminMode = adminMode
        
        // Solo visible si es debug o admin
        visibility = if (debugMode || adminMode) VISIBLE else GONE
    }
    
    // Iniciar observación
    fun observe(lifecycleOwner: LifecycleOwner) {
        if (!isDebugMode && !isAdminMode) return
        
        // Observar conteo de cola
        lifecycleOwner.lifecycleScope.launch {
            OfflineQueueManager.getQueueCount().collectLatest { count ->
                queueCountText.text = "Elementos en cola: $count"
                queueCountText.setTextColor(
                    if (count > 0) 0xFFFF9800.toInt() else 0xFF4CAF50.toInt()
                )
            }
        }
        
        // Observar estado del servidor
        lifecycleOwner.lifecycleScope.launch {
            OfflineQueueManager.getServerStatus().collectLatest { status ->
                serverStatusText.text = status.getDisplayText()
                serverStatusText.setTextColor(
                    when (status) {
                        is ServerStatus.Stable -> 0xFF4CAF50.toInt()
                        is ServerStatus.Online -> 0xFFFFC107.toInt()
                        is ServerStatus.Offline -> 0xFFF44336.toInt()
                        is ServerStatus.Unknown -> 0xFF9E9E9E.toInt()
                    }
                )
            }
        }
    }
    
    // Actualización manual
    fun updateStatus(queueCount: Int, status: ServerStatus) {
        queueCountText.text = "Elementos en cola: $queueCount"
        serverStatusText.text = status.getDisplayText()
    }
    
    // Forzar sincronización (solo admin)
    fun forceSync() {
        if (isAdminMode) {
            OfflineQueueManager.forceSync(context)
        }
    }
}