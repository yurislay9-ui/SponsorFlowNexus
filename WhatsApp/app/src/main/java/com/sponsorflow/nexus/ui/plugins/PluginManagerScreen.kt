/*
 * SponsorFlow Nexus - Plugin Manager Screen
 * Pantalla para gestionar plugins (Plan Empresario)
 */
package com.sponsorflow.nexus.ui.plugins

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sponsorflow.nexus.plugin.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@HiltViewModel
class PluginManagerViewModel @Inject constructor() : ViewModel() {
    private val _plugins = mutableStateListOf<PluginInfo>()
    val plugins: List<PluginInfo> get() = _plugins.toList()

    private val _enabledStates = mutableStateMapOf<String, Boolean>()
    fun isEnabled(id: String) = _enabledStates[id] ?: true

    init {
        loadPlugins()
    }

    private fun loadPlugins() {
        // Plugins de ejemplo preinstalados
        _plugins.addAll(listOf(
            PluginInfo("auto_reply", "Auto Respuesta", "1.0.0", PluginType.ACTION, "Nexus", "Respuestas automáticas personalizadas"),
            PluginInfo("sentiment", "Análisis Sentimiento", "1.0.0", PluginType.ANALYZER, "Nexus", "Analiza emociones en mensajes"),
            PluginInfo("whatsapp_api", "WhatsApp API", "1.0.0", PluginType.INTEGRATION, "Nexus", "Integración con WhatsApp Business"),
            PluginInfo("translator", "Traductor", "1.0.0", PluginType.RESPONSE, "Nexus", "Traduce mensajes automáticamente")
        ))
        _plugins.forEach { _enabledStates[it.id] = it.enabled }
    }

    fun togglePlugin(id: String) {
        _enabledStates[id] = !(_enabledStates[id] ?: true)
    }

    fun getPluginCount(): Int = _plugins.size
    fun getEnabledCount(): Int = _enabledStates.values.count { it }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginManagerScreen(
    viewModel: PluginManagerViewModel = viewModel(),
    onBack: () -> Unit = {},
    hasSDK: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Plugins") },
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
        ) {
            // Header stats
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Total", viewModel.getPluginCount().toString())
                    StatItem("Activos", viewModel.getEnabledCount().toString())
                }
            }

            // SDK Banner para Empresario
            if (hasSDK) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Code, "SDK", tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("SDK de Plugins", style = MaterialTheme.typography.titleMedium)
                            Text("Crea tus propios plugins", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Plugin list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.plugins) { plugin ->
                    PluginCard(
                        plugin = plugin,
                        isEnabled = viewModel.isEnabled(plugin.id),
                        onToggle = { viewModel.togglePlugin(plugin.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PluginCard(
    plugin: PluginInfo,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = when (plugin.type) {
                    PluginType.ACTION -> Icons.Default.PlayArrow
                    PluginType.ANALYZER -> Icons.Default.Analytics
                    PluginType.INTEGRATION -> Icons.Default.Link
                    PluginType.RESPONSE -> Icons.AutoMirrored.Filled.Message
                },
                contentDescription = null,
                tint = if (isEnabled) MaterialTheme.colorScheme.primary else Color.Gray
            )

            Spacer(Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(plugin.name, style = MaterialTheme.typography.titleMedium)
                Text(plugin.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(
                    "v${plugin.version} • ${plugin.type.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Toggle
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}