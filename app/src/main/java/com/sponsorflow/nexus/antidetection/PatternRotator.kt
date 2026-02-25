/**
 * SponsorFlow Nexus v1.0 - Pattern Rotator
 * 
 * Componente encargado de la rotación de patrones de comportamiento para evitar
 * detección automatizada. Implementa un sistema de rotación pseudo-aleatoria
 * que ayuda a simular patrones de usuario humanos variados.
 * 
 * Este componente es crucial para la funcionalidad de anti-detección del sistema,
 * proporcionando diversidad en los patrones de comportamiento que pueden ser
 * monitoreados por sistemas de detección automatizada.
 * 
 * @author SponsorFlow Nexus Team
 * @version 1.0
 * @since 1.0
 */
package com.sponsorflow.nexus.antidetection

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger

/**
 * Objeto singleton que gestiona la rotación de patrones de comportamiento.
 * 
 * Proporciona métodos para obtener patrones de forma aleatoria y acceder
 * a la lista completa de patrones disponibles para la rotación.
 * 
 * El sistema utiliza ThreadLocalRandom para garantizar seguridad en entornos
 * multihilo y evita bloqueos de concurrencia mediante AtomicInteger.
 */
object PatternRotator {

    /**
     * Lista interna de saludos disponibles para la rotación.
     * 
     * Contiene una colección predefinida de saludos que serán utilizados
     * para simular diferentes formas de iniciar conversaciones o interacciones.
     * Cada saludo representa un patrón de comportamiento distinto.
     */
    private val greetings = listOf(
        "Hola", "Buenos días", "Buenas tardes", "Buenas noches",
        "¡Hola!", "¿Qué tal?", "Saludos", "Bienvenido"
    )
    
    /**
     * Lista interna de despedidas disponibles para la rotación.
     * 
     * Contiene una colección predefinida de despedidas que serán utilizadas
     * para simular diferentes formas de finalizar conversaciones o interacciones.
     * Cada despedida representa un patrón de comportamiento distinto.
     */
    private val closings = listOf(
        "Saludos", "Gracias por contactarnos", "Estamos para servirte",
        "Cuídate", "Hasta pronto", "¡Que tengas un buen día!"
    )
    
    /**
     * Lista interna de reconocimientos disponibles para la rotación.
     * 
     * Contiene una colección predefinida de reconocimientos que serán utilizados
     * para simular diferentes formas de responder o confirmar mensajes.
     * Cada reconocimiento representa un patrón de comportamiento distinto.
     */
    private val acknowledgments = listOf(
        "Entendido", "Perfecto", "De acuerdo", "Claro",
        "Comprendo", "Vale", "Perfecto, ya lo tengo"
    )

    /**
     * Índice atómico del último saludo utilizado.
     * 
     * Se utiliza para evitar la repetición inmediata del mismo saludo,
     * mejorando la naturalidad de las interacciones simuladas.
     */
    private val lastGreetingIndex = AtomicInteger(-1)
    
    /**
     * Índice atómico de la última despedida utilizada.
     * 
     * Se utiliza para evitar la repetición inmediata de la misma despedida,
     * mejorando la naturalidad de las interacciones simuladas.
     */
    private val lastClosingIndex = AtomicInteger(-1)
    
    /**
     * Índice atómico del último reconocimiento utilizado.
     * 
     * Se utiliza para evitar la repetición inmediata del mismo reconocimiento,
     * mejorando la naturalidad de las interacciones simuladas.
     */
    private val lastAckIndex = AtomicInteger(-1)

    /**
     * Obtiene un saludo aleatorio de la lista de saludos disponibles.
     * 
     * Este método implementa la lógica de rotación para saludos,
     * seleccionando aleatoriamente uno de los saludos predefinidos
     * mientras evita la repetición inmediata del mismo saludo.
     * 
     * @return String con el saludo seleccionado, o una cadena vacía
     *         si ocurre un error o la lista está vacía
     * 
     * @throws IllegalArgumentException si el índice generado es inválido
     * @throws IllegalStateException si el estado interno es inconsistente
     * @throws SecurityException si no se tienen permisos para acceder al índice
     * 
     * @see getRandomClosing
     * @see getRandomAcknowledgment
     * @see ThreadLocalRandom
     * @see AtomicInteger
     */
    fun getRandomGreeting(): String {
        return try {
            if (greetings.size <= 1) return greetings.getOrElse(0) { "" }
            var index: Int
            do { index = ThreadLocalRandom.current().nextInt(greetings.size) } while (index == lastGreetingIndex.get())
            lastGreetingIndex.set(index)
            greetings[index]
        } catch (e: IllegalArgumentException) {
            greetings.getOrElse(0) { "" }
        } catch (e: IllegalStateException) {
            greetings.getOrElse(0) { "" }
        } catch (e: SecurityException) {
            greetings.getOrElse(0) { "" }
        } catch (e: Exception) {
            greetings.getOrElse(0) { "" }
        }
    }

