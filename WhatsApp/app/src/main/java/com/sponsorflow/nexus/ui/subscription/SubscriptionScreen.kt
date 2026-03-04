package com.sponsorflow.nexus.ui.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

@Composable
fun SubscriptionScreen(onBack: () -> Unit = {}, onSubscribe: (String) -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Planes de Suscripción", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("Plan Actual: Gratis")
        Spacer(Modifier.height(24.dp))
        Button(onClick = { onSubscribe("BASICO") }) { Text("Plan Básico - $9.99") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onSubscribe("AVANZADO") }) { Text("Plan Avanzado - $19.99") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onSubscribe("VIP") }) { Text("Plan VIP - $29.99") }
    }
}
