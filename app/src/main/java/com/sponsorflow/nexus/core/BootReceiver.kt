/*
 * SponsorFlow Nexus v2.3 - Boot Receiver
 */
package com.sponsorflow.nexus.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, NexusForegroundService::class.java).apply {
                action = NexusForegroundService.ACTION_START
            }
            context.startForegroundService(serviceIntent)
        }
    }
}