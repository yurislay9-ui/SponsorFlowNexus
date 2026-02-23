/*
 * SponsorFlow Nexus v1.0 - Application Class
 * CORREGIDO: @HiltWorker, scope con SupervisorJob, logSecurityEvent corregido
 */
package com.sponsorflow.nexus

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sponsorflow.nexus.config.DynamicConfigManager
import com.sponsorflow.nexus.security.IntegrityChecker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class NexusApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    private lateinit var encryptedPrefs: EncryptedSharedPreferences
    private lateinit var masterKey: MasterKey
    
    // CORREGIDO: Scope con SupervisorJob para poder cancelar
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        try {
            initEncryptedStorage()  // Inicializar ANTES de security
            initSecurity()
            initWorkManager()
            fetchRemoteConfig()
        } catch (e: Exception) {
            // Evitar crash si algo falla en inicialización
            android.util.Log.e("NexusApplication", "Error inicializando: ${e.message}")
        }
    }

    private fun initSecurity() {
        val integrity = IntegrityChecker()
        val report = integrity.runAllChecks(this)
        if (!report.passedAll) {
            logSecurityEvent(report.toString())
        }
    }

    private fun initEncryptedStorage() {
        masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        encryptedPrefs = EncryptedSharedPreferences.create(
            this,
            "nexus_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    private fun initWorkManager() {
        val configWork = PeriodicWorkRequestBuilder<ConfigSyncWorker>(
            6, TimeUnit.HOURS
        ).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "config_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            configWork
        )
    }

    // CORREGIDO: Usar scope con SupervisorJob
    private fun fetchRemoteConfig() {
        applicationScope.launch {
            DynamicConfigManager(this@NexusApplication).fetchConfig()
        }
    }
    
    // CORREGIDO: Usar clave fija, no crear infinitas claves
    private fun logSecurityEvent(event: String) {
        // Sobrescribir en lugar de crear nuevas claves
        encryptedPrefs.edit()
            .putString("last_security_event", event)
            .putLong("last_security_event_time", System.currentTimeMillis())
            .apply()
    }
    
    fun getEncryptedPrefs(): EncryptedSharedPreferences = encryptedPrefs
    fun getMasterKey(): MasterKey = masterKey
}

// CORREGIDO: @HiltWorker para inyección correcta
@HiltWorker
class ConfigSyncWorker(
    appContext: android.content.Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            DynamicConfigManager(applicationContext).fetchConfig()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
