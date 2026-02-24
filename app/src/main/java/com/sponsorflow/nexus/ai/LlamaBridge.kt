/*
 * SponsorFlow Nexus v1.0 - Llama.cpp JNI Bridge
 * CORREGIDO: Null safety con Result, manejo de UnsatisfiedLinkError
 */
package com.sponsorflow.nexus.ai

/**
 * Bridge JNI para comunicación con librería nativa Llama.cpp
 * Proporciona inferencia de modelos GGUF locales.
 */
class LlamaBridge {

    private var modelHandle: Long = 0
    private var isLoaded: Boolean = false
    private var nativeLibraryLoaded: Boolean = false

    init {
        nativeLibraryLoaded = try {
            System.loadLibrary("llamanexus")
            true
        } catch (e: UnsatisfiedLinkError) {
            android.util.Log.e("LlamaBridge", "Failed to load native library", e)
            false
        }
    }

    @Throws(Exception::class)
    external fun loadModelNative(modelPath: String): Long

    @Throws(Exception::class)
    external fun runInferenceNative(modelHandle: Long, prompt: String, maxTokens: Int, temperature: Float): String

    external fun unloadModelNative(modelHandle: Long)

    external fun getTokenCountNative(modelHandle: Long, text: String): Int

    /**
     * Carga un modelo GGUF desde la ruta especificada.
     * @param path Ruta al archivo del modelo
     * @return Result con true si se cargó correctamente, o error con detalles
     */
    fun loadModel(path: String): Result<Boolean> {
        if (!nativeLibraryLoaded) {
            return Result.failure(NativeLibraryException("Native library not loaded"))
        }
        
        return try {
            if (isLoaded) unloadModel()
            val handle = loadModelNative(path)
            if (handle == 0L) {
                Result.failure(ModelLoadException("Failed to load model from: $path"))
            } else {
                modelHandle = handle
                isLoaded = true
                Result.success(true)
            }
        } catch (e: UnsatisfiedLinkError) {
            Result.failure(NativeLibraryException("Native method not found: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(ModelLoadException("Error loading model: ${e.message}"))
        }
    }
    
    /**
     * Versión simplificada para compatibilidad
     */
    fun loadModelSimple(path: String): Boolean {
        return loadModel(path).getOrDefault(false)
    }

    fun runInference(prompt: String, maxTokens: Int = 256, temperature: Float = 0.7f): String? {
        return try {
            if (!isLoaded || modelHandle == 0L) return null
            runInferenceNative(modelHandle, prompt, maxTokens, temperature)
        } catch (e: Exception) {
            null
        }
    }

    fun unloadModel() {
        if (modelHandle != 0L) {
            unloadModelNative(modelHandle)
            modelHandle = 0
            isLoaded = false
        }
    }

    fun isModelLoaded(): Boolean = isLoaded && modelHandle != 0L
}

/**
 * Excepción lanzada cuando falla la carga de la librería nativa
 */
class NativeLibraryException(message: String) : Exception(message)

/**
 * Excepción lanzada cuando falla la carga del modelo
 */
class ModelLoadException(message: String) : Exception(message)
