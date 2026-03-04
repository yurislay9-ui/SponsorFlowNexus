package com.sponsorflow.nexus.ui.human

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

@Composable
fun HumanBehaviorScreen(onBack: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Comportamiento Humano", style = MaterialTheme.typography.headlineMedium)
    }
}
