/*
 * SponsorFlow Nexus v2.3 - Network Helper
 */
package com.sponsorflow.nexus.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NetworkHelper {
    
    // Cliente con timeouts configurados
    fun createClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
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