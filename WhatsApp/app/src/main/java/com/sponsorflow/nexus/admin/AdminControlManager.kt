package com.sponsorflow.nexus.admin

import android.content.Context

class AdminControlManager(private val context: Context) {
    fun isAdmin(): Boolean = false
    fun getStats(): Map<String, Any> = emptyMap()
}
