package com.sponsorflow.nexus.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

@Composable
fun AnalyticsDashboardScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Estadísticas", style = MaterialTheme.typography.headlineMedium)
    }
}
