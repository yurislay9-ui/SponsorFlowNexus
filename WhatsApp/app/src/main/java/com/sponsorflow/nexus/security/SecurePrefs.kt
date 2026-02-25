/**
 * SponsorFlow Nexus v1.0 - Secure Preferences
 * 
 * Sistema de preferencias encriptadas para almacenar datos sensibles
 * de forma segura utilizando EncryptedSharedPreferences de AndroidX Security.
 * 
 * Este componente proporciona una capa de seguridad para datos sensibles
 * como tokens de licencia, credenciales de API, y configuraciones críticas
 * que no deben ser accesibles en caso de que el dispositivo sea rooteado.
 * 
 * @author SponsorFlow Nexus Team
 * @version 1.0
 * @since 1.0
 */
package com.sponsorflow.nexus.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sponsorflow.nexus.NexusLogger
import com.sponsorflow.nexus.R
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Clase que gestiona preferencias encriptadas de forma segura.
 * 
 * Proporciona métodos para almacenar y recuperar datos sensibles
 * utilizando el sistema de EncryptedSharedPreferences de AndroidX Security.
 * 
 * @property context Contexto de Android para acceder a recursos y almacenamiento
 * @property encryptedPrefs Preferencias encriptadas configuradas con MasterKey
 * 
 * @see EncryptedSharedPreferences
 * @see MasterKey
 */
