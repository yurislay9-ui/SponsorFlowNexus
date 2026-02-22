/*
 * SponsorFlow Nexus v2.3 - Foreground Service
 */
package com.sponsorflow.nexus.core

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sponsorflow.nexus.R
import com.sponsorflow.nexus.core.enums.OperationStatus

class NexusForegroundService : Service() {

    private var operationStatus: OperationStatus = OperationStatus.IDLE

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startOperation()
            ACTION_STOP -> stopOperation()
            ACTION_PAUSE -> pauseOperation()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startOperation() {
        operationStatus = OperationStatus.RUNNING
        updateNotification()
    }

    private fun stopOperation() {
        operationStatus = OperationStatus.IDLE
        updateNotification()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun pauseOperation() {
        operationStatus = OperationStatus.PAUSED
        updateNotification()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: Intent()
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_name))
            .setContentText(operationStatus.getDescription())
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification() {
        getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, createNotification())
    }

    companion object {
        const val ACTION_START = "com.sponsorflow.nexus.START"
        const val ACTION_STOP = "com.sponsorflow.nexus.STOP"
        const val ACTION_PAUSE = "com.sponsorflow.nexus.PAUSE"
        private const val CHANNEL_ID = "nexus_service_channel"
        private const val NOTIFICATION_ID = 1001
    }
}