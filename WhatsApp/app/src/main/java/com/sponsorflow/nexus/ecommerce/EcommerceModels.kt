/*
 * E-Commerce Models - Data classes and enums
 */
package com.sponsorflow.nexus.ecommerce

/**
 * Producto del catálogo
 */
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String = "USD",
    val imageUrl: String?,
    val category: String,
    val stock: Int,
    val variants: List<ProductVariant>? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Variante de producto (talla, color, etc.)
 */
data class ProductVariant(
    val name: String,
    val options: List<String>,
    val priceModifier: Double = 0.0
)

/**
 * Item en el carrito
 */
data class CartItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val variant: String? = null,
    val totalPrice: Double
)

/**
 * Carrito de compras
 */
data class Cart(
    val phone: String,
    val items: MutableList<CartItem> = mutableListOf(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getTotal(): Double = items.sumOf { it.totalPrice }
    fun getItemCount(): Int = items.sumOf { it.quantity }
}

/**
 * Orden de compra
 */
data class Order(
    val id: String,
    val phone: String,
    val customerName: String?,
    val items: List<CartItem>,
    val total: Double,
    val currency: String,
    val status: OrderStatus,
    val paymentMethod: String?,
    val paymentProof: String?,
    val shippingAddress: String?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val confirmedAt: Long?,
    val shippedAt: Long?,
    val deliveredAt: Long?
)

/**
 * Estado de la orden
 */
enum class OrderStatus {
    PENDING,      // Esperando pago
    CONFIRMED,    // Pago confirmado
    PROCESSING,   // Preparando envío
    SHIPPED,      // Enviado
    DELIVERED,    // Entregado
    CANCELLED,    // Cancelado
    REFUNDED      // Reembolsado
}

/**
 * Sesión de checkout
 */
data class CheckoutSession(
    val phone: String,
    val cart: Cart,
    val currentStep: CheckoutStep,
    val shippingAddress: String?,
    val paymentMethod: String?,
    val notes: String?,
    val startedAt: Long
)

/**
 * Paso del checkout
 */
enum class CheckoutStep {
    CART_REVIEW,      // Revisar carrito
    SHIPPING,         // Dirección de envío
    PAYMENT_METHOD,   // Método de pago
    CONFIRMATION,     // Confirmar pedido
    PAYMENT_PENDING,  // Esperando pago
    COMPLETED         // Completado
}

/**
 * Métricas de e-commerce
 */
data class EcommerceMetrics(
    val totalOrders: Int,
    val pendingOrders: Int,
    val completedOrders: Int,
    val totalRevenue: Double,
    val avgOrderValue: Double,
    val topProducts: List<String>
)
