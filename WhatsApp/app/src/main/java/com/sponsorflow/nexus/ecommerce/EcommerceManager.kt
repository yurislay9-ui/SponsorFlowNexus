/*
 * SponsorFlow Nexus - E-Commerce Manager
 * Carrito de compras, catálogo y pagos dentro de WhatsApp
 * SOLO VIP
 */
package com.sponsorflow.nexus.ecommerce

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

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

data class ProductVariant(
    val name: String,
    val options: List<String>,
    val priceModifier: Double = 0.0
)

data class CartItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val variant: String? = null,
    val totalPrice: Double
)

data class Cart(
    val phone: String,
    val items: MutableList<CartItem> = mutableListOf(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getTotal(): Double = items.sumOf { it.totalPrice }
    fun getItemCount(): Int = items.sumOf { it.quantity }
}

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

enum class OrderStatus {
    PENDING,      // Esperando pago
    CONFIRMED,    // Pago confirmado
    PROCESSING,   // Preparando envío
    SHIPPED,      // Enviado
    DELIVERED,    // Entregado
    CANCELLED,    // Cancelado
    REFUNDED      // Reembolsado
}

data class CheckoutSession(
    val phone: String,
    val cart: Cart,
    val currentStep: CheckoutStep,
    val shippingAddress: String?,
    val paymentMethod: String?,
    val notes: String?,
    val startedAt: Long
)

enum class CheckoutStep {
    CART_REVIEW,      // Revisar carrito
    SHIPPING,         // Dirección de envío
    PAYMENT_METHOD,   // Método de pago
    CONFIRMATION,     // Confirmar pedido
    PAYMENT_PENDING,  // Esperando pago
    COMPLETED         // Completado
}

class EcommerceManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("nexus_ecommerce", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Productos en memoria
    private val products = ConcurrentHashMap<String, Product>()

    // Carritos por teléfono
    private val carts = ConcurrentHashMap<String, Cart>()

    // Órdenes
    private val orders = ConcurrentHashMap<String, Order>()

    // Sesiones de checkout
    private val checkoutSessions = ConcurrentHashMap<String, CheckoutSession>()

    // Orden por teléfono
    private val ordersByPhone = ConcurrentHashMap<String, MutableList<String>>()

    companion object {
        private const val PREF_PRODUCTS = "products"
        private const val PREF_ORDERS = "orders"

        val PAYMENT_METHODS = listOf(
            "transferencia" to "Transferencia bancaria",
            "pago_movil" to "Pago Móvil",
            "zelle" to "Zelle",
            "crypto" to "Criptomonedas (USDT)",
            "efectivo" to "Efectivo contra entrega",
            "mercadopago" to "MercadoPago"
        )

        val CURRENCIES = mapOf(
            "USD" to "$",
            "EUR" to "€",
            "COP" to "$",
            "MXN" to "$",
            "ARS" to "$",
            "BRL" to "R$"
        )
    }

    // ==================== PRODUCTOS ====================

    /**
     * Agregar producto al catálogo
     */
    fun addProduct(product: Product): Boolean {
        products[product.id] = product
        saveProducts()
        return true
    }

    /**
     * Obtener producto por ID
     */
    fun getProduct(productId: String): Product? = products[productId]

    /**
     * Obtener todos los productos activos
     */
    fun getActiveProducts(): List<Product> = products.values.filter { it.isActive }

    /**
     * Obtener productos por categoría
     */
    fun getProductsByCategory(category: String): List<Product> =
        products.values.filter { it.isActive && it.category.equals(category, ignoreCase = true) }

    /**
     * Buscar productos
     */
    fun searchProducts(query: String): List<Product> {
        val lowerQuery = query.lowercase()
        return products.values.filter {
            it.isActive && (
                it.name.lowercase().contains(lowerQuery) ||
                it.description.lowercase().contains(lowerQuery) ||
                it.category.lowercase().contains(lowerQuery)
            )
        }
    }

    /**
     * Obtener categorías disponibles
     */
    fun getCategories(): List<String> = products.values.map { it.category }.distinct()

    /**
     * Actualizar stock
     */
    fun updateStock(productId: String, quantity: Int): Boolean {
        products[productId]?.let { product ->
            val newStock = product.stock - quantity
            if (newStock < 0) return false
            products[productId] = product.copy(stock = newStock)
            saveProducts()
            return true
        }
        return false
    }

    // ==================== CARRITO ====================

    /**
     * Agregar al carrito
     */
    fun addToCart(phone: String, productId: String, quantity: Int = 1, variant: String? = null): String {
        val product = products[productId] ?: return "Producto no encontrado"

        if (!product.isActive) return "Producto no disponible"
        if (product.stock < quantity) return "Stock insuficiente. Disponible: ${product.stock}"

        val cart = carts.getOrPut(phone) { Cart(phone) }

        // Verificar si ya existe
        val existingItem = cart.items.find {
            it.productId == productId && it.variant == variant
        }

        if (existingItem != null) {
            val newQuantity = existingItem.quantity + quantity
            if (product.stock < newQuantity) return "Stock insuficiente"
            cart.items.remove(existingItem)
            cart.items.add(existingItem.copy(
                quantity = newQuantity,
                totalPrice = product.price * newQuantity
            ))
        } else {
            cart.items.add(CartItem(
                productId = productId,
                productName = product.name,
                quantity = quantity,
                unitPrice = product.price,
                variant = variant,
                totalPrice = product.price * quantity
            ))
        }

        cart.updatedAt = System.currentTimeMillis()
        carts[phone] = cart

        return "✅ ${product.name} agregado al carrito"
    }

    /**
     * Remover del carrito
     */
    fun removeFromCart(phone: String, productId: String): Boolean {
        val cart = carts[phone] ?: return false
        val removed = cart.items.removeAll { it.productId == productId }
        if (removed) {
            cart.updatedAt = System.currentTimeMillis()
            saveCarts()
        }
        return removed
    }

    /**
     * Actualizar cantidad
     */
    fun updateCartQuantity(phone: String, productId: String, quantity: Int): Boolean {
        val cart = carts[phone] ?: return false

        if (quantity <= 0) {
            return removeFromCart(phone, productId)
        }

        val product = products[productId] ?: return false
        if (product.stock < quantity) return false

        cart.items.find { it.productId == productId }?.let { item ->
            cart.items.remove(item)
            cart.items.add(item.copy(
                quantity = quantity,
                totalPrice = item.unitPrice * quantity
            ))
            cart.updatedAt = System.currentTimeMillis()
            saveCarts()
            return true
        }

        return false
    }

    /**
     * Obtener carrito
     */
    fun getCart(phone: String): Cart? = carts[phone]

    /**
     * Verificar si hay carrito
     */
    fun hasCart(phone: String): Boolean {
        val cart = carts[phone] ?: return false
        return cart.items.isNotEmpty()
    }

    /**
     * Limpiar carrito
     */
    fun clearCart(phone: String) {
        carts.remove(phone)
        saveCarts()
    }

    // ==================== CHECKOUT ====================

    /**
     * Iniciar checkout
     */
    fun startCheckout(phone: String): CheckoutSession? {
        val cart = carts[phone] ?: return null
        if (cart.items.isEmpty()) return null

        val session = CheckoutSession(
            phone = phone,
            cart = cart,
            currentStep = CheckoutStep.CART_REVIEW,
            shippingAddress = null,
            paymentMethod = null,
            notes = null,
            startedAt = System.currentTimeMillis()
        )

        checkoutSessions[phone] = session
        return session
    }

    /**
     * Actualizar paso de checkout
     */
    fun updateCheckoutStep(phone: String, step: CheckoutStep, data: Map<String, Any>? = null): CheckoutSession? {
        val session = checkoutSessions[phone] ?: return null

        val updated = when (step) {
            CheckoutStep.SHIPPING -> session.copy(
                shippingAddress = data?.get("address") as? String
            )
            CheckoutStep.PAYMENT_METHOD -> session.copy(
                paymentMethod = data?.get("method") as? String
            )
            CheckoutStep.CONFIRMATION -> session.copy(
                notes = data?.get("notes") as? String
            )
            else -> session
        }.copy(currentStep = step)

        checkoutSessions[phone] = updated
        return updated
    }

    /**
     * Completar checkout - crear orden
     */
    fun completeCheckout(phone: String): Order? {
        val session = checkoutSessions[phone] ?: return null
        val cart = session.cart

        if (cart.items.isEmpty()) return null

        val orderId = "ORD-${System.currentTimeMillis()}"
        val order = Order(
            id = orderId,
            phone = phone,
            customerName = null,
            items = cart.items.toList(),
            total = cart.getTotal(),
            currency = "USD",
            status = OrderStatus.PENDING,
            paymentMethod = session.paymentMethod,
            paymentProof = null,
            shippingAddress = session.shippingAddress,
            notes = session.notes,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            confirmedAt = null,
            shippedAt = null,
            deliveredAt = null
        )

        orders[orderId] = order
        ordersByPhone.getOrPut(phone) { mutableListOf() }.add(orderId)

        // Limpiar carrito y sesión
        clearCart(phone)
        checkoutSessions.remove(phone)

        saveOrders()
        return order
    }

    /**
     * Obtener sesión de checkout
     */
    fun getCheckoutSession(phone: String): CheckoutSession? = checkoutSessions[phone]

    // ==================== ÓRDENES ====================

    /**
     * Confirmar pago de orden
     */
    fun confirmOrder(orderId: String, paymentProof: String? = null): Boolean {
        orders[orderId]?.let { order ->
            orders[orderId] = order.copy(
                status = OrderStatus.CONFIRMED,
                paymentProof = paymentProof,
                confirmedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            saveOrders()
            return true
        }
        return false
    }

    /**
     * Actualizar estado de orden
     */
    fun updateOrderStatus(orderId: String, status: OrderStatus): Boolean {
        orders[orderId]?.let { order ->
            val updated = when (status) {
                OrderStatus.SHIPPED -> order.copy(
                    shippedAt = System.currentTimeMillis()
                )
                OrderStatus.DELIVERED -> order.copy(
                    deliveredAt = System.currentTimeMillis()
                )
                else -> order
            }

            orders[orderId] = updated.copy(
                status = status,
                updatedAt = System.currentTimeMillis()
            )
            saveOrders()
            return true
        }
        return false
    }

    /**
     * Obtener orden por ID
     */
    fun getOrder(orderId: String): Order? = orders[orderId]

    /**
     * Obtener órdenes del cliente
     */
    fun getCustomerOrders(phone: String): List<Order> {
        return ordersByPhone[phone]?.mapNotNull { orders[it] }?.sortedByDescending { it.createdAt }
            ?: emptyList()
    }

    /**
     * Obtener última orden pendiente
     */
    fun getPendingOrder(phone: String): Order? {
        return ordersByPhone[phone]?.mapNotNull { orders[it] }
            ?.find { it.status == OrderStatus.PENDING }
    }

    // ==================== MÉTODOS DE PAGO ====================

    /**
     * Obtener información de pago según método
     */
    fun getPaymentInfo(method: String): Map<String, String> {
        return when (method) {
            "transferencia" -> mapOf(
                "title" to "Transferencia Bancaria",
                "instructions" to "Realiza la transferencia y envía el comprobante",
                "fields" to "Nombre, Banco, Número de referencia"
            )
            "pago_movil" to mapOf(
                "title" to "Pago Móvil",
                "instructions" to "Usa Pago Móvil y comparte el comprobante",
                "fields" to "Teléfono, Banco, Referencia"
            )
            "zelle" to mapOf(
                "title" to "Zelle",
                "instructions" to "Envía el pago via Zelle",
                "fields" to "Email Zelle"
            )
            "crypto" to mapOf(
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

    // ==================== GENERAR MENSAJES ====================

    /**
     * Generar mensaje con catálogo de productos
     */
    fun generateCatalogMessage(products: List<Product>): String {
        if (products.isEmpty()) return "📦 No hay productos disponibles"

        val sb = StringBuilder()
        sb.append("📦 *CATÁLOGO DE PRODUCTOS*\n\n")

        products.forEachIndexed { index, product ->
            val emoji = if (product.stock > 0) "✅" else "❌"
            sb.append("${index + 1}. ${product.name}\n")
            sb.append("   💰 $${String.format("%.2f", product.price)}\n")
            sb.append("   📦 $emoji Stock: ${product.stock}\n")
            sb.append("   📝 ${product.description.take(50)}...\n\n")
        }

        sb.append("\n✏️ Escribe el número para comprar")
        return sb.toString()
    }

    /**
     * Generar mensaje del carrito
     */
    fun generateCartMessage(cart: Cart, phone: String): String {
        if (cart.items.isEmpty()) return "🛒 Tu carrito está vacío"

        val sb = StringBuilder()
        sb.append("🛒 *TU CARRITO*\n\n")

        cart.items.forEachIndexed { index, item ->
            sb.append("${index + 1}. ${item.productName}")
            if (item.variant != null) sb.append(" (${item.variant})")
            sb.append("\n")
            sb.append("   Cantidad: ${item.quantity} x $${String.format("%.2f", item.unitPrice)}\n")
            sb.append("   Subtotal: $${String.format("%.2f", item.totalPrice)}\n\n")
        }

        sb.append("─────────────────────\n")
        sb.append("💵 *TOTAL: $${String.format("%.2f", cart.getTotal())}*\n")
        sb.append("📦 Items: ${cart.getItemCount()}\n\n")

        sb.append("¿Qué haces?\n")
        sb.append("✅ - Comprar ahora\n")
        sb.append("➕ - Agregar más productos\n")
        sb.append("❌ - Vaciar carrito")

        return sb.toString()
    }

    /**
     * Generar mensaje de orden
     */
    fun generateOrderMessage(order: Order): String {
        val statusEmoji = when (order.status) {
            OrderStatus.PENDING -> "⏳"
            OrderStatus.CONFIRMED -> "✅"
            OrderStatus.PROCESSING -> "📦"
            OrderStatus.SHIPPED -> "🚚"
            OrderStatus.DELIVERED -> "🎉"
            OrderStatus.CANCELLED -> "❌"
            OrderStatus.REFUNDED -> "💸"
        }

        val sb = StringBuilder()
        sb.append("🎫 *PEDIDO #${order.id.takeLast(8)}*\n\n")
        sb.append("Estado: $statusEmoji ${order.status.name}\n\n")

        sb.append("📦 *Productos:*\n")
        order.items.forEach { item ->
            sb.append("• ${item.productName}")
            if (item.variant != null) sb.append(" (${item.variant})")
            sb.append("\n")
            sb.append("  ${item.quantity} x $${String.format("%.2f", item.unitPrice)} = $${String.format("%.2f", item.totalPrice)}\n")
        }

        sb.append("\n─────────────────────\n")
        sb.append("💵 *TOTAL: $${String.format("%.2f", order.total)}*\n\n")

        if (order.status == OrderStatus.PENDING) {
            sb.append("📝 Para pagar, escribe: *PAGAR*")
        }

        return sb.toString()
    }

    // =================══ PERSISTENCIA ====================

    private fun saveProducts() {
        val json = gson.toJson(products)
        prefs.edit().putString(PREF_PRODUCTS, json).apply()
    }

    private fun saveOrders() {
        val json = gson.toJson(orders)
        prefs.edit().putString(PREF_ORDERS, json).apply()
    }

    private fun saveCarts() {
        val json = gson.toJson(carts)
        prefs.edit().putString("carts", json).apply()
    }

    fun loadFromPrefs() {
        val productsJson = prefs.getString(PREF_PRODUCTS, null)
        if (productsJson != null) {
            val loaded: Map<String, Product> = gson.fromJson(productsJson)
            products.putAll(loaded)
        }

        val ordersJson = prefs.getString(PREF_ORDERS, null)
        if (ordersJson != null) {
            val loaded: Map<String, Order> = gson.fromJson(ordersJson)
            orders.putAll(loaded)
        }

        val cartsJson = prefs.getString("carts", null)
        if (cartsJson != null) {
            val loaded: Map<String, Cart> = gson.fromJson(cartsJson)
            carts.putAll(loaded)
        }
    }

    // ==================== ESTADÍSTICAS ====================

    /**
     * Obtener métricas de e-commerce
     */
    fun getEcommerceMetrics(): EcommerceMetrics {
        val allOrders = orders.values.toList()
        val completedOrders = allOrders.filter {
            it.status == OrderStatus.CONFIRMED || it.status == OrderStatus.DELIVERED
        }

        return EcommerceMetrics(
            totalOrders = allOrders.size,
            pendingOrders = allOrders.count { it.status == OrderStatus.PENDING },
            completedOrders = completedOrders.size,
            totalRevenue = completedOrders.sumOf { it.total },
            avgOrderValue = if (completedOrders.isNotEmpty()) {
                completedOrders.sumOf { it.total } / completedOrders.size
            } else 0.0,
            topProducts = products.values.sortedByDescending { it.stock }.take(5).map { it.name }
        )
    }
}

data class EcommerceMetrics(
    val totalOrders: Int,
    val pendingOrders: Int,
    val completedOrders: Int,
    val totalRevenue: Double,
    val avgOrderValue: Double,
    val topProducts: List<String>
)
