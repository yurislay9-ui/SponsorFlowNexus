/*
 * 24-Hour Window Manager (Compact)
 */
package com.sponsorflow.nexus.window

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap

class TwentyFourHourWindowManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("nexus_window", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val activeWindows = ConcurrentHashMap<String, ChatWindow>()
    private var config = WindowConfig()

    fun startWindow(phoneNumber: String, startedByCustomer: Boolean = true): ChatWindow {
        val now = System.currentTimeMillis()
        val window = ChatWindow(phoneNumber, now, now, now + WindowConstants.HOURS_24_MS, 0, true, startedByCustomer)
        activeWindows[phoneNumber] = window
        saveToPrefs()
        return window
    }

    fun checkWindow(phoneNumber: String): WindowCheckResult {
        val window = activeWindows[phoneNumber] ?: return WindowCheckResult(false, WindowStatus.NO_WINDOW, reason = "No active window")
        val now = System.currentTimeMillis()
        val remaining = window.windowEndTime - now
        return when {
            remaining <= 0 -> WindowCheckResult(false, WindowStatus.EXPIRED, reason = "24h window expired")
            remaining < WindowConstants.HOUR_MS -> WindowCheckResult(true, WindowStatus.NEAR_EXPIRE, remaining, window.messagesInWindow)
            else -> WindowCheckResult(true, WindowStatus.ACTIVE, remaining, window.messagesInWindow)
        }
    }

    fun recordMessage(phoneNumber: String): Boolean {
        val window = activeWindows[phoneNumber] ?: return false
        val check = checkWindow(phoneNumber)
        if (!check.canRespond) return false
        activeWindows[phoneNumber] = window.copy(messagesInWindow = window.messagesInWindow + 1)
        saveToPrefs()
        return true
    }

    fun closeWindow(phoneNumber: String) { activeWindows.remove(phoneNumber); saveToPrefs() }
    fun getWindow(phoneNumber: String): ChatWindow? = activeWindows[phoneNumber]
    fun getAllWindows() = activeWindows.toMap()
    fun setConfig(newConfig: WindowConfig) { config = newConfig; saveToPrefs() }
    private fun saveToPrefs() { prefs.edit().putString("windows", gson.toJson(activeWindows)).apply() }
    fun loadFromPrefs() { prefs.getString("windows", null)?.let { activeWindows.putAll(gson.fromJson(it)) } }
}
