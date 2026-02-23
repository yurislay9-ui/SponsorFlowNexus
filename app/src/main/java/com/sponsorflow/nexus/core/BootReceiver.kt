/*
 * SponsorFlow Nexus v2.4 - Boot Receiver
 * CORREGIDO: Protecciones adicionales contra abuso
 */
package com.sponsorflow.nexus.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Validar que el intent no sea nulo
        if (intent == null) {
            Log.w(TAG, "Intent nulo recibido")
            return
        }

        // Validar la acción
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && 
            action != "android.intent.action.QUICKBOOT_POWERON") {
            Log.d(TAG, "Acción ignorada: $action")
            return
        }

        // Verificar permisos
        if (checkPermission(context)) {
            startService(context)
        } else {
            Log.w(TAG, "Permisos insuficientes")
        }
    }

    private fun checkPermission(context: Context): Boolean {
        return context.checkCallingOrSelfPermission(
            android.Manifest.permission.RECEIVE_BOOT_COMPLETED
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun startService(context: Context) {
        try {
            val serviceIntent = Intent(context, NexusForegroundService::class.java).apply {
                action = NexusForegroundService.ACTION_START
            }
            context.startForegroundService(serviceIntent)
            Log.i(TAG, "Servicio iniciado después del boot")
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando servicio: ${e.message}")
        }
    }
}
