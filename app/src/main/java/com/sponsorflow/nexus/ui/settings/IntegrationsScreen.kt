/*
 * SponsorFlow Nexus - Integrations Screen
 */
package com.sponsorflow.nexus.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sponsorflow.nexus.core.enums.SubscriptionTier
import com.sponsorflow.nexus.integration.UserWebhookManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationsScreen(
    onBack: () -> Unit = {},
    userTier: SubscriptionTier = SubscriptionTier.FREE
) {
    val context = LocalContext.current
    val viewModel: IntegrationsViewModel = viewModel {
        IntegrationsViewModel(UserWebhookManager(context.applicationContext))
    }
    val scope = rememberCoroutineScope()
    val isEmpresario = userTier == SubscriptionTier.EMPRESARIO
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Integraciones") },
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
        ) {
            if (!isEmpresario) {
                PlanLockedBanner()
            } else {
                IntegrationContent(viewModel, context, scope)
            }
        }
    }
}

@Composable
private fun IntegrationContent(
    viewModel: IntegrationsViewModel,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope
) {
    IntegrationInfoCard()
    
    OutlinedTextField(
        value = viewModel.webhookUrl,
        onValueChange = { viewModel.updateUrl(it) },
        label = { Text("URL del Webhook") },
        placeholder = { Text("https://hooks.zapier.com/hooks/catch/...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        singleLine = true,
        trailingIcon = {
            if (viewModel.webhookUrl.isNotBlank()) {
                IconButton(onClick = { viewModel.updateUrl("") }) {
                    Icon(Icons.Default.Clear, "Limpiar")
                }
            }
        }
    )
    
    Button(
        onClick = {
            viewModel.saveUrl()
            Toast.makeText(context, "URL guardada", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        enabled = viewModel.webhookUrl.isNotBlank()
    ) {
        Icon(Icons.Default.Save, null)
        Spacer(Modifier.width(8.dp))
        Text("Guardar URL")
    }
    
    OutlinedButton(
        onClick = {
            scope.launch {
                val success = viewModel.testWebhook()
                Toast.makeText(context, if (success) "Test exitoso" else "Test fallido", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        enabled = viewModel.webhookUrl.isNotBlank() && !viewModel.isLoading
    ) {
        if (viewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Default.Send, null)
        }
        Spacer(Modifier.width(8.dp))
        Text("Probar Webhook")
    }
    
    viewModel.lastTestResult?.let { result ->
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(result, modifier = Modifier.padding(16.dp))
        }
    }
    
    AvailableEventsList()
}