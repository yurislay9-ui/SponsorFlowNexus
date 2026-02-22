/*
 * SponsorFlow Nexus v2.3 - Secure Config Manager
 */
package com.sponsorflow.nexus.config

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureConfigManager {

    private const val PREFS_NAME = "secure_config"
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

    // Guardar config de forma segura
    fun saveConfig(key: String, value: String) {
        prefs?.edit()?.putString("cfg_$key", value)?.apply()
    }

    // Leer config
    fun getConfig(key: String, default: String = ""): String {
        return prefs?.getString("cfg_$key", default) ?: default
    }

    // Guardar licencia
    fun saveLicense(tier: String, expiresAt: Long) {
        prefs?.edit()
            ?.putString("license_tier", tier)
            ?.putLong("license_expires", expiresAt)
            ?.apply()
    }

    // Obtener tier de licencia
    fun getLicenseTier(): String {
        return prefs?.getString("license_tier", "FREE") ?: "FREE"
    }

    // Verificar si licencia válida
    fun isLicenseValid(): Boolean {
        val expires = prefs?.getLong("license_expires", 0) ?: 0
        return expires > System.currentTimeMillis()
    }
}