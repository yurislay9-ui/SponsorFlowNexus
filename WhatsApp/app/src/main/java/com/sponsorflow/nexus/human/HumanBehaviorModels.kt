/*
 * Human Behavior Models
 */
package com.sponsorflow.nexus.human

data class HumanBehaviorConfig(
    val minDelaySeconds: Int = 3,
    val maxDelaySeconds: Int = 25,
    val activeHourStart: Int = 7,
    val activeHourEnd: Int = 23,
    val cutoffHours: Int = 24,
    val maxDailyResponses: Int = 88,
    val maxHourlyResponses: Int = 18,
    val shortResponseChance: Double = 0.3,
    val filterBotPhrases: Boolean = true,
    val enabled: Boolean = true
)

object ShortResponses {
    val affirmatives = listOf("ok", "si", "sí", "está bien", "perfecto", "dale", "un momento", "ya voy", "ahora te aviso", "claro", "por supuesto", "cómo no", "ajá", "ya te digo", "espérate")
    val acknowledgements = listOf("gracias", "muchas gracias", "te agradezco", "ok gracias", "perfecto, gracias")
    val waiting = listOf("espera", "un seg", "un segundo", "ahora", "déjame ver", "consulto", "busco")
    fun getRandom(): String = listOf(affirmatives, acknowledgements, waiting).flatten().random()
}

object BotPhrases {
    val phrases = listOf("Como modelo de lenguaje", "Como modelo de IA", "Soy un asistente de IA", "I am an AI", "I am a language model", "Based on my training", "Mi entrenamiento", "Fue entrenado para", "Puedo ayudarte con", "Estoy diseñado para", "Tengo la capacidad de", "Gracias por tu pregunta", "Espero que esta respuesta", "Si tienes más preguntas")
}
