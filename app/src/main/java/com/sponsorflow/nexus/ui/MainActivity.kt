/*
 * SponsorFlow Nexus v2.4 - Main Activity
 * CORREGIDO: Version actualizada a v2.4
 */
package com.sponsorflow.nexus.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sponsorflow.nexus.core.NexusForegroundService
import com.sponsorflow.nexus.ui.theme.NexusTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startServiceIfNeeded()
        setContent {
            NexusTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NexusNavHost(navController = navController)
                }
            }
        }
    }

    private fun startServiceIfNeeded() {
        val intent = Intent(this, NexusForegroundService::class.java).apply {
            action = NexusForegroundService.ACTION_START
        }
        startForegroundService(intent)
    }
}