    /**
     * Obtiene una despedida aleatoria de la lista de despedidas disponibles.
     * 
     * Este método implementa la lógica de rotación para despedidas,
     * seleccionando aleatoriamente una de las despedidas predefinidas
     * mientras evita la repetición inmediata de la misma despedida.
     * 
     * @return String con la despedida seleccionada, o una cadena vacía
     *         si ocurre un error o la lista está vacía
     * 
     * @throws IllegalArgumentException si el índice generado es inválido
     * @throws IllegalStateException si el estado interno es inconsistente
     * 
     * @see getRandomGreeting
     * @see getRandomAcknowledgment
     * @see ThreadLocalRandom
     * @see AtomicInteger
     */
    fun getRandomClosing(): String {
        return try {
            if (closings.size <= 1) return closings.getOrElse(0) { "" }
            var index: Int
            do { index = ThreadLocalRandom.current().nextInt(closings.size) } while (index == lastClosingIndex.get())
            lastClosingIndex.set(index)
            closings[index]
        } catch (e: IllegalArgumentException) {
            closings.getOrElse(0) { "" }
        } catch (e: IllegalStateException) {
            closings.getOrElse(0) { "" }
        } catch (e: Exception) {
            closings.getOrElse(0) { "" }
        }
    }

    /**
     * Obtiene un reconocimiento aleatorio de la lista de reconocimientos disponibles.
     * 
     * Este método implementa la lógica de rotación para reconocimientos,
     * seleccionando aleatoriamente uno de los reconocimientos predefinidos
     * mientras evita la repetición inmediata del mismo reconocimiento.
     * 
     * @return String con el reconocimiento seleccionado, o una cadena vacía
     *         si ocurre un error o la lista está vacía
     * 
     * @throws IllegalArgumentException si el índice generado es inválido
     * @throws IllegalStateException si el estado interno es inconsistente
     * 
     * @see getRandomGreeting
     * @see getRandomClosing
     * @see ThreadLocalRandom
     * @see AtomicInteger
     */
    fun getRandomAcknowledgment(): String {
        return try {
            if (acknowledgments.size <= 1) return acknowledgments.getOrElse(0) { "" }
            var index: Int
            do { index = ThreadLocalRandom.current().nextInt(acknowledgments.size) } while (index == lastAckIndex.get())
            lastAckIndex.set(index)
            acknowledgments[index]
        } catch (e: IllegalArgumentException) {
            acknowledgments.getOrElse(0) { "" }
        } catch (e: IllegalStateException) {
            acknowledgments.getOrElse(0) { "" }
        } catch (e: Exception) {
            acknowledgments.getOrElse(0) { "" }
        }
    }

    /**
     * Formatea una respuesta de texto aplicando normalización y variaciones.
     * 
     * Este método realiza el formateo básico de textos de respuesta,
     * eliminando espacios extra y aplicando variaciones aleatorias
     * para simular comportamientos humanos más naturales.
     * 
     * @param text String que contiene el texto a formatear
     * @return String con el texto formateado, o el texto original
     *         si ocurre un error durante el procesamiento
     * 
     * @throws IllegalArgumentException si el texto de entrada es nulo
     * 
     * @see ThreadLocalRandom
     * @see Regex
     */
    fun formatResponse(text: String): String {
        return try {
            text.trim()
                .replace(Regex("\\s+"), " ")
                .let { if (ThreadLocalRandom.current().nextFloat() < 0.3f) it.lowercase() else it }
        } catch (e: IllegalArgumentException) {
            text
        } catch (e: Exception) {
            text
        }
    }
}
