/*
 * SponsorFlow Nexus v2.4 - Navigation Host
 */
package com.sponsorflow.nexus.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sponsorflow.nexus.ui.inventory.InventoryManagementScreen
import com.sponsorflow.nexus.ui.settings.IntegrationsScreen

@Composable
fun NexusNavHost(
    navController: NavHostController,
    startDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToInventory = { navController.navigate("inventory") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("inventory") {
            InventoryManagementScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            IntegrationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("chat") {
            AssistantChatScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToInventory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // Pantalla principal simplificada
    androidx.compose.material3.Text("SponsorFlow Nexus - Home")
}