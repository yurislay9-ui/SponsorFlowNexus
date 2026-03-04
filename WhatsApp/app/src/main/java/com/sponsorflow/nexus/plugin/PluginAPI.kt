package com.sponsorflow.nexus.plugin

import android.content.Context

class PluginAPI(private val context: Context) {
    fun registerPlugin(id: String, name: String) {}
    fun unregisterPlugin(id: String) {}
    fun getPlugins(): List<String> = emptyList()
}
