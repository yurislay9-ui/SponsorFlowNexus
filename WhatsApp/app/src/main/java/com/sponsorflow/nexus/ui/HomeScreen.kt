package com.sponsorflow.nexus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToInventory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPlugins: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToSmartQueue: () -> Unit = {},
    onNavigateToRiskLevel: () -> Unit = {},
    onNavigateToBanDetection: () -> Unit = {}
) {
    var isActive by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Workflow Hub", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp)) {
                Text(if (isActive) "Servicio Activo" else "Servicio Inactivo")
                Spacer(Modifier.weight(1f))
                Switch(checked = isActive, onCheckedChange = { isActive = it })
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNavigateToInventory, modifier = Modifier.weight(1f)) { Text("Inventario") }
            Button(onClick = onNavigateToAnalytics, modifier = Modifier.weight(1f)) { Text("Stats") }
        }
        
        Spacer(Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNavigateToSmartQueue, modifier = Modifier.weight(1f)) { Text("Cola") }
            Button(onClick = onNavigateToRiskLevel, modifier = Modifier.weight(1f)) { Text("Riesgo") }
        }
    }
}
