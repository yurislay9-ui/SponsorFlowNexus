/*
 * SponsorFlow Nexus v1.0 - Logout Manager (Factory Reset)
 */
package com.sponsorflow.nexus.account

import android.content.Context
import java.io.File

object LogoutManager {
    
    // Logout completo - Factory Reset
    suspend fun fullLogout(
        context: Context,
        sessionManager: SessionManager
    ): LogoutResult {
        return try {
            // 1. Limpiar sesión
            sessionManager.clearSession()
            
            // 2. Limpiar nonces
            NonceGenerator.clear()
            
            // 3. Limpiar caché de la app
            clearAppCache(context)
            
            // 4. Limpiar base de datos
            clearDatabase(context)
            
            // 5. Limpiar archivos temporales
            clearTempFiles(context)
            
            // 6. Limpiar modelos descargados
            clearDownloadedModels(context)
            
            // 7. Limpiar SharedPreferences adicionales
            clearAllPreferences(context)
            
            LogoutResult.Success
        } catch (e: Exception) {
            LogoutResult.Partial(e.message ?: "Error desconocido")
        }
    }
    
    // Limpiar caché de la app
    private fun clearAppCache(context: Context) {
        val cacheDir = context.cacheDir
        if (cacheDir.exists()) {
            deleteRecursive(cacheDir)
        }
        
        // Limpiar caché externa si existe
        context.externalCacheDir?.let {
            if (it.exists()) deleteRecursive(it)
        }
    }
    
    // Limpiar base de datos
    private fun clearDatabase(context: Context) {
        val databasesDir = File(context.applicationInfo.dataDir, "databases")
        if (databasesDir.exists()) {
            databasesDir.listFiles()?.forEach { it.delete() }
        }
        
        // Limpiar archivos de Room
        val roomDir = File(context.applicationInfo.dataDir, "databases")
        roomDir.listFiles()?.filter { 
            it.name.endsWith("-shm") || it.name.endsWith("-wal") 
        }?.forEach { it.delete() }
    }
    
    // Limpiar archivos temporales
    private fun clearTempFiles(context: Context) {
        val filesDir = context.filesDir
        filesDir.listFiles()?.filter { 
            it.name.endsWith(".tmp") || 
            it.name.endsWith(".temp") ||
            it.name.contains("temp_")
        }?.forEach { it.delete() }
    }
    
    // Limpiar modelos descargados
    private fun clearDownloadedModels(context: Context) {
        val modelsDir = File(context.filesDir, "models")
        if (modelsDir.exists()) {
            deleteRecursive(modelsDir)
        }
        
        // Limpiar descargas parciales
        val downloadsDir = File(context.filesDir, "downloads")
        if (downloadsDir.exists()) {
            downloadsDir.listFiles()?.filter {
                it.name.endsWith(".downloading")
            }?.forEach { it.delete() }
        }
    }
    
    // Limpiar todas las preferencias
    private fun clearAllPreferences(context: Context) {
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        if (prefsDir.exists()) {
            prefsDir.listFiles()?.forEach { file ->
                // Preservar device_id si es necesario
                if (!file.name.contains("device")) {
                    file.delete()
                }
            }
        }
    }
    
    // Eliminar directorio recursivamente
    private fun deleteRecursive(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursive(it) }
        }
        file.delete()
    }
    
    // Logout rápido (solo sesión)
    fun quickLogout(sessionManager: SessionManager) {
        sessionManager.clearSession()
        NonceGenerator.clear()
    }
}

sealed class LogoutResult {
    object Success : LogoutResult()
    data class Partial(val error: String) : LogoutResult()
    data class Failed(val error: String) : LogoutResult()
}