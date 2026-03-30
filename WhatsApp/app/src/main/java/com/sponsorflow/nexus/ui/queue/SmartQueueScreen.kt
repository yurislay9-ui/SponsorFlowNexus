/*
 * SponsorFlow Nexus - Smart Queue Screen
 * Cola inteligente para gestionar mensajes pendientes
 */
package com.sponsorflow.nexus.ui.queue

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import com.sponsorflow.nexus.queue.MessagePriority
import com.sponsorflow.nexus.queue.QueueConfig
import com.sponsorflow.nexus.queue.SmartQueueManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartQueueScreen(
    onBack: () -> Unit = {},
    queueManager: SmartQueueManager? = null
) {
    var queueConfig by remember { mutableStateOf(QueueConfig()) }
    var queueSize by remember { mutableIntStateOf(0) }
    var isEnabled by remember { mutableStateOf(true) }
    var isExpanded by remember { mutableStateOf(false) }
    var processingDelay by remember { mutableIntStateOf(3) }
    var maxQueueSize by remember { mutableIntStateOf(50) }

    LaunchedEffect(Unit) {
        while (true) {
            queueSize = queueManager?.getQueueSize("default") ?: 0
            delay(2000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cola Inteligente", fontWeight = FontWeight.Bold) },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Estado del Servicio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (isEnabled) "Activo" else "Pausado",
                                color = if (isEnabled) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                        Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "En Cola",
                        value = queueSize.toString(),
                        color = Color(0xFF6366F1),
                        icon = Icons.Default.List
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Capacidad",
                        value = "$maxQueueSize",
                        color = Color(0xFF8B5CF6),
                        icon = Icons.Default.Storage
                    )
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
                            Text(
                                "Configuración Avanzada",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { isExpanded = !isExpanded }) {
                                Icon(
                                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    "Expandir"
                                )
                            }
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Delay entre mensajes: ${processingDelay}s", style = MaterialTheme.typography.bodyMedium)
                            Slider(
                                value = processingDelay.toFloat(),
                                onValueChange = { processingDelay = it.toInt() },
                                valueRange = 1f..30f,
                                steps = 28
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Máximo en cola: $maxQueueSize", style = MaterialTheme.typography.bodyMedium)
                            Slider(
                                value = maxQueueSize.toFloat(),
                                onValueChange = { maxQueueSize = it.toInt() },
                                valueRange = 10f..500f,
                                steps = 48
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    queueConfig = QueueConfig(minDelaySeconds = processingDelay, maxQueueSize = maxQueueSize, enabled = isEnabled)
                                    queueManager?.setConfig(queueConfig)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Save, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Guardar Configuración")
                            }
                        }
                    }
                }
            }

            item {
                Text("Prioridades de Mensajes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(MessagePriority.entries.toTypedArray()) { priority ->
                val (color, description) = when (priority) {
                    MessagePriority.HIGH -> Color(0xFFEF4444) to "Mensajes urgentes con prioridad máxima"
                    MessagePriority.NORMAL -> Color(0xFF6366F1) to "Mensajes estándar en orden de llegada"
                    MessagePriority.LOW -> Color(0xFF8B5CF6) to "Mensajes no urgentes"
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(priority.name, fontWeight = FontWeight.Bold)
                            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cómo funciona", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "La cola inteligente gestiona automáticamente el envío de mensajes, aplicando delays realistas y priorizando mensajes importantes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, title: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
