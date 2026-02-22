/*
 * SponsorFlow Nexus - Admin Control Manager
 */
package com.sponsorflow.nexus.admin

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.provider.Settings
import android.util.Log
import androidx.work.*
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

class AdminControlManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AdminControl"
        private const val PREFS_NAME = "admin_control"
        private const val KEY_LAST_ACTION = "last_user_action"
        private const val KEY_MODEL_DOWNLOADED = "model_downloaded"
        private var ADMIN_HEARTBEAT_URL = ""
        private var ADMIN_ERROR_URL = ""
        
        fun configure(webhooks: AdminWebhooks) {
            ADMIN_HEARTBEAT_URL = webhooks.heartbeatUrl
            ADMIN_ERROR_URL = webhooks.errorUrl
        }
        
        fun scheduleHeartbeat(context: Context, intervalHours: Long = 1) {
            val work = PeriodicWorkRequestBuilder<HeartbeatWorker>(intervalHours, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("admin_heartbeat", ExistingPeriodicWorkPolicy.KEEP, work)
        }
        
        fun getDeviceId(context: Context): String {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    fun recordUserAction(action: String) { prefs.edit().putString(KEY_LAST_ACTION, action).apply() }
    fun setModelDownloaded(downloaded: Boolean) { prefs.edit().putBoolean(KEY_MODEL_DOWNLOADED, downloaded).apply() }
    
    fun getDeviceStatus(): AdminHeartbeat = AdminHeartbeat(
        deviceId = getDeviceId(context),
        appVersion = "2.4.0",
        lastUserAction = prefs.getString(KEY_LAST_ACTION, "none") ?: "none",
        modelDownloaded = prefs.getBoolean(KEY_MODEL_DOWNLOADED, false),
        networkType = getNetworkType() ?: "unknown",
        batteryLevel = getBatteryLevel()
    )
    
    private fun getNetworkType(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return "none"
        val caps = cm.getNetworkCapabilities(network) ?: return "unknown"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "data"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
            else -> "other"
        }
    }
    
    private fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
    
    suspend fun sendHeartbeat(): Boolean {
        if (ADMIN_HEARTBEAT_URL.isBlank()) return false
        val status = getDeviceStatus()
        val json = JSONObject().apply {
            put("event_type", "admin_heartbeat")
            put("device_id", status.deviceId)
            put("app_version", status.appVersion)
            put("last_user_action", status.lastUserAction)
            put("model_downloaded", status.modelDownloaded)
            put("network_type", status.networkType)
            put("battery_level", status.batteryLevel)
            put("timestamp", System.currentTimeMillis())
        }
        return try { postJson(ADMIN_HEARTBEAT_URL, json.toString()); Log.d(TAG, "Heartbeat sent"); true }
        catch (e: Exception) { Log.e(TAG, "Heartbeat failed: ${e.message}"); false }
    }
    
    suspend fun reportError(errorType: String, message: String, stackTrace: String? = null) {
        if (ADMIN_ERROR_URL.isBlank()) return
        val json = JSONObject().apply {
            put("event_type", "admin_error_report")
            put("device_id", getDeviceId(context))
            put("error_type", errorType)
            put("message", message)
            put("stack_trace", stackTrace)
            put("app_version", "2.4.0")
            put("timestamp", System.currentTimeMillis())
        }
        try { postJson(ADMIN_ERROR_URL, json.toString()); Log.d(TAG, "Error report sent") }
        catch (e: Exception) { Log.e(TAG, "Error report failed: ${e.message}") }
    }
    
    fun executeCommand(command: AdminCommand): CommandResult {
        Log.d(TAG, "Executing command: ${command.type}")
        return when (command.type) {
            "WIPE_DATA" -> try {
                context.cacheDir.deleteRecursively()
                context.deleteDatabase("nexus_database")
                prefs.edit().clear().apply()
                CommandResult(success = true, message = "Datos eliminados")
            } catch (e: Exception) { CommandResult(success = false, message = e.message ?: "Error desconocido") }
            "FORCE_LOGOUT" -> try {
                context.getSharedPreferences("nexus_session", Context.MODE_PRIVATE).edit().clear().apply()
                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                CommandResult(success = true, message = "Logout forzado")
            } catch (e: Exception) { CommandResult(success = false, message = e.message ?: "Error desconocido") }
            "UPDATE_CONFIG" -> CommandResult(success = true, message = "Config actualizado")
            "SHOW_MESSAGE" -> CommandResult(success = true, message = command.payload ?: "Mensaje del admin")
            else -> CommandResult(success = false, message = "Comando desconocido: ${command.type}")
        }
    }
    
    fun isDeviceBanned(bannedDevices: List<String>): Boolean = bannedDevices.contains(getDeviceId(context))
    
    private fun postJson(urlString: String, json: String): String {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpsURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("User-Agent", "SponsorFlowNexus/2.4")
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
        val responseCode = conn.responseCode
        return if (responseCode in 200..299) conn.inputStream.bufferedReader().readText() else throw Exception("HTTP $responseCode")
    }
}