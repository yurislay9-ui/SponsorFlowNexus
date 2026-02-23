/*
 * SponsorFlow Nexus v1.0 - File Validator
 */
package com.sponsorflow.nexus.ai.download

import java.io.File

class FileValidator {
    
    // Sufijo para archivos en descarga
    private val tempSuffix = ".downloading"
    
    // Verificar si archivo es válido
    fun isValid(file: File, expectedSizeMB: Int = 0): Boolean {
        if (!file.exists()) return false
        if (file.length() == 0L) return false
        if (file.name.endsWith(tempSuffix)) return false
        
        // Verificar tamaño mínimo
        if (expectedSizeMB > 0) {
            val minSize = expectedSizeMB * 1024L * 1024L * 90 / 100 // 90%
            if (file.length() < minSize) return false
        }
        
        return true
    }
    
    // Verificar si es archivo temporal
    fun isPartial(file: File): Boolean {
        return file.name.endsWith(tempSuffix)
    }
    
    // Obtener tamaño en MB
    fun getSizeMB(file: File): Int {
        return (file.length() / (1024 * 1024)).toInt()
    }
    
    // Generar nombre de archivo temporal
    fun getTempName(fileName: String): String {
        return "$fileName$tempSuffix"
    }
}