package com.sponsorflow.nexus.ui.channels

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

@Composable
fun ChannelConfigScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Configuración de Canales", style = MaterialTheme.typography.headlineSmall)
    }
}
