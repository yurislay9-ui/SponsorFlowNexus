package com.sponsorflow.nexus.ui.window

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

@Composable
fun TwentyFourHourWindowScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Ventana de 24 Horas", style = MaterialTheme.typography.headlineMedium)
    }
}
