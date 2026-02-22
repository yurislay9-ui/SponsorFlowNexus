/*
 * SponsorFlow Nexus v2.3 - Memory Limiter
 */
package com.sponsorflow.nexus.ai

import android.app.ActivityManager
import android.content.Context

object MemoryLimiter {
    
    // MB mínimos requeridos por plan
    private val planMemoryRequirements = mapOf(
        "FREE" to 512L,
        "BASIC" to 1024L,
        "PRO" to 2048L,
        "ENTERPRISE" to 4096L
    )
    
    // Obtener memoria disponible en MB
    fun getAvailableMemoryMB(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) 
            as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem / (1024 * 1024)
    }
    
    // Obtener memoria total en MB
    fun getTotalMemoryMB(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) 
            as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem / (1024 * 1024)
    }
    
    // Verificar si hay suficiente memoria para el plan
    fun canLoadModel(context: Context, plan: String): Boolean {
        val available = getAvailableMemoryMB(context)
        val required = planMemoryRequirements[plan] ?: 1024L
        return available >= required
    }
    
    // Obtener memoria máxima usable para IA (en MB)
    fun getMaxUsableMemory(context: Context): Long {
        val available = getAvailableMemoryMB(context)
        // Usar máximo 50% de memoria disponible
        return (available * 0.5).toLong()
    }
    
    // Verificar si dispositivo es gama baja (< 2GB)
    fun isLowEndDevice(context: Context): Boolean {
        return getTotalMemoryMB(context) < 2048
    }
    
    // Obtener configuración de memoria recomendada
    fun getRecommendedConfig(context: Context): MemoryConfig {
        val total = getTotalMemoryMB(context)
        return when {
            total < 1024 -> MemoryConfig(
                contextSize = 512,
                threads = 2,
                batchSize = 128
            )
            total < 2048 -> MemoryConfig(
                contextSize = 1024,
                threads = 4,
                batchSize = 256
            )
            total < 4096 -> MemoryConfig(
                contextSize = 2048,
                threads = 6,
                batchSize = 512
            )
            else -> MemoryConfig(
                contextSize = 4096,
                threads = 8,
                batchSize = 1024
            )
        }
    }
}

data class MemoryConfig(
    val contextSize: Int,
    val threads: Int,
    val batchSize: Int
)