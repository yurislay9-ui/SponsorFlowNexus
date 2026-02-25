/*
 * SponsorFlow Nexus v1.0 - AI Engine (Singleton)
 * CORREGIDO: Thread-safe con Mutex y AtomicBoolean
 */
package com.sponsorflow.nexus.ai

import com.sponsorflow.nexus.core.contracts.ai.IAIEngine
import com.sponsorflow.nexus.core.contracts.ai.ModelInfo
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

object AIEngine : IAIEngine {

    private var llamaBridge: LlamaBridge? = null
    private var modelInfo: ModelInfo? = null
    
    // Thread-safe: AtomicBoolean para estados
    private val isGenerating = AtomicBoolean(false)
    private val cancelled = AtomicBoolean(false)
    
    // Mutex para serializar operaciones de generación (suspend context)
    private val generationMutex = Mutex()
    // Lock para operaciones síncronas (no-suspend context)
    private val modelLock = Any()

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
        } catch (e: IllegalArgumentException) {
            AppResult.Error(AppError.ValidationError(e.message ?: "Invalid parameters"))
        } catch (e: IllegalStateException) {
            AppResult.Error(AppError.AIError(e.message ?: "AI state error"))
        } catch (e: Exception) {
            AppResult.Error(AppError.fromException(e))
        }
    }

    override suspend fun generateResponse(
        prompt: String,
        maxTokens: Int,
        temperature: Float
    ): AppResult<String> {
        // Usar Mutex para evitar generaciones concurrentes
        return generationMutex.withLock {
            return@withLock try {
                if (!isModelLoaded()) {
                    return@withLock AppResult.Error(AppError.AIError("Modelo no cargado"))
                }
                
                // Verificar y marcar como generando
                if (isGenerating.getAndSet(true)) {
                    return@withLock AppResult.Error(AppError.AIError("Ya hay una generación en progreso"))
                }
                
                cancelled.set(false)
                
                try {
                    val result = llamaBridge?.runInference(prompt, maxTokens, temperature)
                    
                    if (cancelled.get()) {
                        AppResult.Error(AppError.AIError("Cancelado"))
                    } else {
                        AppResult.Success(result ?: "")
                    }
                } finally {
                    isGenerating.set(false)
                }
            } catch (e: IllegalArgumentException) {
                isGenerating.set(false)
                AppResult.Error(AppError.ValidationError(e.message ?: "Invalid parameters"))
            } catch (e: IllegalStateException) {
                isGenerating.set(false)
                AppResult.Error(AppError.AIError(e.message ?: "AI state error"))
            } catch (e: Exception) {
                isGenerating.set(false)
                AppResult.Error(AppError.fromException(e))
            }
        }
    }

    // CORREGIDO: Usar synchronized para función no-suspend
    override fun unloadModel() {
        synchronized(modelLock) {
            llamaBridge?.unloadModel()
            modelInfo = null
        }
    }

    override fun isModelLoaded(): Boolean = llamaBridge?.isModelLoaded() ?: false

    override fun getModelInfo(): ModelInfo? = modelInfo

    override fun cancelGeneration() {
        cancelled.set(true)
    }

    override fun isGenerating(): Boolean = isGenerating.get()
    
    // Cleanup para lifecycle
    fun destroy() {
        unloadModel()
        llamaBridge = null
    }
}
