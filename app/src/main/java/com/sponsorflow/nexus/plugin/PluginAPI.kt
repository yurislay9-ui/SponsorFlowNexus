/*
 * SponsorFlow Nexus v1.0 - Plugin API
 * API para que desarrolladores creen plugins
 * CORREGIDO: Version actualizada a v1.0
 */
package com.sponsorflow.nexus.plugin

import android.content.Context

abstract class NexusPluginBase : NexusPlugin {
    
    protected lateinit var context: Context
    
    override fun initialize(context: Context) {
        this.context = context
        onInitialize()
    }
    
    protected open fun onInitialize() {}
    
    override fun shutdown() {
        onShutdown()
    }
    
    protected open fun onShutdown() {}
    
    protected fun success(data: Map<String, Any> = emptyMap()): PluginResult {
        return PluginResult(true, data)
    }
    
    protected fun error(message: String): PluginResult {
        return PluginResult(false, error = message)
    }
}

// Helper para crear plugins fácilmente
class PluginBuilder {
    private var id: String = ""
    private var name: String = ""
    private var version: String = "1.0.0"
    private var type: PluginType = PluginType.ACTION
    private var author: String = ""
    private var description: String = ""
    
    fun id(id: String) = apply { this.id = id }
    fun name(name: String) = apply { this.name = name }
    fun version(v: String) = apply { this.version = v }
    fun type(t: PluginType) = apply { this.type = t }
    fun author(a: String) = apply { this.author = a }
    fun description(d: String) = apply { this.description = d }
    
    fun buildInfo() = PluginInfo(id, name, version, type, author, description)
}
