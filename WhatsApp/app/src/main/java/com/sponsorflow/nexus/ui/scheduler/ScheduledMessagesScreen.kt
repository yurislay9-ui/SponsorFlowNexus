package com.sponsorflow.nexus.ui.scheduler

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Suppress("UNUSED_PARAMETER")
@Composable
fun ScheduledMessagesScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mensajes Programados", style = MaterialTheme.typography.headlineMedium)
    }
}
