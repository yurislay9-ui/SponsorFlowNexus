/*
 * SponsorFlow Nexus - Channel Configuration Screen
 * Dashboard para configurar canales multi-mensajería
 */
package com.sponsorflow.nexus.ui.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.sponsorflow.nexus.channels.Channel
import com.sponsorflow.nexus.channels.ChannelConfig
import com.sponsorflow.nexus.channels.MultiChannelManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelConfigScreen(
    onBack: () -> Unit,
    channelManager: MultiChannelManager
) {
    var channels by remember { mutableStateOf<List<ChannelConfig>>(emptyList()) }
    var installedApps by remember { mutableStateOf<List<Channel>>(emptyList()) }

    LaunchedEffect(Unit) {
        channels = Channel.entries.map { channel ->
            channelManager.getChannelConfig(channel) ?: ChannelConfig(
                channel = channel,
                isEnabled = false,
                isNotificationAccessEnabled = false,
                isAccessibilityEnabled = false,
                isDefault = false
            )
        }
        installedApps = channelManager.getInstalledChannels()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💬 Canales de Mensajería") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Configura tus canales de mensajería",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Activa los canales que deseas usar. Cada uno requerirá permisos de Notification Access y Accessibility.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Canales instalados
            val availableChannels = Channel.entries.filter { it in installedApps }

            if (availableChannels.isNotEmpty()) {
                item {
                    Text(
                        "📱 Canales Disponibles",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                items(availableChannels) { channel ->
                    val config = channels.find { it.channel == channel } 
                        ?: ChannelConfig(channel = channel)
                    ChannelCard(
                        config = config,
                        isInstalled = true,
                        onToggle = { enabled ->
                            if (enabled) {
                                channelManager.enableChannel(channel)
                            } else {
                                channelManager.disableChannel(channel)
                            }
                            channels = channels.map {
                                if (it.channel == channel) it.copy(isEnabled = enabled) else it
                            }
                        },
                        onSetDefault = {
                            channelManager.setDefaultChannel(channel)
                            channels = channels.map {
                                it.copy(isDefault = it.channel == channel)
                            }
                        }
                    )
                }
            }

            // Canales no instalados
            val unavailableChannels = Channel.entries.filter { it !in installedApps }

            if (unavailableChannels.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "❌ Canales No Instalados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                items(unavailableChannels) { channel ->
                    val config = channels.find { it.channel == channel }
                        ?: ChannelConfig(channel = channel)
                    ChannelCard(
                        config = config,
                        isInstalled = false,
                        onToggle = { },
                        onSetDefault = { }
                    )
                }
            }

            // Permisos
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "⚙️ Permisos Requeridos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PermissionItem(
                            icon = Icons.Default.Notifications,
                            title = "Notification Access",
                            description = "Lee mensajes entrantes",
                            isRequired = true
                        )
                        Divider(Modifier.padding(vertical = 8.dp))
                        PermissionItem(
                            icon = Icons.Default.Accessibility,
                            title = "Accessibility Service",
                            description = "Escribe y envía mensajes",
                            isRequired = true
                        )
                    }
                }
            }

            // Info extra
            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE65100)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Ve a Configuración > Accessibility para habilitar los permisos",
                            fontSize = 12.sp,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelCard(
    config: ChannelConfig,
    isInstalled: Boolean,
    onToggle: (Boolean) -> Unit,
    onSetDefault: () -> Unit
) {
    val channel = config.channel

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isInstalled) Modifier.alpha(0.5f) else Modifier
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icono del canal
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (channel) {
                                    Channel.WHATSAPP -> Color(0xFF25D366)
                                    Channel.MESSENGER -> Color(0xFF0084FF)
                                    Channel.INSTAGRAM -> Color(0xFFE4405F)
                                    Channel.TELEGRAM -> Color(0xFF0088CC)
                                    Channel.DISCORD -> Color(0xFF5865F2)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            channel.icon,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            channel.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (!isInstalled) {
                            Text(
                                "No instalado",
                                fontSize = 12.sp,
                                color = Color.Red
                            )
                        }
                    }
                }

                // Toggle
                if (isInstalled) {
                    Switch(
                        checked = config.isEnabled,
                        onCheckedChange = onToggle
                    )
                }
            }

            // Opciones adicionales si está habilitado
            if (config.isEnabled && isInstalled) {
                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                // Estado de permisos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PermissionStatus(
                        label = "Notificaciones",
                        enabled = config.isNotificationAccessEnabled
                    )
                    PermissionStatus(
                        label = "Accesibilidad",
                        enabled = config.isAccessibilityEnabled
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Default channel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Canal por defecto",
                        fontSize = 14.sp
                    )
                    RadioButton(
                        selected = config.isDefault,
                        onClick = onSetDefault
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionStatus(label: String, enabled: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (enabled) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (enabled) Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = if (enabled) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

@Composable
fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isRequired: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Medium)
                if (isRequired) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "*",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(description, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