class SecurePrefs private constructor(private val context: Context) {
    
    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            EncryptedSharedPreferences.create(
                context,
                context.getString(R.string.secure_prefs_name),
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: GeneralSecurityException) {
            NexusLogger.error("Failed to create encrypted preferences", e)
            throw SecurityException("Unable to create secure preferences", e)
        } catch (e: IOException) {
            NexusLogger.error("Failed to create encrypted preferences", e)
            throw SecurityException("Unable to create secure preferences", e)
        }
    }
    
    /**
     * Almacena un token de licencia de forma segura.
     * 
     * @param licenseKey Token de licencia a almacenar
     * @return true si se almacenó exitosamente, false en caso contrario
     */
    fun saveLicenseKey(licenseKey: String): Boolean {
        return try {
            encryptedPrefs.edit()
                .putString(context.getString(R.string.pref_license_key), licenseKey)
                .apply()
            NexusLogger.info("License key saved securely")
            true
        } catch (e: SecurityException) {
            NexusLogger.error("Failed to save license key", e)
            false
        } catch (e: Exception) {
            NexusLogger.error("Unexpected error saving license key", e)
            false
        }
    }
    
    /**
     * Recupera el token de licencia almacenado de forma segura.
     * 
     * @return Token de licencia si existe, null en caso contrario
     */
    fun getLicenseKey(): String? {
        return try {
            encryptedPrefs.getString(
                context.getString(R.string.pref_license_key), 
                null
            )
        } catch (e: SecurityException) {
            NexusLogger.error("Failed to retrieve license key", e)
            null
        } catch (e: Exception) {
            NexusLogger.error("Unexpected error retrieving license key", e)
            null
        }
    }
    
    /**
     * Elimina el token de licencia almacenado.
     * 
     * @return true si se eliminó exitosamente, false en caso contrario
     */
    fun clearLicenseKey(): Boolean {
        return try {
            encryptedPrefs.edit()
                .remove(context.getString(R.string.pref_license_key))
                .apply()
            NexusLogger.info("License key cleared securely")
            true
        } catch (e: SecurityException) {
            NexusLogger.error("Failed to clear license key", e)
            false
        } catch (e: Exception) {
            NexusLogger.error("Unexpected error clearing license key", e)
            false
        }
    }
    
    /**
     * Almacena un token de API de forma segura.
     * 
     * @param apiToken Token de API a almacenar
     * @return true si se almacenó exitosamente, false en caso contrario
     */
    fun saveApiToken(apiToken: String): Boolean {
        return try {
            encryptedPrefs.edit()
                .putString(context.getString(R.string.pref_api_token), apiToken)
                .apply()
            NexusLogger.info("API token saved securely")
            true
        } catch (e: SecurityException) {
            NexusLogger.error("Failed to save API token", e)
            false
        } catch (e: Exception) {
            NexusLogger.error("Unexpected error saving API token", e)
            false
        }
    }
    
    /**
     * Recupera el token de API almacenado de forma segura.
     * 
     * @return Token de API si existe, null en caso contrario
     */
    fun getApiToken(): String? {
        return try {
            encryptedPrefs.getString(
                context.getString(R.string.pref_api_token), 
                null
            )
        } catch (e: SecurityException) {
            NexusLogger.error("Failed to retrieve API token", e)
            null
        } catch (e: Exception) {
            NexusLogger.error("Unexpected error retrieving API token", e)
            null
        }
    }
    
    /**
     * Almacena credenciales de pago de forma segura.
     * 
     * @param paymentCredentials Credenciales de pago en formato JSON
     * @return true si se almacenaron exitosamente, false en caso contrario
     */
    fun savePaymentCredentials(paymentCredentials: String): Boolean {
        return try {
            encryptedPrefs.edit()
                .putString(context.getString(R.string.pref_payment_credentials), paymentCredentials)
                .apply()
            NexusLogger.info("Payment credentials saved securely")
            true
        } catch (e: SecurityException) {
            NexusLogger.error("Failed to save payment credentials", e)
            false
        } catch (e: Exception) {
            NexusLogger.error("Unexpected error saving payment credentials", e)
            false
        }
    }
    
    /**
     * Recupera las credenciales de pago almacenadas de forma segura.
     * 
     * @return Credenciales de pago en formato JSON si existen, null en caso contrario
     */
    fun getPaymentCredentials(): String? {
        return try {
            encryptedPrefs.getString(
                context.getString(R.string.pref_payment_credentials), 
                null
            )
        } catch (e: SecurityException) {
            NexusLogger.error("Failed to retrieve payment credentials", e)
            null
        } catch (e: Exception) {
            NexusLogger.error("Unexpected error retrieving payment credentials", e)
            null
        }
    }
    
    /**
     * Verifica si existen datos sensibles almacenados.
     * 
     * @return true si hay datos sensibles almacenados, false en caso contrario
     */
    fun hasSensitiveData(): Boolean {
        return try {
            encryptedPrefs.contains(context.getString(R.string.pref_license_key)) ||
            encryptedPrefs.contains(context.getString(R.string.pref_api_token)) ||
            encryptedPrefs.contains(context.getString(R.string.pref_payment_credentials))
        } catch (e: SecurityException) {
            NexusLogger.error("Failed to check for sensitive data", e)
            false
        } catch (e: Exception) {
            NexusLogger.error("Unexpected error checking sensitive data", e)
            false
        }
    }
    
    /**
     * Limpia todos los datos sensibles almacenados.
     * 
     * @return true si se limpiaron exitosamente, false en caso contrario
     */
    fun clearAllSensitiveData(): Boolean {
        return try {
            val editor = encryptedPrefs.edit()
            editor.clear()
            editor.apply()
            NexusLogger.info("All sensitive data cleared securely")
            true
        } catch (e: SecurityException) {
            NexusLogger.error("Failed to clear sensitive data", e)
            false
        } catch (e: Exception) {
            NexusLogger.error("Unexpected error clearing sensitive data", e)
            false
        }
    }
    
    /**
     * Compañero object para implementar el patrón Singleton.
     */
    companion object {
        @Volatile
        private var INSTANCE: SecurePrefs? = null
        
        /**
         * Obtiene una instancia singleton de SecurePrefs.
         * 
         * @param context Contexto de Android
         * @return Instancia singleton de SecurePrefs
         */
        fun getInstance(context: Context): SecurePrefs {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SecurePrefs(context.applicationContext)
                    .also { INSTANCE = it }
            }
        }
    }
}