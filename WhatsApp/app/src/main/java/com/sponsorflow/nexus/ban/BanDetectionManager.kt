package com.sponsorflow.nexus.ban

import android.content.Context

class BanDetectionManager(private val context: Context) {
    fun checkRisk(phone: String): Int = 0
    fun registerEvent(type: String, phone: String) {}
    fun getAlerts(): List<String> = emptyList()
}
