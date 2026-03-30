package com.sponsorflow.nexus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
private val AccentCyan = Color(0xFF06B6D4)
private val AccentOrange = Color(0xFFF59E0B)

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
                        text = if (isActive) "Activo" else "Inactivo",
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
                        title = "Tasa Éxito",
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

            // Anti-Detection Section
            item {
                Text(
                    text = "Anti-Detección",
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
                        text = "Cola Inteligente",
                        icon = Icons.Default.List,
                        color = AccentCyan,
                        onClick = onNavigateToSmartQueue,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        text = "Comportamiento",
                        icon = Icons.Default.Psychology,
                        color = AccentPurple,
                        onClick = { /* Navigate to human behavior */ },
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
                        text = "Ventana 24h",
                        icon = Icons.Default.Schedule,
                        color = AccentBlue,
                        onClick = { /* Navigate to 24h window */ },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        text = "Detección Bloqueo",
                        icon = Icons.Default.Shield,
                        color = AccentOrange,
                        onClick = onNavigateToBanDetection,
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
                        text = "Nivel de Riesgo",
                        icon = Icons.Default.Warning,
                        color = AccentRed,
                        onClick = onNavigateToRiskLevel,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        text = "Riesgo",
                        icon = Icons.Default.TrendingUp,
                        color = AccentGreen,
                        onClick = onNavigateToRiskLevel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Actions Section
            item {
                Text(
                    text = "Acciones Rápidas",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        text = "Inventario",
                        icon = Icons.Default.Inventory,
                        color = AccentBlue,
                        onClick = onNavigateToInventory,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        text = "Estadísticas",
                        icon = Icons.Default.Analytics,
                        color = AccentPurple,
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
                        text = "Plugins",
                        icon = Icons.Default.Extension,
                        color = AccentCyan,
                        onClick = onNavigateToPlugins,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        text = "Configuración",
                        icon = Icons.Default.Settings,
                        color = TextSecondary,
                        onClick = onNavigateToSettings,
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
        Column(modifier = Modifier.padding(16.dp)) {
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
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
