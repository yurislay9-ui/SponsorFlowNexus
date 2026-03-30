/*
 * Analytics Dashboard UI Components
 */
package com.sponsorflow.nexus.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sponsorflow.nexus.analytics.*

@Composable
fun ROICard(roi: ROIMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Text("📈 ROI del Asistente", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MetricItem(label = "ROI", value = "${roi.roiPercentage.toInt()}%", icon = Icons.Default.Percent, color = Color(0xFF4CAF50))
                MetricItem(label = "Ahorrado", value = "$${roi.costVsHumanAgent.toInt()}", icon = Icons.Default.Savings, color = Color(0xFF2196F3))
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailItem("Mensajes", "${roi.totalMessagesProcessed}")
                DetailItem("Conversiones", "${roi.totalConversions}")
                DetailItem("Ingresos", "$${roi.totalRevenue.toInt()}")
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = color)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
fun AntiDetectionCard(anti: AntiDetectionMetrics) {
    val usagePercent = (anti.messagesToday.toFloat() / 500 * 100).toInt()
    val statusColor = when {
        anti.riskLevel < 30 -> Color(0xFF4CAF50)
        anti.riskLevel < 70 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("🛡️ Anti-Detección", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                Text("Nivel: ${anti.riskLevel}%", color = statusColor, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { usagePercent / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = statusColor
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Msgs hoy: ${anti.messagesToday}/500", fontSize = 12.sp, color = Color.Gray)
                Text("Baneos: ${anti.totalBans}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TodayMetricsCard(today: DailyStats) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📅 Hoy", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatBox("Mensajes", "${today.messagesReceived}", Icons.AutoMirrored.Filled.Message)
                StatBox("Respuestas", "${today.messagesSent}", Icons.AutoMirrored.Filled.Reply)
                StatBox("Ventas", "$${today.revenue}", Icons.Default.AttachMoney)
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}
