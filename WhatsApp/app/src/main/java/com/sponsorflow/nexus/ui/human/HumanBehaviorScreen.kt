/*
 * SponsorFlow Nexus - Human Behavior Screen
 * Configuración de comportamiento humano para anti-detección
 */
package com.sponsorflow.nexus.ui.human

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sponsorflow.nexus.human.HumanBehaviorConfig
import com.sponsorflow.nexus.human.HumanBehaviorManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HumanBehaviorScreen(
    onBack: () -> Unit = {},
    humanManager: HumanBehaviorManager? = null
) {
    var isEnabled by remember { mutableStateOf(true) }
    var minDelay by remember { mutableFloatStateOf(3f) }
    var maxDelay by remember { mutableFloatStateOf(15f) }
    var activeHourStart by remember { mutableIntStateOf(8) }
    var activeHourEnd by remember { mutableIntStateOf(22) }
    var shortResponseChance by remember { mutableFloatStateOf(0.3f) }
    var showAdvanced by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comportamiento Humano", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEnabled) Color(0xFF10B981).copy(alpha = 0.1f)
                                         else Color(0xFFEF4444).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Simulación de Comportamiento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                if (isEnabled) "Activo - Respuestas más humanas" else "Inactivo",
                                color = if (isEnabled) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                        Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Configuración de Delays", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showAdvanced = !showAdvanced }) {
                                Icon(if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Expandir")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Delay mínimo: ${minDelay.toInt()}s", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = minDelay,
                            onValueChange = { minDelay = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Delay máximo: ${maxDelay.toInt()}s", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = maxDelay,
                            onValueChange = { maxDelay = it },
                            valueRange = 5f..60f,
                            steps = 54,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFEF4444), activeTrackColor = Color(0xFFEF4444))
                        )

                        if (minDelay >= maxDelay) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "El delay mínimo debe ser menor que el máximo",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Horas Activas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Hora de inicio: ${String.format("%02d:00", activeHourStart)}", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = activeHourStart.toFloat(),
                            onValueChange = { activeHourStart = it.toInt() },
                            valueRange = 0f..23f,
                            steps = 22
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Hora de fin: ${String.format("%02d:00", activeHourEnd)}", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = activeHourEnd.toFloat(),
                            onValueChange = { activeHourEnd = it.toInt() },
                            valueRange = 0f..23f,
                            steps = 22
                        )

                        if (activeHourStart >= activeHourEnd) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "La hora de inicio debe ser menor que la hora de fin",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Respuestas Cortas Aleatorias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Probabilidad de usar respuestas cortas como 'ok', 'si', 'gracias'",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Probabilidad: ${(shortResponseChance * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = shortResponseChance,
                            onValueChange = { shortResponseChance = it },
                            valueRange = 0f..1f
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val config = HumanBehaviorConfig(
                            enabled = isEnabled,
                            minDelaySeconds = minDelay.toInt(),
                            maxDelaySeconds = maxDelay.toInt(),
                            activeHourStart = activeHourStart,
                            activeHourEnd = activeHourEnd,
                            shortResponseChance = shortResponseChance
                        )
                        humanManager?.setConfig(config)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Configuración")
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Por qué es importante", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "WhatsApp detecta patrones automatizados analizando los tiempos de respuesta y comportamiento. " +
                            "Con esta configuración, el asistente simula comportamientos humanos realistas para evitar bloqueos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text("Ejemplos de Respuestas Cortas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(
                listOf("ok", "si", "no", "gracias", "de nada", "perfecto", "claro", "bien", "vale", "confirmado")
            ) { response ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(response, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
