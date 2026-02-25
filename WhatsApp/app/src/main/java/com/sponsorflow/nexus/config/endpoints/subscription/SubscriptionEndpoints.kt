package com.sponsorflow.nexus.config.endpoints.subscription

import com.sponsorflow.nexus.BuildConfig

object SubscriptionEndpoints {
    private val BASE_URL = BuildConfig.SERVER_URL
    private val API_BASE = "$BASE_URL/api/subscription"
    
    fun activate() = "$API_BASE/activate"
    fun renew() = "$API_BASE/renew"
    fun cancel() = "$API_BASE/cancel"
    fun status() = "$API_BASE/status"
}