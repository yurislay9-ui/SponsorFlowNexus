/*
 * SponsorFlow Nexus v2.4 - Plugin Manager
 * Plan: ENTERPRISE
 * CORREGIDO: Thread-safe con ConcurrentHashMap
 */
package com.sponsorflow.nexus.plugin

import android.content.Context
import java.util.concurrent.ConcurrentHashMap

interface NexusPlugin {
    val info: PluginInfo
    fun initialize(context: Context)
    fun execute(input: Map<String, Any>): PluginResult
    fun shutdown()
}

class PluginManager(private val context: Context) {

    // CORREGIDO: Thread-safe con ConcurrentHashMap
    private val plugins = ConcurrentHashMap<String, NexusPlugin>()
    private val prefs = context.getSharedPreferences("nexus_plugins", Context.MODE_PRIVATE)

    fun registerPlugin(plugin: NexusPlugin): Boolean {
        if (plugins.containsKey(plugin.info.id)) return false
        plugin.initialize(context)
        plugins[plugin.info.id] = plugin
        return true
    }

    fun unregisterPlugin(pluginId: String) {
        plugins[pluginId]?.shutdown()
        plugins.remove(pluginId)
    }

    fun getPlugin(id: String): NexusPlugin? = plugins[id]

    fun getAllPlugins(): List<PluginInfo> = plugins.values.map { it.info }

    fun enablePlugin(id: String) {
        prefs.edit().putBoolean("enabled_$id", true).apply()
    }

    fun disablePlugin(id: String) {
        prefs.edit().putBoolean("enabled_$id", false).apply()
    }

    fun isEnabled(id: String): Boolean = prefs.getBoolean("enabled_$id", true)

    fun executePlugin(id: String, input: Map<String, Any>): PluginResult {
        val plugin = plugins[id] ?: return PluginResult(false, error = "Plugin no encontrado")
        if (!isEnabled(id)) return PluginResult(false, error = "Plugin deshabilitado")
        return try {
            plugin.execute(input)
        } catch (e: Exception) {
            PluginResult(false, error = e.message ?: "Error desconocido")
        }
    }

    /**
     * Ejecuta todos los plugins de un tipo dado
     * CORREGIDO: Captura excepciones para evitar que un plugin rompa los demás
     */
    fun executeByType(type: PluginType, input: Map<String, Any>): List<PluginResult> {
        return plugins.values
            .filter { it.info.type == type && isEnabled(it.info.id) }
            .map { plugin ->
                try {
                    plugin.execute(input)
                } catch (e: Exception) {
                    PluginResult(
                        success = false,
                        error = "Plugin ${plugin.info.name} falló: ${e.message}"
                    )
                }
            }
    }
}
