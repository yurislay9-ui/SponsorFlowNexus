package com.sponsorflow.nexus.ui.autoresponder

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp

@Composable
fun KeywordAutoResponseScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Auto-Respuestas", style = MaterialTheme.typography.headlineMedium)
    }
}
