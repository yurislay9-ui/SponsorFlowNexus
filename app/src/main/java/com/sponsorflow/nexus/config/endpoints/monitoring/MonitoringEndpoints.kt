package com.sponsorflow.nexus.config.endpoints.monitoring

import com.sponsorflow.nexus.BuildConfig

object MonitoringEndpoints {
    private val BASE_URL = BuildConfig.SERVER_URL
    private val API_BASE = "$BASE_URL/api/monitoring"
    
    fun metrics() = "$API_BASE/metrics"
    fun errorReport() = "$API_BASE/error"
    fun logs() = "$API_BASE/logs"
    fun health() = "$API_BASE/health"
    fun heartbeat() = "$API_BASE/heartbeat"
}