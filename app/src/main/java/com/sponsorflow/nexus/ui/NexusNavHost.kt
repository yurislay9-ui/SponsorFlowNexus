/*
 * SponsorFlow Nexus v2.4 - Navigation Host
 */
package com.sponsorflow.nexus.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    val context = LocalContext.current
    var isActive by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        Text(
            text = "SponsorFlow Nexus",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Tu asistente de WhatsApp con IA",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Botón principal
        Button(
            onClick = { 
                isActive = !isActive
                Toast.makeText(
                    context, 
                    if (isActive) "Servicio activado" else "Servicio desactivado",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isActive) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (isActive) "Detener Asistente" else "Activar Asistente",
                modifier = Modifier.padding(8.dp)
            )
        }
        
        // Botón Chat IA
        OutlinedButton(
            onClick = {
                context.startActivity(
                    Intent(context, AssistantChatActivity::class.java)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("💬 Chat con IA")
        }
        
        // Botón Inventario
        OutlinedButton(
            onClick = onNavigateToInventory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📦 Inventario de Productos")
        }
        
        // Botón Configuración
        OutlinedButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⚙️ Configuración")
        }
        
        // Estado
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Estado del Servicio",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (isActive) "✅ Activo - Escuchando mensajes" else "⏸️ Inactivo",
                    color = if (isActive) 
                        Color(0xFF4CAF50) 
                    else 
                        Color.Gray
                )
            }
        }
    }
}
