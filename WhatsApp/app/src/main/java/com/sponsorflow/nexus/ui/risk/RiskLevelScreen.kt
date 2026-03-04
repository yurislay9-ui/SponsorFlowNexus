package com.sponsorflow.nexus.ui.risk

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

@Composable
fun RiskLevelScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Nivel de Riesgo", style = MaterialTheme.typography.headlineMedium)
    }
}
