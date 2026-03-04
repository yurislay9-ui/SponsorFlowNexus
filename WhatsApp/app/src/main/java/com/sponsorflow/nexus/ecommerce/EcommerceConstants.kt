/*
 * E-Commerce Constants
 */
package com.sponsorflow.nexus.ecommerce

/**
 * Constantes de e-commerce
 */
object EcommerceConstants {

    // SharedPreferences keys
    const val PREF_PRODUCTS = "products"
    const val PREF_ORDERS = "orders"
    const val PREF_CARTS = "carts"

    // Métodos de pago disponibles
    val PAYMENT_METHODS = listOf(
        "transferencia" to "Transferencia bancaria",
        "pago_movil" to "Pago Móvil",
        "zelle" to "Zelle",
        "crypto" to "Criptomonedas (USDT)",
        "efectivo" to "Efectivo contra entrega",
        "mercadopago" to "MercadoPago"
    )

    // Monedas soportadas
    val CURRENCIES = mapOf(
        "USD" to "$",
        "EUR" to "€",
        "COP" to "$",
        "MXN" to "$",
        "ARS" to "$",
        "BRL" to "R$"
    )

    // Instrucciones de pago por método
    fun getPaymentInfo(method: String): Map<String, String> {
        return when (method) {
            "transferencia" -> mapOf(
                "title" to "Transferencia Bancaria",
                "instructions" to "Realiza la transferencia y envía el comprobante",
                "fields" to "Nombre, Banco, Número de referencia"
            )
            "pago_movil" -> mapOf(
                "title" to "Pago Móvil",
                "instructions" to "Usa Pago Móvil y comparte el comprobante",
                "fields" to "Teléfono, Banco, Referencia"
            )
            "zelle" -> mapOf(
                "title" to "Zelle",
                "instructions" to "Envía el pago via Zelle",
                "fields" to "Email Zelle"
            )
            "crypto" -> mapOf(
                "title" to "Criptomonedas (USDT)",
                "instructions" to "Envía USDT a la siguiente dirección TRC20:",
                "address" to "TU_DIRECCION_USDT"
            )
            else -> mapOf(
                "title" to method,
                "instructions" to "Contacta al vendedor"
            )
        }
    }
}
