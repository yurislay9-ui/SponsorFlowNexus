/*
 * SponsorFlow Nexus v1.0 - Storage Helper
 * CORREGIDO: Version actualizada a v1.0
 */
package com.sponsorflow.nexus.ai.download

import android.content.Context
import android.os.StatFs
import android.util.Log
import java.io.File

class StorageHelper(private val context: Context) {
    
    fun getAvailableSpace(): Long {
        return try {
            val stat = StatFs(context.filesDir.path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }
    
    fun hasSpace(requiredMB: Int): Boolean {
        val available = getAvailableSpace()
        val required = (requiredMB + 50) * 1024L * 1024L
        return available >= required
    }
    
    fun getAvailableMB(): Int {
        return (getAvailableSpace() / (1024 * 1024)).toInt()
    }
    
    fun cleanTempFiles(directory: File) {
        try {
            directory.listFiles()
                ?.filter { it.name.endsWith(".downloading") }
                ?.forEach { it.delete() }
            
            directory.listFiles()
                ?.filter { it.length() == 0L }
                ?.forEach { it.delete() }
        } catch (e: Exception) {
            Log.e("Nexus", "Error limpiando temporales: ${e.message}")
        }
    }
    
    fun getUsageMB(directory: File): Int {
        return (directory.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum() / (1024 * 1024)).toInt()
    }
}
