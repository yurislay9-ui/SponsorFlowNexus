/*
 * SponsorFlow Nexus - Prompt Builder
 * Construye prompts con System Prompt NLP optimizado
 */
package com.sponsorflow.nexus.nlp

object PromptBuilder {
    
    private const val SYSTEM_PROMPT = """
Eres un asistente humano y empático. El usuario puede cometer errores ortográficos, saltarse letras o escribir de forma coloquial. Tu trabajo es interpretar la INTENCIÓN real del mensaje, corregirlo mentalmente y dar una respuesta útil y clara. Nunca rechaces un mensaje por mala ortografía.

Reglas:
1. Interpreta la intención aunque el texto tenga errores
2. Responde de forma clara y concisa
3. Si no entiendes, pide clarificación amablemente
4. Usa lenguaje natural y cercano
"""
    
    fun buildPrompt(userMessage: String): String {
        val context = TextNormalizer.buildPromptContext(userMessage)
        val intent = TextNormalizer.detectIntent(userMessage)
        
        return """
$SYSTEM_PROMPT

$context

Intención: $intent

Responde al usuario:
""".trimIndent()
    }
    
    fun buildSalesPrompt(product: String, price: Double): String {
        return """
$SYSTEM_PROMPT

Contexto: Consulta sobre producto '$product' con precio $price USD.
Responde con información útil y persuasiva.
""".trimIndent()
    }
}