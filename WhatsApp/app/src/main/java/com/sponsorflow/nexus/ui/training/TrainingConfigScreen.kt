package com.sponsorflow.nexus.ui.training

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrainingConfigScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Configuración de Entrenamiento", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("Entrena a tu asistente de IA")
        Spacer(Modifier.height(8.dp))
        Button(onClick = {}) { Text("Agregar FAQ") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {}) { Text("Agregar Producto") }
    }
}
