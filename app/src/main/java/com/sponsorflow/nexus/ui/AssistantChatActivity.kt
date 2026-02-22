/*
 * SponsorFlow Nexus - Assistant Chat Activity
 * Activity que hospeda la pantalla de chat con IA
 */
package com.sponsorflow.nexus.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sponsorflow.nexus.ui.theme.NexusTheme

class AssistantChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AssistantChatScreen(
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}