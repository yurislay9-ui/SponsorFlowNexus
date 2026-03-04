package com.sponsorflow.nexus.security

import android.content.Context
import android.content.SharedPreferences

class SecurePrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
    fun put(key: String, value: String) = prefs.edit().putString(key, value).apply()
    fun get(key: String, default: String = ""): String = prefs.getString(key, default) ?: default
    fun remove(key: String) = prefs.edit().remove(key).apply()
    fun clear() = prefs.edit().clear().apply()
}
