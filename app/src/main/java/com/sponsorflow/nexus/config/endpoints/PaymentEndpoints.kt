package com.sponsorflow.nexus.config.endpoints

import com.sponsorflow.nexus.BuildConfig

object PaymentEndpoints {
    private val BASE_URL = BuildConfig.SERVER_URL
    private val API_BASE = "$BASE_URL/api/payment"
    
    fun initiate() = "$API_BASE/initiate"
    fun status() = "$API_BASE/status"
    fun verify() = "$API_BASE/verify"
    fun refund() = "$API_BASE/refund"
    fun history() = "$API_BASE/history"
}