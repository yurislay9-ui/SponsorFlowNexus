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
import androidx.compose.material.icons.automirrored.filled.Send
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
            if (!isEmpresario) PlanLockedBanner() else IntegrationContent(viewModel, context, scope)
        }
    }
}