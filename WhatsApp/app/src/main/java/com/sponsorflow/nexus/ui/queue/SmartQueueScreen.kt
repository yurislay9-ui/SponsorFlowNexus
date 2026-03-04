package com.sponsorflow.nexus.ui.queue

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

@Composable
fun SmartQueueScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Cola Inteligente", style = MaterialTheme.typography.headlineMedium)
    }
}
