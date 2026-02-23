/*
 * SponsorFlow Nexus v1.0 - TxHash Registry (Anti Double Spend)
 * CORREGIDO: Operación atómica useIfNew, fix cleanOldRecords
 */
package com.sponsorflow.nexus.subscription

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TxHashRegistry {
    
    private const val PREFS_NAME = "tx_registry"
    private var prefs: android.content.SharedPreferences? = null
    private val lock = Any()
    
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
    
    // CORREGIDO: Operación atómica con synchronized
    fun useIfNew(txHash: String): Boolean {
        synchronized(lock) {
            val existing = isUsed(txHash)
            if (existing) {
                return false
            }
            markUsedWithTimestamp(txHash)
            return true
        }
    }
    
    // CORREGIDO: Fix cleanOldRecords - copiar keys antes de iterar
    fun cleanOldRecords() {
        val cutoff = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        
        // Copiar keys antes de iterar para evitar ConcurrentModificationException
        val timeKeys: List<String> = prefs?.all?.keys?.filter { it.startsWith("tx_time_") } ?: emptyList()
        
        if (timeKeys.isEmpty()) return
        
        val editor = prefs?.edit()
        timeKeys.forEach { key ->
            val time = prefs?.getLong(key, 0) ?: 0
            if (time < cutoff && time > 0) {
                val txKey = key.removePrefix("tx_time_")
                editor?.remove(key)?.remove(txKey)
            }
        }
        editor?.apply()
    }
}
