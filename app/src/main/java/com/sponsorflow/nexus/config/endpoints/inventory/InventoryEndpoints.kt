package com.sponsorflow.nexus.config.endpoints.inventory

import com.sponsorflow.nexus.BuildConfig

object InventoryEndpoints {
    private val BASE_URL = BuildConfig.SERVER_URL
    private val API_BASE = "$BASE_URL/api/inventory"
    
    fun products() = "$API_BASE/products"
    fun stockUpdate() = "$API_BASE/stock/update"
    fun saleRegister() = "$API_BASE/sale/register"
    fun stats() = "$API_BASE/stats"
}