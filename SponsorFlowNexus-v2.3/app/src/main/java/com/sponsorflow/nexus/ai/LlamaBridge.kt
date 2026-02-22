/*
 * SponsorFlow Nexus v2.3 - Llama.cpp JNI Bridge
 */
package com.sponsorflow.nexus.ai

class LlamaBridge {

    private var modelHandle: Long = 0
    private var isLoaded: Boolean = false

    init {
        System.loadLibrary("llamanexus")
    }

    @Throws(Exception::class)
    external fun loadModelNative(modelPath: String): Long

    @Throws(Exception::class)
    external fun runInferenceNative(modelHandle: Long, prompt: String, maxTokens: Int, temperature: Float): String

    external fun unloadModelNative(modelHandle: Long)

    external fun getTokenCountNative(modelHandle: Long, text: String): Int

    fun loadModel(path: String): Boolean {
        return try {
            if (isLoaded) unloadModel()
            modelHandle = loadModelNative(path)
            isLoaded = modelHandle != 0L
            isLoaded
        } catch (e: Exception) {
            isLoaded = false
            false
        }
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

    companion object {
        init {
            System.loadLibrary("llamanexus")
        }
    }
}