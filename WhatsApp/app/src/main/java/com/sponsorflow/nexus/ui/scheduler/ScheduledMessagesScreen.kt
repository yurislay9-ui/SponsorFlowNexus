package com.sponsorflow.nexus.ui.scheduler

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

@Composable
fun ScheduledMessagesScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mensajes Programados", style = MaterialTheme.typography.headlineMedium)
    }
}
