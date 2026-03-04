/*
 * Risk Level Manager (Compact)
 */
package com.sponsorflow.nexus.risk

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.ConcurrentHashMap

class RiskLevelManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("nexus_risk", Context.MODE_PRIVATE)
    private val phoneRiskLevels = ConcurrentHashMap<String, RiskLevel>()
    private var currentLevel = RiskLevel.NORMAL

    fun setRiskLevel(level: RiskLevel) { currentLevel = level; prefs.edit().putString("level", level.name).apply() }
    fun getRiskLevel(): RiskLevel = currentLevel
    fun setPhoneRiskLevel(phone: String, level: RiskLevel) { phoneRiskLevels[phone] = level }
    fun getPhoneRiskLevel(phone: String): RiskLevel = phoneRiskLevels[phone] ?: currentLevel

    fun checkCanSend(phone: String): RiskCheckResult {
        val level = getPhoneRiskLevel(phone)
        return RiskCheckResult(true, level.delayMinMs, null, level)
    }

    fun loadFromPrefs() { prefs.getString("level", null)?.let { currentLevel = RiskLevel.valueOf(it) } }
}
