package com.sponsorflow.nexus.config.endpoints

import com.sponsorflow.nexus.BuildConfig

object WhatsAppEndpoints {
    private val BASE_URL = BuildConfig.SERVER_URL
    private val API_BASE = "$BASE_URL/api/whatsapp"
    
    fun licenseValidate() = "$BASE_URL/api/license/validate"
    fun configFetch() = "$BASE_URL/api/config/fetch"
    fun heartbeat() = "$BASE_URL/api/monitoring/heartbeat"
    fun paymentVerify() = "$BASE_URL/api/payment/verify"
    
    fun messageRegister() = "$API_BASE/message/register"
    fun contactsGet() = "$API_BASE/contacts"
    fun contactBlock() = "$API_BASE/contact/block"
    fun contactUnblock() = "$API_BASE/contact/unblock"
}