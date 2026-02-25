/*
 * SponsorFlow Nexus v1.0 - Secure Logging
 */
package com.sponsorflow.nexus.core.util

import android.util.Log
import com.sponsorflow.nexus.BuildConfig

object SecureLog {
    
    // Solo loguear en DEBUG builds
    private val isDebug = BuildConfig.DEBUG
    
    fun d(tag: String, message: String) {
        if (isDebug) Log.d(tag, message)
    }
    
    fun e(tag: String, message: String) {
        if (isDebug) Log.e(tag, message)
    }
    
    fun w(tag: String, message: String) {
        if (isDebug) Log.w(tag, message)
    }
    
    fun i(tag: String, message: String) {
        if (isDebug) Log.i(tag, message)
    }
    
    fun v(tag: String, message: String) {
        if (isDebug) Log.v(tag, message)
    }
    
    // Para errores críticos que siempre se deben registrar
    fun critical(tag: String, message: String) {
        // En producción, enviar a crashlytics o similar
        if (isDebug) Log.e(tag, message)
    }
}