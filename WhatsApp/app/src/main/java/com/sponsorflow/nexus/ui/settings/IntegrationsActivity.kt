/*
 * SponsorFlow Nexus - Integrations Activity
 */
package com.sponsorflow.nexus.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sponsorflow.nexus.core.enums.SubscriptionTier
import com.sponsorflow.nexus.ui.theme.NexusTheme

class IntegrationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Obtener tier del usuario (por defecto FREE)
        val userTier = getUserTier()
        
        setContent {
            NexusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IntegrationsScreen(
                        userTier = userTier,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
    
    private fun getUserTier(): SubscriptionTier {
        val prefs = getSharedPreferences("nexus_session", MODE_PRIVATE)
        val tierName = prefs.getString("subscription_tier", "FREE") ?: "FREE"
        return SubscriptionTier.fromName(tierName)
    }
}