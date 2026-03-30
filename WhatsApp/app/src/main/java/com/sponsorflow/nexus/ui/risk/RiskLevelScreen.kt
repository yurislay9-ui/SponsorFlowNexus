package com.sponsorflow.nexus.ui.risk

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Suppress("UNUSED_PARAMETER")
@Composable
fun RiskLevelScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Nivel de Riesgo", style = MaterialTheme.typography.headlineMedium)
    }
}
