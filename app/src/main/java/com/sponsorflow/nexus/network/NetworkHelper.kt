/*
 * SponsorFlow Nexus v2.4 - Network Helper
 * CORREGIDO: Version actualizada a v2.4, timeouts adecuados
 */
package com.sponsorflow.nexus.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NetworkHelper {
    
    // Cliente con timeouts configurados para operaciones normales
    fun createClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    // Timeout corto para pings
    fun createPingClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .build()
    }
}
