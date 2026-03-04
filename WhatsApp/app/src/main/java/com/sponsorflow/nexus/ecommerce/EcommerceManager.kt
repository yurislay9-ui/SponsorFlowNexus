/*
 * E-Commerce Manager (Compact)
 */
package com.sponsorflow.nexus.ecommerce

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap

class EcommerceManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("nexus_ecommerce", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val products = ConcurrentHashMap<String, Product>()
    private val carts = ConcurrentHashMap<String, Cart>()
    private val orders = ConcurrentHashMap<String, Order>()

    fun addProduct(product: Product): Boolean { products[product.id] = product; return true }
    fun getProduct(id: String): Product? = products[id]
    fun getProducts(): List<Product> = products.values.filter { it.isActive }.toList()
    fun addToCart(phone: String, productId: String, qty: Int = 1): String {
        val product = products[productId] ?: return "Product not found"
        val cart = carts.getOrPut(phone) { Cart(phone) }
        cart.items.add(CartItem(productId, product.name, qty, product.price, null, product.price * qty))
        return "Added to cart"
    }
    fun getCart(phone: String): Cart? = carts[phone]
    fun createOrder(phone: String): Order? {
        val cart = carts[phone] ?: return null
        val order = Order("ORD_${System.currentTimeMillis()}", phone, null, cart.items.toList(), cart.getTotal(), "USD", OrderStatus.PENDING, null, null, null, null, System.currentTimeMillis(), System.currentTimeMillis(), null, null, null)
        orders[order.id] = order
        carts.remove(phone)
        return order
    }
    fun loadFromPrefs() { /* Load from prefs */ }
}
