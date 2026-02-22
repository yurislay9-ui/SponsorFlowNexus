/*
 * SponsorFlow Nexus v2.3 - AI Engine (Singleton)
 */
package com.sponsorflow.nexus.ai

import com.sponsorflow.nexus.core.contracts.ai.IAIEngine
import com.sponsorflow.nexus.core.contracts.ai.ModelInfo
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import java.io.File

object AIEngine : IAIEngine {

    private var llamaBridge: LlamaBridge? = null
    private var modelInfo: ModelInfo? = null
    private var isGenerating: Boolean = false
    private var cancelled: Boolean = false

    fun initialize() {
        if (llamaBridge == null) {
            llamaBridge = LlamaBridge()
        }
    }

    override suspend fun loadModel(modelPath: String): AppResult<Unit> {
        return try {
            initialize()
            val file = File(modelPath)
            
            if (!file.exists()) {
                return AppResult.Error(AppError.AIError("Modelo no encontrado"))
            }
            
            // Validar GGUF antes de cargar
            val check = ModelValidator.quickCheck(file)
            when (check) {
                is ModelCheckResult.NotFound -> 
                    return AppResult.Error(AppError.AIError("Archivo no existe"))
                is ModelCheckResult.TooSmall -> 
                    return AppResult.Error(AppError.AIError("Archivo muy pequeño"))
                is ModelCheckResult.InvalidHeader -> 
                    return AppResult.Error(AppError.AIError("Archivo corrupto"))
                else -> {}
            }
            
            val startTime = System.currentTimeMillis()
            val loaded = llamaBridge?.loadModel(modelPath) ?: false
            
            if (loaded) {
                modelInfo = ModelInfo(
                    name = file.name,
                    path = modelPath,
                    size = file.length(),
                    contextSize = 2048,
                    loadTime = System.currentTimeMillis() - startTime
                )
                AppResult.Success(Unit)
            } else {
                AppResult.Error(AppError.AIError("Error al cargar modelo"))
            }
        } catch (e: Exception) {
            AppResult.Error(AppError.fromException(e))
        }
    }

    override suspend fun generateResponse(
        prompt: String,
        maxTokens: Int,
        temperature: Float
    ): AppResult<String> {
        return try {
            if (!isModelLoaded()) {
                return AppResult.Error(AppError.AIError("Modelo no cargado"))
            }
            isGenerating = true
            cancelled = false
            val result = llamaBridge?.runInference(prompt, maxTokens, temperature)
            isGenerating = false
            if (cancelled) {
                AppResult.Error(AppError.AIError("Cancelado"))
            } else {
                AppResult.Success(result ?: "")
            }
        } catch (e: Exception) {
            isGenerating = false
            AppResult.Error(AppError.fromException(e))
        }
    }

    override fun unloadModel() {
        llamaBridge?.unloadModel()
        modelInfo = null
    }

    override fun isModelLoaded(): Boolean = llamaBridge?.isModelLoaded() ?: false

    override fun getModelInfo(): ModelInfo? = modelInfo

    override fun cancelGeneration() {
        cancelled = true
    }

    override fun isGenerating(): Boolean = isGenerating
    
    // Cleanup para lifecycle
    fun destroy() {
        unloadModel()
        llamaBridge = null
    }
}