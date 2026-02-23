/*
 * SponsorFlow Nexus v1.0 - Navigation Host
 */
package com.sponsorflow.nexus.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sponsorflow.nexus.R
import com.sponsorflow.nexus.ui.inventory.InventoryManagementScreen
import com.sponsorflow.nexus.ui.settings.IntegrationsScreen
import com.sponsorflow.nexus.ui.subscription.SubscriptionScreen

@Composable
fun NexusNavHost(
    navController: NavHostController,
    startDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToInventory = { navController.navigate("inventory") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("inventory") {
            InventoryManagementScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            IntegrationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("chat") {
            AssistantChatScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToInventory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    var isActive by remember { mutableStateOf(false) }
    var showSubscription by remember { mutableStateOf(false) }
    
    if (showSubscription) {
        SubscriptionScreen(
            onBack = { showSubscription = false },
            onSubscribe = { plan ->
                Toast.makeText(context, "Suscripción a $plan iniciada", Toast.LENGTH_SHORT).show()
                showSubscription = false
            }
        )
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "SponsorFlow Nexus",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Configuración", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logo/Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tu Asistente de WhatsApp",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Potenciado con IA",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) 
                            Color(0xFFE8F5E9) 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) Color(0xFF4CAF50)
                                    else Color.Gray
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isActive) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isActive) "Servicio Activo" else "Servicio Inactivo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = if (isActive) "Escuchando mensajes de WhatsApp" else "Toca para activar el asistente",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Main Button
                Button(
                    onClick = { 
                        isActive = !isActive
                        Toast.makeText(
                            context, 
                            if (isActive) "¡Asistente activado!" else "Asistente desactivado",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isActive) "DETENER ASISTENTE" else "ACTIVAR ASISTENTE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quick Actions
                Text(
                    text = "Acciones Rápidas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Chat,
                        label = "Chat IA",
                        color = Color(0xFF2196F3),
                        onClick = {
                            context.startActivity(
                                Intent(context, AssistantChatActivity::class.java)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.Inventory,
                        label = "Inventario",
                        color = Color(0xFFFF9800),
                        onClick = onNavigateToInventory,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Extension,
                        label = "Plugins",
                        color = Color(0xFF9C27B0),
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.Analytics,
                        label = "Estadísticas",
                        color = Color(0xFF4CAF50),
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Subscription Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Plan Actual: GRATIS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Actualiza para desbloquear todas las funciones",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showSubscription = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ver Planes de Suscripción")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stats Cards
                Text(
                    text = "Estadísticas de Hoy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Mensajes",
                        value = "0",
                        icon = Icons.Default.Message,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Respuestas",
                        value = "0",
                        icon = Icons.Default.Reply,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
