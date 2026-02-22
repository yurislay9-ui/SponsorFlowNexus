/*
 * SponsorFlow Nexus - Plugin Manager Activity
 */
package com.sponsorflow.nexus.ui.plugins

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sponsorflow.nexus.ui.theme.NexusTheme

class PluginManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val hasSDK = intent.getBooleanExtra("has_sdk", false)
        
        setContent {
            NexusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PluginManagerScreen(
                        onBack = { finish() },
                        hasSDK = hasSDK
                    )
                }
            }
        }
    }
}