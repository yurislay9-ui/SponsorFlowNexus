/*
 * SponsorFlow Nexus - Analytics Dashboard Screen
 * Muestra métricas de ROI, conversiones y anti-detección
 */
package com.sponsorflow.nexus.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sponsorflow.nexus.analytics.AnalyticsManager
import com.sponsorflow.nexus.analytics.DashboardData
import com.sponsorflow.nexus.analytics.ROIMetrics
import com.sponsorflow.nexus.analytics.AntiDetectionMetrics
import com.sponsorflow.nexus.core.enums.SubscriptionTier
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(
    onBack: () -> Unit,
    userTier: SubscriptionTier = SubscriptionTier.FREE,
    analyticsManager: AnalyticsManager? = null
) {
    var dashboardData by remember { mutableStateOf<DashboardData?>(null) }

    LaunchedEffect(Unit) {
        dashboardData = analyticsManager?.getDashboardData(7)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Analytics & ROI") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Verificar acceso
            if (userTier == SubscriptionTier.FREE) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFE65100)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Analytics disponible desde plan Básico",
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            // ROI Principal
            dashboardData?.roiMetrics?.let { roi ->
                ROICard(roi)
            }

            // Anti-Detection Status
            dashboardData?.antiDetectionStatus?.let { anti ->
                AntiDetectionCard(anti)
            }

            // Métricas del día
            dashboardData?.dailyStats?.lastOrNull()?.let { today ->
                TodayMetricsCard(today)
            }

            // Gráfico de 7 días
            dashboardData?.dailyStats?.let { stats ->
                WeeklyChartCard(stats)
            }

            // Recomendaciones
            RecommendationsCard(dashboardData)
        }
    }
}

@Composable
fun ROICard(roi: ROIMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "📈 ROI del Asistente",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // ROI Principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    label = "ROI",
                    value = "${roi.roiPercentage.toInt()}%",
                    icon = Icons.Default.Percent,
                    color = Color(0xFF4CAF50)
                )
                MetricItem(
                    label = "Ahorrado",
                    value = "$${roi.costVsHumanAgent.toInt()}",
                    icon = Icons.Default.Savings,
                    color = Color(0xFF2196F3)
                )
            }

            Spacer(Modifier.height(16.dp))

            Divider()

            Spacer(Modifier.height(16.dp))

            // Detalles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem("Mensajes", "${roi.totalMessagesProcessed}")
                DetailItem("Conversiones", "${roi.totalConversions}")
                DetailItem("Ingresos", "$${roi.totalRevenue.toInt()}")
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem("Clientes", "${roi.customersServed}")
                DetailItem("Tiempo ahorrado", "${roi.timeSavedHours.toInt()}h")
                DetailItem("Msgs/cliente", "${roi.avgMessagesPerCustomer.toInt()}")
            }
        }
    }
}

@Composable
fun AntiDetectionCard(anti: AntiDetectionMetrics) {
    val usagePercent = (anti.messagesToday.toFloat() / 500 * 100).toInt()
    val statusColor = when {
        usagePercent < 50 -> Color(0xFF4CAF50)
        usagePercent < 80 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = statusColor
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "🛡️ Anti-Detección WhatsApp",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // Barra de progreso
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Uso diario", fontSize = 12.sp)
                    Text("${anti.messagesToday}/500 ($usagePercent%)", fontSize = 12.sp)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { usagePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = statusColor,
                    trackColor = Color.Gray.copy(alpha = 0.2f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Métricas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SmallMetric("Delay promedio", "${anti.avgDelayBetweenMessagesMs/1000}s")
                SmallMetric("Delays aleatorios", "${anti.randomDelaysAdded}")
                SmallMetric("Alertas", "${anti.rateLimitWarnings}")
            }

            if (usagePercent > 80) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "接近 límite. Considerá reducir mensajes.",
                            fontSize = 12.sp,
                            color = Color(0xFFF44336)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodayMetricsCard(today: com.sponsorflow.nexus.analytics.DailyAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "📅 Hoy",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    label = "Enviados",
                    value = "${today.messagesSent}",
                    icon = Icons.Default.Send,
                    color = Color(0xFF2196F3)
                )
                MetricItem(
                    label = "Recibidos",
                    value = "${today.messagesReceived}",
                    icon = Icons.Default.Inbox,
                    color = Color(0xFF4CAF50)
                )
                MetricItem(
                    label = "Ventas",
                    value = "${today.conversions}",
                    icon = Icons.Default.ShoppingCart,
                    color = Color(0xFFFF9800)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Ingresos hoy: $${today.revenue.toInt()}",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun WeeklyChartCard(stats: List<com.sponsorflow.nexus.analytics.DailyAnalytics>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "📊 Últimos 7 días",
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            // Gráfico simple de barras
            val maxMessages = stats.maxOfOrNull { it.messagesSent } ?: 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                stats.forEach { day ->
                    val height = if (maxMessages > 0) {
                        (day.messagesSent.toFloat() / maxMessages * 80).dp
                    } else 4.dp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(height)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            day.date.takeLast(2),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Totales de la semana
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("Total msgs: ${stats.sumOf { it.messagesSent }}")
                Text("Ventas: ${stats.sumOf { it.conversions }}")
                Text("$${stats.sumOf { it.revenue.toInt() }}")
            }
        }
    }
}

@Composable
fun RecommendationsCard(data: DashboardData?) {
    val recommendations = mutableListOf<String>()

    data?.let {
        // Analizar patrones y generar recomendaciones
        if (it.roiMetrics.roiPercentage < 50) {
            recommendations.add("💡 Mejorá el prompt de la IA para aumentar conversiones")
        }
        if (it.antiDetectionStatus.messagesToday > 400) {
            recommendations.add("⚠️ Estas cerca del límite. Reducí mensajes no esenciales")
        }
        if (it.roiMetrics.avgMessagesPerCustomer < 3) {
            recommendations.add("📝 Los clientes responden poco. Mejorá el engagement")
        }
        if (it.roiMetrics.totalConversions < it.roiMetrics.customersServed * 0.1) {
            recommendations.add("🎯 Tasa de conversión baja. Optimizá las respuestas de ventas")
        }
        if (recommendations.isEmpty()) {
            recommendations.add("✅ Todo va bien! Continua así")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "💡 Recomendaciones",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            recommendations.forEach { rec ->
                Text(
                    rec,
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Componentes auxiliares
@Composable
fun MetricItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            fontWeight = FontWeight.Medium
        )
        Text(
            label,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun SmallMetric(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            label,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}
