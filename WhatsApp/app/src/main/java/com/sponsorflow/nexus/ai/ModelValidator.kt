/*
 * SponsorFlow Nexus v1.0 - GGUF Model Validator
 * CORREGIDO: Magic number little-endian, tamaño mínimo aumentado
 */
package com.sponsorflow.nexus.ai

import java.io.File
import java.io.RandomAccessFile
import android.util.Log

object ModelValidator {
    
    // Magic number de archivos GGUF: "GGUF" en little-endian (como se almacena en archivo)
    // GGUF en ASCII = 0x47= G, 0x47 = G, 0x55 = U, 0x46 = F
    // En little-endian (lectura directa): 0x47475546
    // En big-endian (como Java lee por defecto): 0x46554747
    private const val GGUF_MAGIC_LE = 0x47475546 // GGUF en little-endian
    private const val MIN_MODEL_SIZE = 10 * 1024 * 1024L // Mínimo 10MB para modelo real
    
    // Verificar si archivo es GGUF válido
    fun isValidGGUF(file: File): Boolean {
        if (!file.exists()) return false
        if (file.length() < MIN_MODEL_SIZE) {
            Log.w("Nexus", "Archivo demasiado pequeño: ${file.length()} bytes")
            return false
        }
        
        return try {
            RandomAccessFile(file, "r").use { raf ->
                // Leer magic number (4 bytes) - Java readInt() usa big-endian
                // Convertir de big-endian a little-endian para comparar
                val magicBigEndian = raf.readInt()
                val magic = Integer.reverseBytes(magicBigEndian)
                
                if (magic != GGUF_MAGIC_LE) {
                    Log.e("Nexus", "Magic number inválido: 0x${Integer.toHexString(magic)}, esperado: 0x${Integer.toHexString(GGUF_MAGIC_LE)}")
                    return false
                }
                
                // Leer versión (4 bytes) - también hay que convertir
                val versionBigEndian = raf.readInt()
                val version = Integer.reverseBytes(versionBigEndian)
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