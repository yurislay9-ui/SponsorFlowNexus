/*
 * SponsorFlow Nexus v1.0 - Main Activity
 * Version actualizada a v1.0 - CON ANTI-DETECCIÃN INTEGRADA
 */
package com.sponsorflow.nexus.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sponsorflow.nexus.core.NexusForegroundService
import com.sponsorflow.nexus.ui.theme.NexusTheme
import com.sponsorflow.nexus.queue.SmartQueueManager
import com.sponsorflow.nexus.window.TwentyFourHourWindowManager
import com.sponsorflow.nexus.risk.RiskLevelManager
import com.sponsorflow.nexus.ban.BanDetectionManager
import com.sponsorflow.nexus.human.HumanBehaviorManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Managers de Anti-DetecciÃ³n - inicializados una sola vez
    private lateinit var queueManager: SmartQueueManager
    private lateinit var hourWindowManager: TwentyFourHourWindowManager
    private lateinit var riskManager: RiskLevelManager
    private lateinit var banManager: BanDetectionManager
    private lateinit var humanManager: HumanBehaviorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar managers de Anti-DetecciÃ³n
        initializeAntiDetectionManagers()
        
        startServiceIfNeeded()
        
        setContent {
            NexusTheme {
                val navController = rememberNavController()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pasar managers al NavHost
                    NexusNavHost(
                        navController = navController,
                        queueManager = queueManager,
                        windowManager = hourWindowManager,
                        riskManager = riskManager,
                        banManager = banManager,
                        humanManager = humanManager
                    )
                }
            }
        }
    }

    /**
     * Inicializa todos los managers de Anti-DetecciÃ³n
     */
    private fun initializeAntiDetectionManagers() {
        queueManager = SmartQueueManager(this)
        hourWindowManager = TwentyFourHourWindowManager(this)
        riskManager = RiskLevelManager(this)
        banManager = BanDetectionManager(this)
        humanManager = HumanBehaviorManager(this)
        
        // Cargar configuraciÃ³n guardada
        queueManager.loadFromPrefs()
        windowManager.loadFromPrefs()
        riskManager.loadFromPrefs()
        banManager.loadFromPrefs()
        humanManager.loadFromPrefs()
    }

    private fun startServiceIfNeeded() {
        val intent = Intent(this, NexusForegroundService::class.java).apply {
            action = NexusForegroundService.ACTION_START
        }
        startForegroundService(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Guardar estado al cerrar
        queueManager.saveToPrefs()
        windowManager.saveToPrefs()
        riskManager.saveToPrefs()
        banManager.saveToPrefs()
    }
}
