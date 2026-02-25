/**
 * SponsorFlow Nexus v1.0 - Plugin API
 * 
 * API centralizada para la gestión y comunicación con plugins externos.
 * Proporciona una interfaz segura y controlada para la integración
 * de funcionalidades adicionales en el sistema.
 * 
 * Este componente es fundamental para la extensibilidad del sistema,
 * permitiendo la carga, validación y ejecución segura de plugins
 * desarrollados por terceros o internamente.
 * 
 * @author SponsorFlow Nexus Team
 * @version 1.0
 * @since 1.0
 */
package com.sponsorflow.nexus.plugin

import android.content.Context

/**
 * Clase base abstracta para la creación de plugins en el sistema.
 * 
 * Esta clase proporciona una base segura y controlada para el desarrollo
 * de plugins que se integran con el sistema SponsorFlow Nexus. Implementa
 * el patrón Template Method para garantizar un ciclo de vida consistente
 * de inicialización y finalización de plugins.
 * 
 * Los desarrolladores deben extender esta clase para crear plugins
 * personalizados que se integren con el sistema de forma segura.
 * 
 * @property context Contexto de Android proporcionado durante la inicialización
 * 
 * @see NexusPlugin
 * @see PluginResult
 * @see PluginBuilder
 */
abstract class NexusPluginBase : NexusPlugin {
    
    /**
     * Contexto de Android proporcionado por el sistema para el plugin.
     * 
     * Este contexto debe ser utilizado para acceder a recursos del sistema,
     * realizar operaciones de I/O, o cualquier otra funcionalidad que requiera
     * un contexto de Android.
     */
    protected lateinit var context: Context
    
    /**
     * Inicializa el plugin con el contexto proporcionado.
     * 
     * Este método configura el contexto del plugin y llama al método
     * de inicialización específico del plugin. Debe ser llamado antes
     * de cualquier otra operación del plugin.
     * 
     * @param context Contexto de Android para el plugin
     * 
     * @see onInitialize
     */
    override fun initialize(context: Context) {
        this.context = context
        onInitialize()
    }
    
    /**
     * Método de inicialización específico del plugin.
     * 
     * Los plugins deben sobrescribir este método para implementar su
     * lógica de inicialización específica. Este método se llama después
     * de que el contexto ha sido establecido.
     * 
     * Por defecto no realiza ninguna acción, pero puede ser sobreescrito
     * por las clases hijas para proporcionar funcionalidad de inicialización.
     */
    protected open fun onInitialize() {}
    
    /**
     * Finaliza el plugin y libera recursos.
     * 
     * Este método llama al método de finalización específico del plugin
     * y asegura que todos los recursos sean liberados adecuadamente.
     * 
     * @see onShutdown
     */
    override fun shutdown() {
        onShutdown()
    }
    
    /**
     * Método de finalización específico del plugin.
     * 
     * Los plugins deben sobrescribir este método para implementar su
     * lógica de limpieza y liberación de recursos. Este método se llama
     * antes de que el plugin sea completamente desactivado.
     * 
     * Por defecto no realiza ninguna acción, pero puede ser sobreescrito
     * por las clases hijas para proporcionar funcionalidad de limpieza.
     */
    protected open fun onShutdown() {}
    
    /**
     * Crea un resultado exitoso para una operación de plugin.
     * 
     * Este método facilita la creación de resultados exitosos que pueden
     * incluir datos adicionales en forma de mapa. Es útil para retornar
     * información del resultado de una operación del plugin.
     * 
     * @param data Mapa opcional con datos adicionales del resultado
     * @return PluginResult con éxito establecido en true y los datos proporcionados
     * 
     * @see PluginResult
     * @see error
     */
    protected fun success(data: Map<String, Any> = emptyMap()): PluginResult {
        return PluginResult(true, data)
    }
    
    /**
     * Crea un resultado de error para una operación de plugin.
     * 
     * Este método facilita la creación de resultados de error que incluyen
     * un mensaje descriptivo del problema ocurrido. Es útil para retornar
     * información sobre fallos en las operaciones del plugin.
     * 
     * @param message Mensaje descriptivo del error ocurrido
     * @return PluginResult con éxito establecido en false y el mensaje de error
     * 
     * @see PluginResult
     * @see success
     */
    protected fun error(message: String): PluginResult {
        return PluginResult(false, error = message)
    }
}

/**
 * Constructor de plugins para facilitar la creación de información de plugins.
 * 
 * Esta clase proporciona una interfaz fluida (fluent interface) para
 * construir objetos PluginInfo de forma sencilla y legible. Permite
 * configurar todas las propiedades de un plugin de manera encadenada.
 * 
 * @see PluginInfo
 * @see NexusPluginBase
 */
class PluginBuilder {
    /**
     * Identificador único del plugin.
     * 
     * Este identificador debe ser único en todo el sistema y se utiliza
     * para identificar y gestionar el plugin.
     */
    private var id: String = ""
    
    /**
     * Nombre descriptivo del plugin.
     * 
     * Este nombre será mostrado a los usuarios y debe ser descriptivo
     * y fácil de entender.
     */
    private var name: String = ""
    
    /**
     * Versión del plugin.
     * 
     * Por defecto se establece en "1.0.0". Se recomienda seguir el
     * estándar de versionado semántico (SemVer).
     */
    private var version: String = "1.0.0"
    
    /**
     * Tipo de plugin según su funcionalidad.
     * 
     * Define el tipo de plugin según la enumeración PluginType.
     * Por defecto se establece en PluginType.ACTION.
     */
    private var type: PluginType = PluginType.ACTION
    
    /**
     * Autor del plugin.
     * 
     * Nombre o identificador del desarrollador o equipo que creó el plugin.
     */
    private var author: String = ""
    
    /**
     * Descripción del plugin.
     * 
     * Texto descriptivo que explica la funcionalidad y propósito del plugin.
     */
    private var description: String = ""
    
    /**
     * Establece el identificador del plugin.
     * 
     * @param id Identificador único del plugin
     * @return Esta instancia de PluginBuilder para encadenamiento
     */
    fun id(id: String) = apply { this.id = id }
    
    /**
     * Establece el nombre del plugin.
     * 
     * @param name Nombre descriptivo del plugin
     * @return Esta instancia de PluginBuilder para encadenamiento
     */
    fun name(name: String) = apply { this.name = name }
    
    /**
     * Establece la versión del plugin.
     * 
     * @param v Versión del plugin (recomendado formato SemVer)
     * @return Esta instancia de PluginBuilder para encadenamiento
     */
    fun version(v: String) = apply { this.version = v }
    
    /**
     * Establece el tipo del plugin.
     * 
     * @param t Tipo de plugin según PluginType
     * @return Esta instancia de PluginBuilder para encadenamiento
     */
    fun type(t: PluginType) = apply { this.type = t }
    
    /**
     * Establece el autor del plugin.
     * 
     * @param a Nombre o identificador del autor
     * @return Esta instancia de PluginBuilder para encadenamiento
     */
    fun author(a: String) = apply { this.author = a }
    
    /**
     * Establece la descripción del plugin.
     * 
     * @param d Descripción del plugin
     * @return Esta instancia de PluginBuilder para encadenamiento
     */
    fun description(d: String) = apply { this.description = d }
    
    /**
     * Construye y retorna un objeto PluginInfo con los valores configurados.
     * 
     * @return Objeto PluginInfo con toda la información del plugin
     * 
     * @see PluginInfo
     */
    fun buildInfo() = PluginInfo(id, name, version, type, author, description)
}
