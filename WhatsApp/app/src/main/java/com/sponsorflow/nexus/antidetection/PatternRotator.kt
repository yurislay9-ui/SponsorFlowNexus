package com.sponsorflow.nexus.antidetection

object PatternRotator {

    private val greetings = listOf("Hola", "Hey", "Qué tal", "Buenos días", "Buenas")

    fun rotate(): String = "pattern"

    fun formatResponse(text: String): String {
        return text
    }

    fun rotateGreeting(): String {
        return greetings.random()
    }

    fun rotateEnding(): String {
        val endings = listOf("¿Hay algo más en lo que pueda ayudar?", "¡Estoy aquí para lo que necesites!", "Pregúntame lo que quieras.", "Saludos!")
        return endings.random()
    }
}
