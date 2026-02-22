/*
 * SponsorFlow Nexus v2.3 - GGUF Model Validator
 */
package com.sponsorflow.nexus.ai

import java.io.File
import java.io.RandomAccessFile
import android.util.Log

object ModelValidator {
    
    // Magic number de archivos GGUF: "GGUF" en ASCII
    private const val GGUF_MAGIC = 0x46554747 // "GGUF" little-endian
    private const val MIN_MODEL_SIZE = 1024L // Mínimo 1KB
    
    // Verificar si archivo es GGUF válido
    fun isValidGGUF(file: File): Boolean {
        if (!file.exists()) return false
        if (file.length() < MIN_MODEL_SIZE) return false
        
        return try {
            RandomAccessFile(file, "r").use { raf ->
                // Leer magic number (4 bytes)
                val magic = raf.readInt()
                
                if (magic != GGUF_MAGIC) {
                    Log.e("Nexus", "Magic number inválido: 0x${magic.toString(16)}")
                    return false
                }
                
                // Leer versión (4 bytes)
                val version = raf.readInt()
                Log.d("Nexus", "GGUF versión: $version")
                
                // Verificar versión soportada (3 o superior)
                version >= 3
            }
        } catch (e: Exception) {
            Log.e("Nexus", "Error validando modelo: ${e.message}")
            false
        }
    }
    
    // Verificar integridad básica
    fun quickCheck(file: File): ModelCheckResult {
        if (!file.exists()) {
            return ModelCheckResult.NotFound
        }
        if (file.length() < MIN_MODEL_SIZE) {
            return ModelCheckResult.TooSmall
        }
        if (!isValidGGUF(file)) {
            return ModelCheckResult.InvalidHeader
        }
        return ModelCheckResult.Valid
    }
}

sealed class ModelCheckResult {
    object Valid : ModelCheckResult()
    object NotFound : ModelCheckResult()
    object TooSmall : ModelCheckResult()
    object InvalidHeader : ModelCheckResult()
}