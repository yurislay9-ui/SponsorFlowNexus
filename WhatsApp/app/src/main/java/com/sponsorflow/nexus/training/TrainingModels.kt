/*
 * Training Models - Data classes for Training system
 */
package com.sponsorflow.nexus.training

/**
 * Categoría de entrenamiento
 */
data class TrainingCategory(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val isRequired: Boolean = false
)

/**
 * Item de entrenamiento (una lección)
 */
data class TrainingItem(
    val id: String,
    val categoryId: String,
    val title: String,
    val content: String,
    val keywords: List<String> = emptyList(),
    val examples: List<String> = emptyList(),
    val priority: Int = 0,
    val isActive: Boolean = true
)

/**
 * Configuración de entrenamiento para un cliente
 */
data class ClientTraining(
    val phone: String,
    val businessName: String,
    val businessType: String,
    val tone: String,
    val language: String = "es",
    val trainingItems: List<TrainingItem> = emptyList(),
    val customRules: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val faq: List<QAPair> = emptyList(),
    val products: List<ProductInfo> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Pregunta frecuente
 */
data class QAPair(
    val id: String,
    val question: String,
    val answer: String,
    val keywords: List<String> = emptyList(),
    val category: String = "general"
)

/**
 * Información de producto para la IA
 */
data class ProductInfo(
    val id: String,
    val name: String,
    val description: String,
    val price: Double?,
    val features: List<String> = emptyList(),
    val keywords: List<String> = emptyList()
)

/**
 * Plantilla de entrenamiento
 */
data class TrainingTemplate(
    val id: String,
    val name: String,
    val businessType: String,
    val description: String,
    val categories: List<String>,
    val defaultItems: List<TrainingItem>
)

/**
 * Prompt generado automáticamente
 */
data class GeneratedPrompt(
    val fullPrompt: String,
    val systemPrompt: String,
    val contextPrompt: String,
    val examples: List<String>,
    val keywords: List<String>
)

/**
 * Estadísticas de entrenamiento
 */
data class TrainingStats(
    val totalClients: Int,
    val totalTrainingItems: Int,
    val totalFAQs: Int,
    val totalProducts: Int,
    val businessTypeDistribution: Map<String, Int>
)
