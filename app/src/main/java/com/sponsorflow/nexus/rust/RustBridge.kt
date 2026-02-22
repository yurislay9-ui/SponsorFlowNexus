/*
 * SponsorFlow Nexus v2.4 - Rust Bridge
 * Puente JNI para llamar funciones nativas de Rust
 */
package com.sponsorflow.nexus.rust

/**
 * Puente para comunicar Kotlin con Rust via JNI
 * 
 * Uso:
 * ```kotlin
 * val result = RustBridge.addNumbers(5, 3) // 8
 * val version = RustBridge.getRustVersion() // "0.1.0"
 * val greeting = RustBridge.greet("Usuario") // "¡Hola desde Rust, Usuario! 🦀"
 * ```
 */
object RustBridge {

    init {
        // Cargar la librería nativa
        // El archivo se llama libnexus_rust.so en Android
        System.loadLibrary("nexus_rust")
    }

    // ==================== FUNCIONES DE PRUEBA ====================

    /**
     * Suma dos números enteros
     * Función de prueba para verificar que JNI funciona
     * 
     * @param a Primer número
     * @param b Segundo número
     * @return La suma de a + b
     */
    @JvmStatic
    external fun addNumbers(a: Int, b: Int): Int

    /**
     * Obtiene la versión de la librería Rust
     * 
     * @return Versión del crate Rust (ej: "0.1.0")
     */
    @JvmStatic
    external fun getRustVersion(): String

    /**
     * Función de prueba con strings
     * 
     * @param name Nombre a saludar
     * @return Saludo desde Rust
     */
    @JvmStatic
    external fun greet(name: String): String

    /**
     * Verifica que la librería está funcionando correctamente
     * 
     * @return true si la librería está cargada y funcionando
     */
    @JvmStatic
    external fun healthCheck(): Boolean

    // ==================== CRYPTO MODULE ====================

    /**
     * Hashea una contraseña usando Argon2
     * 
     * @param password Contraseña en texto plano
     * @param salt Salt para el hash (16 bytes recomendado)
     * @return Hash de la contraseña o null si hay error
     */
    @JvmStatic
    external fun hashPassword(password: String, salt: ByteArray): ByteArray?

    /**
     * Encripta datos usando AES-256-GCM
     * 
     * @param plaintext Datos a encriptar
     * @param key Clave de 32 bytes
     * @return Datos encriptados (ciphertext + tag) o null si hay error
     */
    @JvmStatic
    external fun encryptAesGcm(plaintext: ByteArray, key: ByteArray): ByteArray?

    /**
     * Desencripta datos usando AES-256-GCM
     * 
     * @param ciphertext Datos encriptados
     * @param key Clave de 32 bytes
     * @return Datos desencriptados o null si hay error
     */
    @JvmStatic
    external fun decryptAesGcm(ciphertext: ByteArray, key: ByteArray): ByteArray?

    // ==================== PAYMENT MODULE ====================

    /**
     * Valida una dirección de wallet TRON
     * 
     * @param address Dirección a validar (ej: "TRX...")
     * @return true si la dirección es válida
     */
    @JvmStatic
    external fun validateTronAddress(address: String): Boolean

    /**
     * Verifica una transacción TRC20
     * 
     * @param txHex Hex de la transacción
     * @param expectedAmount Monto esperado en SUN
     * @return true si la transacción es válida
     */
    @JvmStatic
    external fun verifyTrc20Transaction(txHex: String, expectedAmount: Long): Boolean

    // ==================== UTILIDADES ====================

    /**
     * Verifica que el puente Rust está funcionando
     * Llama a todas las funciones de prueba y reporta el estado
     */
    fun runDiagnostics(): DiagnosticResult {
        return try {
            val addResult = addNumbers(10, 5)
            val version = getRustVersion()
            val greeting = greet("Test")
            val health = healthCheck()

            DiagnosticResult(
                success = true,
                message = "Rust bridge funcionando correctamente",
                addResult = addResult,
                version = version,
                greeting = greeting,
                healthCheck = health
            )
        } catch (e: Exception) {
            DiagnosticResult(
                success = false,
                message = "Error en Rust bridge: ${e.message}"
            )
        }
    }

    data class DiagnosticResult(
        val success: Boolean,
        val message: String,
        val addResult: Int = 0,
        val version: String = "",
        val greeting: String = "",
        val healthCheck: Boolean = false
    ) {
        override fun toString(): String {
            return if (success) {
                """
                ✅ Rust Bridge Diagnóstico OK
                ├── Versión: $version
                ├── addNumbers(10, 5) = $addResult
                ├── greet("Test") = $greeting
                └── healthCheck() = $healthCheck
                """.trimIndent()
            } else {
                "❌ Rust Bridge Error: $message"
            }
        }
    }
}