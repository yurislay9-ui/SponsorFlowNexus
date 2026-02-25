package com.sponsorflow.nexus.config.endpoints.plugin

import com.sponsorflow.nexus.BuildConfig

object PluginEndpoints {
    private val BASE_URL = BuildConfig.SERVER_URL
    private val API_BASE = "$BASE_URL/api/plugin"
    
    fun list() = "$API_BASE/list"
    fun install() = "$API_BASE/install"
    fun uninstall() = "$API_BASE/uninstall"
    fun config() = "$API_BASE/config"
}