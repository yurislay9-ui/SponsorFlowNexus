/**
 * SponsorFlow Nexus v1.0 - Navigation Host
 * 
 * Componente encargado de la gestión de navegación en la aplicación.
 * Implementa un sistema de rutas seguro y robusto.
 * 
 * @author SponsorFlow Nexus Team
 * @version 1.0
 */
package com.sponsorflow.nexus.ui

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sponsorflow.nexus.ui.inventory.InventoryManagementScreen
import com.sponsorflow.nexus.ui.settings.IntegrationsScreen

/**
 * Componente principal de navegación de la aplicación.
 * Gestiona todas las rutas de la aplicación.
 * 
 * @param navController Controlador de navegación
 * @param startDestination Ruta de destino inicial
 * @param queueManager Manager de cola inteligente
 * @param windowManager Manager de ventana de 24h
 * @param riskManager Manager de nivel de riesgo
 * @param banManager Manager de detección de bloqouos
 * @param humanManager Manager de comportamiento humano
 */
@Composable
fun NexusNavHost(
    navController: NavHostController,
    startDestination: String = "home",
    // Managers para Anti-Detección
    queueManager: com.sponsorflow.nexus.queue.SmartQueueManager? = null,
    windowManager: com.sponsorflow.nexus.window.TwentyFourHourWindowManager? = null,
    riskManager: com.sponsorflow.nexus.risk.RiskLevelManager? = null,
    banManager: com.sponsorflow.nexus.ban.BanDetectionManager? = null,
    humanManager: com.sponsorflow.nexus.human.HumanBehaviorManager? = null
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Home Screen
        composable("home") {
            HomeScreen(
                onNavigateToInventory = { navController.navigate("inventory") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToPlugins = { navController.navigate("plugins") },
                onNavigateToAnalytics = { navController.navigate("analytics") },
                onNavigateToSmartQueue = { navController.navigate("smart_queue") },
                onNavigateToRiskLevel = { navController.navigate("risk_level") },
                onNavigateToBanDetection = { navController.navigate("ban_detection") }
            )
        }
        
        // Inventory Screen
        composable("inventory") {
            InventoryManagementScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        // Settings Screen
        composable("settings") {
            IntegrationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        // Chat Screen
        composable("chat") {
            AssistantChatScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        // Anti-Detección: Smart Queue
        composable("smart_queue") {
            queueManager?.let {
                com.sponsorflow.nexus.ui.queue.SmartQueueScreen(
                    onBack = { navController.popBackStack() },
                    queueManager = it
                )
            }
        }
        
        // Anti-Detección: Human Behavior
        composable("human_behavior") {
            humanManager?.let {
                com.sponsorflow.nexus.ui.human.HumanBehaviorScreen(
                    onBack = { navController.popBackStack() },
                    manager = it
                )
            }
        }
        
        // Anti-Detección: 24h Window
        composable("window_24h") {
            windowManager?.let {
                com.sponsorflow.nexus.ui.window.TwentyFourHourWindowScreen(
                    onBack = { navController.popBackStack() },
                    windowManager = it
                )
            }
        }
        
        // Anti-Detección: Risk Level
        composable("risk_level") {
            riskManager?.let {
                com.sponsorflow.nexus.ui.risk.RiskLevelScreen(
                    onBack = { navController.popBackStack() },
                    riskManager = it
                )
            }
        }
        
        // Anti-Detección: Ban Detection
        composable("ban_detection") {
            banManager?.let {
                com.sponsorflow.nexus.ui.ban.BanDetectionScreen(
                    onBack = { navController.popBackStack() },
                    banManager = it
                )
            }
        }
        
        // Analytics
        composable("analytics") {
            com.sponsorflow.nexus.ui.dashboard.AnalyticsDashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        // Plugins
        composable("plugins") {
            com.sponsorflow.nexus.ui.plugins.PluginManagerScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
