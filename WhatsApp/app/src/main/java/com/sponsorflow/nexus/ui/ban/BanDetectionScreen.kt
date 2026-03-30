package com.sponsorflow.nexus.ui.ban

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Suppress("UNUSED_PARAMETER")
@Composable
fun BanDetectionScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Detección de Bloqueos", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Estado: Seguro")
    }
}
