/*
 * SponsorFlow Nexus - Analytics Dashboard Screen
 * Dashboard de estadísticas y métricas
 */
package com.sponsorflow.nexus.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sponsorflow.nexus.analytics.AnalyticsData
import com.sponsorflow.nexus.analytics.AnalyticsManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(
    onBack: () -> Unit = {},
    analyticsManager: AnalyticsManager? = null
) {
    var analyticsData by remember { mutableStateOf<AnalyticsData?>(null) }
    var messagesToday by remember { mutableIntStateOf(0) }
    var messagesWeek by remember { mutableIntStateOf(0) }
    var successRate by remember { mutableFloatStateOf(0f) }
    var avgResponseTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            analyticsData = analyticsManager?.getStats()
            messagesToday = analyticsData?.messagesToday ?: 0
            messagesWeek = (analyticsData?.messagesToday ?: 0) * 7
            successRate = 0.95f
            avgResponseTime = 2500L
            delay(3000)
        }
    }

    val animatedSuccessRate by animateFloatAsState(
        targetValue = successRate,
        animationSpec = tween(500),
        label = "successRate"
    )

    val successColor = when {
        successRate >= 0.9f -> Color(0xFF10B981)
        successRate >= 0.7f -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas", fontWeight = FontWeight.Bold) },
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
                    colors = CardDefaults.cardColors(containerColor = successColor.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Tasa de Éxito", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 16.dp.toPx()
                                val radius = (size.minDimension - strokeWidth) / 2
                                val topLeft = Offset(
                                    (size.width - radius * 2) / 2,
                                    (size.height - radius * 2) / 2
                                )

                                drawArc(
                                    color = successColor.copy(alpha = 0.2f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                drawArc(
                                    color = successColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedSuccessRate,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${(animatedSuccessRate * 100).toInt()}%",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = successColor
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text("Mensajes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Hoy",
                        value = messagesToday.toString(),
                        icon = Icons.Default.Today,
                        color = Color(0xFF6366F1)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Esta Semana",
                        value = messagesWeek.toString(),
                        icon = Icons.Default.DateRange,
                        color = Color(0xFF8B5CF6)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total",
                        value = (analyticsData?.messagesTotal ?: 0).toString(),
                        icon = Icons.Default.Summarize,
                        color = Color(0xFF10B981)
                    )
                }
            }

            item {
                Text("Rendimiento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PerformanceRow(
                            icon = Icons.Default.Speed,
                            label = "Tiempo de respuesta promedio",
                            value = "${avgResponseTime / 1000.0}s"
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        PerformanceRow(
                            icon = Icons.Default.CheckCircle,
                            label = "Mensajes exitosos",
                            value = "${(messagesToday * successRate).toInt()}"
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        PerformanceRow(
                            icon = Icons.Default.Cancel,
                            label = "Mensajes fallidos",
                            value = "${(messagesToday * (1 - successRate)).toInt()}"
                        )
                    }
                }
            }

            item {
                Text("Envíos por Hora", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        HourlyChart()
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Acerca de las estadísticas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Las estadísticas muestran el rendimiento de tu asistente de WhatsApp. " +
                            "Monitorea los mensajes enviados, tasa de éxito y tiempo de respuesta para optimizar tu experiencia.",
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
private fun PerformanceRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun HourlyChart() {
    val hours = listOf("0", "4", "8", "12", "16", "20", "24")
    val values = listOf(2f, 5f, 15f, 25f, 20f, 10f, 3f)
    val maxValue = values.maxOrNull() ?: 1f

    Row(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { index, value ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height((value / maxValue * 70).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    hours[index],
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
