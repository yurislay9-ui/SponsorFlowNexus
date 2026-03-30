/*
 * SponsorFlow Nexus - Ban Detection Screen
 * Detección de bloqueos de WhatsApp
 */
package com.sponsorflow.nexus.ui.ban

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import com.sponsorflow.nexus.ban.BanDetectionManager
import com.sponsorflow.nexus.ban.BanRiskLevel
import com.sponsorflow.nexus.ban.BanStatus
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BanDetectionScreen(
    onBack: () -> Unit = {},
    banManager: BanDetectionManager? = null
) {
    var banStatus by remember { mutableStateOf<BanStatus?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        banStatus = banManager?.checkBanStatus()
        isLoading = false
        while (true) {
            delay(5000)
            banStatus = banManager?.checkBanStatus()
        }
    }

    val statusColor by animateColorAsState(
        targetValue = when (banStatus?.riskLevel) {
            BanRiskLevel.BANNED -> Color(0xFFEF4444)
            BanRiskLevel.DANGER -> Color(0xFFEF4444)
            BanRiskLevel.WARNING -> Color(0xFFF59E0B)
            BanRiskLevel.SAFE -> Color(0xFF10B981)
            null -> Color.Gray
        },
        animationSpec = tween(500),
        label = "statusColor"
    )

    val statusIcon = when (banStatus?.riskLevel) {
        BanRiskLevel.BANNED -> Icons.Default.Block
        BanRiskLevel.DANGER -> Icons.Default.Warning
        BanRiskLevel.WARNING -> Icons.Default.Info
        BanRiskLevel.SAFE -> Icons.Default.Shield
        null -> Icons.Default.QuestionMark
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detección de Bloqueos", fontWeight = FontWeight.Bold) },
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
                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                statusIcon,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = statusColor
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            banStatus?.riskLevel?.name?.replace("_", " ") ?: "Verificando...",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            banStatus?.recommendedAction ?: "Analizando estado de tu cuenta...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text("Indicadores de Riesgo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        IndicatorRow(
                            icon = Icons.Default.Message,
                            label = "Mensajes bloqueados",
                            value = "${banStatus?.indicators?.count { it.contains("block") } ?: 0}",
                            isHigh = banStatus?.indicators?.any { it.contains("block") && it.contains("High") } == true
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        IndicatorRow(
                            icon = Icons.Default.Speed,
                            label = "Límites de velocidad",
                            value = "${banStatus?.indicators?.count { it.contains("rate") } ?: 0}",
                            isHigh = banStatus?.indicators?.any { it.contains("rate") && it.contains("Frequent") } == true
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        IndicatorRow(
                            icon = Icons.Default.Psychology,
                            label = "Respuestas sospechosas",
                            value = "${banStatus?.indicators?.count { it.contains("suspicious") } ?: 0}",
                            isHigh = banStatus?.indicators?.any { it.contains("suspicious") && it.contains("Multiple") } == true
                        )
                    }
                }
            }

            if (banStatus?.indicators?.isNotEmpty() == true) {
                item {
                    Text("Detalles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(banStatus?.indicators ?: emptyList()) { indicator ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(indicator, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cómo funciona", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Esta función monitorea patrones que podrían indicar que tu cuenta está en riesgo de ser bloqueada. " +
                            "Detecta mensajes bloqueados, límites de velocidad y respuestas sospechosas de WhatsApp.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { banManager?.reset() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reiniciar Contadores")
                }
            }
        }
    }
}

@Composable
private fun IndicatorRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isHigh: Boolean
) {
    val color = if (isHigh) Color(0xFFEF4444) else Color(0xFF10B981)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Box(
            modifier = Modifier.clip(CircleShape).background(if (isHigh) Color(0xFFEF4444).copy(alpha = 0.1f) else Color(0xFF10B981).copy(alpha = 0.1f)).padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(value, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
