/*
 * SponsorFlow Nexus v1.0 - AI Engine Interface
 * Skill: Mejores prácticas - Motor de IA abstracto
 */
package com.sponsorflow.nexus.core.contracts.ai

import com.sponsorflow.nexus.core.result.AppResult

interface IAIEngine {
    suspend fun loadModel(modelPath: String): AppResult<Unit>
    suspend fun generateResponse(prompt: String, maxTokens: Int = 256, temperature: Float = 0.7f): AppResult<String>
    fun unloadModel()
    fun isModelLoaded(): Boolean
    fun getModelInfo(): ModelInfo?
    fun cancelGeneration()
    fun isGenerating(): Boolean
}

data class ModelInfo(
    val name: String,
    val path: String,
    val size: Long,
    val contextSize: Int,
    val loadTime: Long
) {
    fun getFormattedSize(): String {
        val mb = size / (1024.0 * 1024.0)
        return if (mb >= 1024) "%.2f GB".format(mb / 1024.0) else "%.2f MB".format(mb)
    }
}

data class InferenceResult(
    val text: String,
    val tokensGenerated: Int,
    val inferenceTimeMs: Long,
    val wasSuccessful: Boolean
)

enum class StopReason { COMPLETED, MAX_TOKENS, CANCELLED, ERROR }