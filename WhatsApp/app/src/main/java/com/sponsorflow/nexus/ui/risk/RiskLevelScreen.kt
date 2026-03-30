/*
 * SponsorFlow Nexus - Risk Level Screen
 * Monitor de nivel de riesgo de la cuenta
 */
package com.sponsorflow.nexus.ui.risk

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.sp
import com.sponsorflow.nexus.risk.RiskAssessment
import com.sponsorflow.nexus.risk.RiskLevel
import com.sponsorflow.nexus.risk.RiskLevelManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskLevelScreen(
    onBack: () -> Unit = {},
    riskManager: RiskLevelManager? = null
) {
    var riskAssessment by remember { mutableStateOf<RiskAssessment?>(null) }
    var consecutiveFailures by remember { mutableIntStateOf(0) }
    var errorRate by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        riskAssessment = riskManager?.assessRisk()
        isLoading = false
        while (true) {
            delay(5000)
            riskAssessment = riskManager?.assessRisk()
        }
    }

    val riskColor by animateColorAsState(
        targetValue = when (riskAssessment?.level) {
            RiskLevel.CRITICAL -> Color(0xFFEF4444)
            RiskLevel.HIGH -> Color(0xFFF59E0B)
            RiskLevel.MEDIUM -> Color(0xFF6366F1)
            RiskLevel.LOW -> Color(0xFF10B981)
            null -> Color.Gray
        },
        animationSpec = tween(500),
        label = "riskColor"
    )

    val riskScore by animateFloatAsState(
        targetValue = (riskAssessment?.score ?: 0).toFloat(),
        animationSpec = tween(500),
        label = "riskScore"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nivel de Riesgo", fontWeight = FontWeight.Bold) },
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
                    colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Nivel de Riesgo Actual",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { riskScore / 100f },
                                modifier = Modifier.size(120.dp),
                                strokeWidth = 12.dp,
                                color = riskColor,
                                trackColor = riskColor.copy(alpha = 0.2f)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${riskScore.toInt()}",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor
                                )
                                Text(
                                    riskAssessment?.level?.name ?: "Desconocido",
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            riskAssessment?.recommendation ?: "Cargando...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text("Factores de Riesgo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        RiskFactorRow(
                            icon = Icons.Default.Warning,
                            label = "Fallos consecutivos",
                            value = "$consecutiveFailures",
                            status = when {
                                consecutiveFailures >= 15 -> RiskLevel.CRITICAL
                                consecutiveFailures >= 7 -> RiskLevel.HIGH
                                consecutiveFailures >= 3 -> RiskLevel.MEDIUM
                                else -> RiskLevel.LOW
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        RiskFactorRow(
                            icon = Icons.Default.Error,
                            label = "Tasa de errores",
                            value = "${(errorRate * 100).toInt()}%",
                            status = when {
                                errorRate >= 0.5f -> RiskLevel.CRITICAL
                                errorRate >= 0.25f -> RiskLevel.HIGH
                                errorRate >= 0.1f -> RiskLevel.MEDIUM
                                else -> RiskLevel.LOW
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        RiskFactorRow(
                            icon = Icons.Default.Speed,
                            label = "Envío rápido",
                            value = if (riskAssessment?.factors?.any { it.contains("Rapid") } == true) "Detectado" else "Normal",
                            status = if (riskAssessment?.factors?.any { it.contains("Rapid") } == true) RiskLevel.HIGH else RiskLevel.LOW
                        )
                    }
                }
            }

            item {
                Text("Recomendaciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(riskAssessment?.factors ?: emptyList()) { factor ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(factor, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { riskManager?.reset() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reiniciar Métricas")
                }
            }
        }
    }
}

@Composable
private fun RiskFactorRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    status: RiskLevel
) {
    val color = when (status) {
        RiskLevel.CRITICAL -> Color(0xFFEF4444)
        RiskLevel.HIGH -> Color(0xFFF59E0B)
        RiskLevel.MEDIUM -> Color(0xFF6366F1)
        RiskLevel.LOW -> Color(0xFF10B981)
    }

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
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.1f)).padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(value, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
