/*
 * SponsorFlow Nexus - 24 Hour Window Screen
 * Monitor de ventana de 24 horas para envío de mensajes
 */
package com.sponsorflow.nexus.ui.window

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sponsorflow.nexus.window.TwentyFourHourWindowManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwentyFourHourWindowScreen(
    onBack: () -> Unit = {},
    windowManager: TwentyFourHourWindowManager? = null
) {
    var messageCount by remember { mutableIntStateOf(0) }
    var remainingCapacity by remember { mutableIntStateOf(1000) }
    var timeUntilNextSlot by remember { mutableLongStateOf(0L) }
    var isLoading by remember { mutableStateOf(true) }

    val maxMessages = 1000
    val usagePercent = messageCount.toFloat() / maxMessages

    val animatedProgress by animateFloatAsState(
        targetValue = usagePercent,
        animationSpec = tween(500),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        isLoading = true
        messageCount = windowManager?.getMessageCount() ?: 0
        remainingCapacity = windowManager?.getRemainingCapacity() ?: 1000
        timeUntilNextSlot = windowManager?.getTimeUntilNextSlot() ?: 0L
        isLoading = false
        while (true) {
            delay(3000)
            messageCount = windowManager?.getMessageCount() ?: 0
            remainingCapacity = windowManager?.getRemainingCapacity() ?: 1000
            timeUntilNextSlot = windowManager?.getTimeUntilNextSlot() ?: 0L
        }
    }

    val progressColor = when {
        usagePercent >= 0.9f -> Color(0xFFEF4444)
        usagePercent >= 0.7f -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ventana de 24 Horas", fontWeight = FontWeight.Bold) },
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
                    colors = CardDefaults.cardColors(containerColor = progressColor.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Uso de la Ventana",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 20.dp.toPx()
                                val radius = (size.minDimension - strokeWidth) / 2
                                val topLeft = Offset(
                                    (size.width - radius * 2) / 2,
                                    (size.height - radius * 2) / 2
                                )

                                drawArc(
                                    color = progressColor.copy(alpha = 0.2f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                drawArc(
                                    color = progressColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedProgress,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${messageCount}",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = progressColor
                                )
                                Text(
                                    "mensajes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${(animatedProgress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = progressColor)
                                Text("Usado", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$remainingCapacity", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                Text("Restante", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$maxMessages", fontWeight = FontWeight.Bold)
                                Text("Total", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tiempo hasta siguiente slot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        if (remainingCapacity > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Capacidad disponible",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF10B981)
                                )
                            }
                        } else {
                            val hours = timeUntilNextSlot / 3600000
                            val minutes = (timeUntilNextSlot % 3600000) / 60000
                            val seconds = (timeUntilNextSlot % 60000) / 1000

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TimeUnit(value = hours.toInt(), label = "Horas")
                                TimeUnit(value = minutes.toInt(), label = "Min")
                                TimeUnit(value = seconds.toInt(), label = "Seg")
                            }
                        }
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
                        title = "Enviados",
                        value = messageCount.toString(),
                        icon = Icons.Default.Send,
                        color = Color(0xFF6366F1)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Pendientes",
                        value = "0",
                        icon = Icons.Default.Pending,
                        color = Color(0xFFF59E0B)
                    )
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Acerca de la ventana de 24h", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "WhatsApp limita el número de mensajes que puedes enviar en un período de 24 horas. " +
                            "Esta función monitorea tu uso y te ayuda a no exceder los límites para evitar bloqueos. " +
                            "El contador se reinicia automáticamente cada 24 horas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { windowManager?.reset() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reiniciar Contador")
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TimeUnit(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            String.format("%02d", value),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF59E0B)
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
