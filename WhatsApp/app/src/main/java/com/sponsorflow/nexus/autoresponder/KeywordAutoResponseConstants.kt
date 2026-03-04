/*
 * Keyword Auto-Response Constants - Default keywords
 */
package com.sponsorflow.nexus.autoresponder

/**
 * Constantes del sistema de autorespuesta
 */
object KeywordAutoResponseConstants {

    // SharedPreferences keys
    const val PREF_RULES = "auto_response_rules"
    const val PREF_CONFIG = "auto_response_config"
    const val PREF_USAGE = "keyword_usage"

    // Keywords por defecto (ventas profesionales)
    val DEFAULT_KEYWORDS = listOf(
        // Catálogo y productos
        KeywordResponse(
            id = "kw_catalog_1",
            keywords = listOf("catalogo", "catálogo", "catalogos", "ver productos", "productos"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.SEND_CATALOG,
            responseMessage = "Aquí tienes nuestro catálogo completo!",
            priority = 10
        ),
        KeywordResponse(
            id = "kw_catalog_2",
            keywords = listOf("tienen", "tienen disponibles", "qué tienen"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.SEND_MESSAGE,
            responseMessage = "Tenemos muchos productos disponibles! Te envío nuestro catálogo para que puedas ver todo.",
            priority = 5
        ),

        // Precios
        KeywordResponse(
            id = "kw_price_1",
            keywords = listOf("precio", "cuánto", "cuanto", "cuesta", "vale", "costo"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.SEND_PRICE_LIST,
            responseMessage = "Te envío nuestra lista de precios actual.",
            priority = 10
        ),
        KeywordResponse(
            id = "kw_price_2",
            keywords = listOf("cuánto sale", "cuanto sale", "precio de"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.SEND_MESSAGE,
            responseMessage = "Cuál producto te interesa? Así te paso el precio exacto.",
            priority = 8
        ),

        // Compra
        KeywordResponse(
            id = "kw_buy_1",
            keywords = listOf("comprar", "adquirir", "pedir", "ordenar", "quiero llevar"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.START_CHECKOUT,
            responseMessage = "Perfecto! Te ayudo con tu compra. Cuál producto/s querés?",
            priority = 10
        ),
        KeywordResponse(
            id = "kw_buy_2",
            keywords = listOf("ya lo quiero", "me lo llevo", "lo llevo"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.START_CHECKOUT,
            responseMessage = "Excelente elección! Te dirijo al checkout. Cuál es tu dirección de entrega?",
            priority = 10
        ),

        // Ayuda y soporte
        KeywordResponse(
            id = "kw_help_1",
            keywords = listOf("ayuda", "ayúdame", "socorro", "no entiendo"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.ESCALATE_HUMAN,
            responseMessage = "Un agente humano te va a ayudar en breve. Por favor, esperá un momento.",
            priority = 10
        ),
        KeywordResponse(
            id = "kw_help_2",
            keywords = listOf("hablar con", "contactar", "llamar"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.ESCALATE_HUMAN,
            responseMessage = "Te conecto con un agente para que te ayude personalmente.",
            priority = 10
        ),

        // Horarios
        KeywordResponse(
            id = "kw_hours_1",
            keywords = listOf("horario", "horarios", "abren", "cierran", "atención"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.SEND_WORKING_HOURS,
            responseMessage = "Nuestros horarios de atención son:\nLun-Vie: 9am a 6pm\nSáb: 9am a 2pm",
            priority = 8
        ),

        // Ubicación
        KeywordResponse(
            id = "kw_location_1",
            keywords = listOf("ubicación", "dirección", "donde están", "dónde están", "venir"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.SEND_LOCATION,
            responseMessage = "Nuestra dirección es: [DIRECCIÓN]. Te comparto la ubicación en el mapa.",
            priority = 8
        ),

        // Envío
        KeywordResponse(
            id = "kw_shipping_1",
            keywords = listOf("envío", "envio", "entrega", "demora", "llega"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.SEND_SHIPPING_INFO,
            responseMessage = "Los envíos tardan 3-5 días hábiles. Envíos gratis a partir de $50.",
            priority = 8
        ),

        // Pago
        KeywordResponse(
            id = "kw_payment_1",
            keywords = listOf("pagar", "pago", "métodos", "medios de pago", "transferencia"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.SEND_PAYMENT_INFO,
            responseMessage = "Aceptamos: Transferencia, MercadoPago, Tarjetas de crédito/débito",
            priority = 8
        ),

        // Descuento
        KeywordResponse(
            id = "kw_discount_1",
            keywords = listOf("descuento", "oferta", "promoción", "descuento"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.APPLY_DISCOUNT,
            discountCode = "BIENVENIDO10",
            discountPercent = 10,
            responseMessage = "Te aplico un 10% de descuento! Código: BIENVENIDO10",
            priority = 7
        ),

        // Stock
        KeywordResponse(
            id = "kw_stock_1",
            keywords = listOf("stock", "disponible", "hay"),
            matchType = KeywordMatchType.CONTAINS,
            actionType = KeywordActionType.SEND_MESSAGE,
            responseMessage = "Todos nuestros productos están disponibles! Consultame cuál te interesa.",
            priority = 6
        )
    )
}
