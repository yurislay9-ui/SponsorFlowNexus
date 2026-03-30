package com.sponsorflow.nexus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores del tema
private val BackgroundDark = Color(0xFF0F172A)
private val SurfaceCard = Color(0xFF1E293B)
private val TextPrimary = Color.White
private val TextSecondary = Color(0xFF94A3B8)
private val AccentGreen = Color(0xFF4CAF50)
private val AccentRed = Color(0xFFEF4444)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentPurple = Color(0xFF8B5CF6)

@Composable
fun HomeScreen(
    onNavigateToInventory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPlugins: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToSmartQueue: () -> Unit = {},
    onNavigateToRiskLevel: () -> Unit = {},
    onNavigateToBanDetection: () -> Unit = {}
) {
    var isActive by remember { mutableStateOf(false) }

    // Datos de ejemplo para el dashboard
    var totalSent by remember { mutableStateOf(0) }
    var totalFailed by remember { mutableStateOf(0) }
    var dailyCount by remember { mutableStateOf(0) }

    val successRate = if (totalSent + totalFailed > 0) {
        (totalSent.toFloat() / (totalSent + totalFailed) * 100)
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dashboard",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (isActive) "Active" else "Idle",
                        color = if (isActive) AccentGreen else TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            // Stats Cards Row 1
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Enviados",
                        value = totalSent.toString(),
                        valueColor = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Fallidos",
                        value = totalFailed.toString(),
                        valueColor = AccentRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Stats Cards Row 2
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Tasa de Éxito",
                        value = String.format("%.1f%%", successRate),
                        valueColor = AccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Mensajes Hoy",
                        value = dailyCount.toString(),
                        valueColor = AccentBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Service Toggle Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isActive) "Servicio Activo" else "Servicio Inactivo",
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentGreen,
                                checkedTrackColor = AccentGreen.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // Quick Actions
            item {
                Text(
                    text = "Acciones Rápidas",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        text = "Inventario",
                        onClick = onNavigateToInventory,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        text = "Stats",
                        onClick = onNavigateToAnalytics,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        text = "Cola",
                        onClick = onNavigateToSmartQueue,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        text = "Riesgo",
                        onClick = onNavigateToRiskLevel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Activity Section
            item {
                Text(
                    text = "Actividad Reciente",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Placeholder for recent activity
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sin actividad reciente",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentPurple
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
