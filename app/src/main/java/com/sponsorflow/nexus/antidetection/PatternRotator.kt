/*
 * SponsorFlow Nexus v2.4 - Pattern Rotator
 * CORREGIDO: Thread-safe con AtomicInteger
 */
package com.sponsorflow.nexus.antidetection

import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

object PatternRotator {

    private val greetings = listOf(
        "Hola", "Buenos días", "Buenas tardes", "Buenas noches",
        "¡Hola!", "¿Qué tal?", "Saludos", "Bienvenido"
    )
    
    private val closings = listOf(
        "Saludos", "Gracias por contactarnos", "Estamos para servirte",
        "Cuídate", "Hasta pronto", "¡Que tengas un buen día!"
    )
    
    private val acknowledgments = listOf(
        "Entendido", "Perfecto", "De acuerdo", "Claro",
        "Comprendo", "Vale", "Perfecto, ya lo tengo"
    )

    // CORREGIDO: Thread-safe con AtomicInteger
    private val lastGreetingIndex = AtomicInteger(-1)
    private val lastClosingIndex = AtomicInteger(-1)
    private val lastAckIndex = AtomicInteger(-1)

    fun getRandomGreeting(): String {
        if (greetings.size <= 1) return greetings.getOrElse(0) { "" }
        var index: Int
        do { index = Random.nextInt(greetings.size) } while (index == lastGreetingIndex.get())
        lastGreetingIndex.set(index)
        return greetings[index]
    }

    fun getRandomClosing(): String {
        if (closings.size <= 1) return closings.getOrElse(0) { "" }
        var index: Int
        do { index = Random.nextInt(closings.size) } while (index == lastClosingIndex.get())
        lastClosingIndex.set(index)
        return closings[index]
    }

    fun getRandomAcknowledgment(): String {
        if (acknowledgments.size <= 1) return acknowledgments.getOrElse(0) { "" }
        var index: Int
        do { index = Random.nextInt(acknowledgments.size) } while (index == lastAckIndex.get())
        lastAckIndex.set(index)
        return acknowledgments[index]
    }

    fun formatResponse(text: String): String {
        return text.trim()
            .replace(Regex("\\s+"), " ")
            .let { if (Random.nextFloat() < 0.3f) it.lowercase() else it }
    }
}
