/*
 * SponsorFlow Nexus v1.0 - Memory Limiter
 * Plan: BASICO, AVANZADO, VIP
 * CORREGIDO: Version actualizada a v1.0
 */
package com.sponsorflow.nexus.ai

import android.app.ActivityManager
import android.content.Context

object MemoryLimiter {
    
    // MB mínimos requeridos por plan
    private val planMemoryRequirements = mapOf(
        "BASICO" to 512L,
        "AVANZADO" to 1024L,
        "VIP" to 2048L
    )
    
    fun getAvailableMemoryMB(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem / (1024 * 1024)
    }
    
    fun getTotalMemoryMB(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem / (1024 * 1024)
    }
    
    fun canLoadModel(context: Context, plan: String): Boolean {
        val available = getAvailableMemoryMB(context)
        val required = planMemoryRequirements[plan] ?: 512L
        return available >= required
    }
    
    fun getMaxUsableMemory(context: Context): Long {
        val available = getAvailableMemoryMB(context)
        return (available * 0.5).toLong()
    }
    
    fun isLowEndDevice(context: Context): Boolean {
        return getTotalMemoryMB(context) < 2048
    }
    
    fun getRecommendedConfig(context: Context): MemoryConfig {
        val total = getTotalMemoryMB(context)
        return when {
            total < 1024 -> MemoryConfig(512, 2, 128)
            total < 2048 -> MemoryConfig(1024, 4, 256)
            total < 4096 -> MemoryConfig(2048, 6, 512)
            else -> MemoryConfig(4096, 8, 1024)
        }
    }
}

data class MemoryConfig(
    val contextSize: Int,
    val threads: Int,
    val batchSize: Int
)
