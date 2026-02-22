/*
 * SponsorFlow Nexus v2.3 - TxHash Registry (Anti Double Spend)
 */
package com.sponsorflow.nexus.subscription

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TxHashRegistry {
    
    private const val PREFS_NAME = "tx_registry"
    private var prefs: android.content.SharedPreferences? = null
    
    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    // Verificar si el hash ya fue usado
    fun isUsed(txHash: String): Boolean {
        return prefs?.getBoolean("tx_$txHash", false) ?: false
    }
    
    // Marcar hash como usado
    fun markUsed(txHash: String) {
        prefs?.edit()?.putBoolean("tx_$txHash", true)?.apply()
    }
    
    // Obtener timestamp de uso
    fun getUsedAt(txHash: String): Long {
        return prefs?.getLong("tx_time_$txHash", 0) ?: 0
    }
    
    // Marcar con timestamp
    fun markUsedWithTimestamp(txHash: String) {
        prefs?.edit()
            ?.putBoolean("tx_$txHash", true)
            ?.putLong("tx_time_$txHash", System.currentTimeMillis())
            ?.apply()
    }
    
    // Verificar y marcar atómicamente
    fun useIfNew(txHash: String): Boolean {
        if (isUsed(txHash)) return false
        markUsedWithTimestamp(txHash)
        return true
    }
    
    // Limpiar registros antiguos (más de 30 días)
    fun cleanOldRecords() {
        val cutoff = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        val editor = prefs?.edit()
        prefs?.all?.keys?.filter { it.startsWith("tx_time_") }?.forEach { key ->
            val time = prefs?.getLong(key, 0) ?: 0
            if (time < cutoff) {
                val txKey = key.replace("tx_time_", "tx_")
                editor?.remove(key)?.remove(txKey)
            }
        }
        editor?.apply()
    }
}