/*
 * Training Constants - Constants for Training system
 */
package com.sponsorflow.nexus.training

/**
 * Constantes del sistema de entrenamiento
 */
object TrainingConstants {

    // Categorías de entrenamiento por defecto
    val DEFAULT_CATEGORIES = listOf(
        TrainingCategory(
            id = "business",
            name = "Información del Negocio",
            description = "Datos básicos de tu empresa",
            icon = "🏢",
            isRequired = true
        ),
        TrainingCategory(
            id = "products",
            name = "Productos y Servicios",
            description = "Qué vendes y cómo lo describís",
            icon = "📦"
        ),
        TrainingCategory(
            id = "responses",
            name = "Respuestas Predefinidas",
            description = "Cómo responder a situaciones comunes",
            icon = "💬"
        ),
        TrainingCategory(
            id = "keywords",
            name = "Palabras Clave",
            description = "Términos importantes de tu nicho",
            icon = "🔑"
        ),
        TrainingCategory(
            id = "faq",
            name = "Preguntas Frecuentes",
            description = "Preguntas y respuestas comunes",
            icon = "❓"
        ),
        TrainingCategory(
            id = "tone",
            name = "Tono y Estilo",
            description = "Cómo debe sonar la IA",
            icon = "🎭"
        ),
        TrainingCategory(
            id = "rules",
            name = "Reglas Personalizadas",
            description = "Normas específicas de tu negocio",
            icon = "📋"
        ),
        TrainingCategory(
            id = "objections",
            name = "Objectiones Comunes",
            description = "Cómo manejar objeciones",
            icon = "🛡️"
        ),
        TrainingCategory(
            id = "closing",
            name = "Cierre de Ventas",
            description = "Técnicas para cerrar",
            icon = "🤝"
        ),
        TrainingCategory(
            id = "forbidden",
            name = "Temas Prohibidos",
            description = "Lo que la IA NO debe decir",
            icon = "🚫"
        )
    )

    // Tipos de negocio
    val BUSINESS_TYPES = listOf(
        "ecommerce" to "Tienda Online",
        "restaurant" to "Restaurante",
        "support" to "Soporte Técnico",
        "realestate" to "Bienes Raíces",
        "health" to "Salud/Belleza",
        "education" to "Educación",
        "finance" to "Finanzas",
        "automotive" to "Automotriz",
        "food" to "Comida/Bebida",
        "retail" to "Tienda Física",
        "services" to "Servicios Profesionales",
        "other" to "Otro"
    )

    // Tonos disponibles
    val TONES = listOf(
        "formal" to "Formal - Profesional y serio",
        "casual" to "Casual - Amigable y relajado",
        "friendly" to "Amigable - Cercano y cálido",
        "professional" to "Profesional - Directo y eficiente",
        "funny" to "Divertido - Con humor",
        "luxury" to "Lujo - Exclusivo y elegante"
    )

    // Descripciones de tono
    fun getToneDescription(tone: String): String = when (tone) {
        "formal" -> "Sé formal, profesional y respetuoso en todo momento."
        "casual" -> "Sé casual, amigable y relajado. Usa un tono cercano."
        "friendly" -> "Sé muy amigable y cálido. Haz que el cliente se sienta cómodo."
        "professional" -> "Sé profesional, directo y eficiente. Ve al grano."
        "funny" -> "Sé divertido pero profesional. Usa humor ligero."
        "luxury" -> "Sé exclusivo y elegante. Trata al cliente como VIP."
        else -> "Sé profesional y amable."
    }

    // Obtener nombre del tipo de negocio
    fun getBusinessTypeName(type: String): String {
        return BUSINESS_TYPES.find { it.first == type }?.second ?: type
    }
}
