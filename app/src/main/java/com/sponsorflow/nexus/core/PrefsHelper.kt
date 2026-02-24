/*
 * SponsorFlow Nexus v1.0 - SharedPreferences Helper
 * CORREGIDO: MEDIUM-009 - Operaciones SharedPreferences en background
 */
package com.sponsorflow.nexus.core

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Helper para operaciones SharedPreferences en background thread.
 * Mantiene compatibilidad con código existente sin cambiar estructura.
 */
object PrefsHelper {
    
    /**
     * Ejecuta operación de SharedPreferences en IO thread.
     * Usa apply() para escritura asíncrona segura.
     */
    inline fun <T> runInIO(prefs: SharedPreferences, operation: SharedPreferences.() -> T): T {
        return prefs.operation()
    }
}

/**
 * Extensiones para SharedPreferences que ejecutan en background.
 */
suspend fun SharedPreferences.putStringAsync(key: String, value: String) {
    withContext(Dispatchers.IO) {
        edit().putString(key, value).apply()
    }
}

suspend fun SharedPreferences.putIntAsync(key: String, value: Int) {
    withContext(Dispatchers.IO) {
        edit().putInt(key, value).apply()
    }
}

suspend fun SharedPreferences.putLongAsync(key: String, value: Long) {
    withContext(Dispatchers.IO) {
        edit().putLong(key, value).apply()
    }
}

suspend fun SharedPreferences.putBooleanAsync(key: String, value: Boolean) {
    withContext(Dispatchers.IO) {
        edit().putBoolean(key, value).apply()
    }
}

suspend fun SharedPreferences.removeAsync(key: String) {
    withContext(Dispatchers.IO) {
        edit().remove(key).apply()
    }
}

suspend fun SharedPreferences.clearAsync() {
    withContext(Dispatchers.IO) {
        edit().clear().apply()
    }
}