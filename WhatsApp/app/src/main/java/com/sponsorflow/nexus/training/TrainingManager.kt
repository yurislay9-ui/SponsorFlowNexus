/*
 * SponsorFlow Nexus - AI Training Manager
 * Sistema de entrenamiento personalizado para cada cliente
 * Permite enseñar a la IA según el nicho y costumbres del negocio
 */
package com.sponsorflow.nexus.training

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

// ==================== MODELOS DE ENTRENAMIENTO ====================

/**
 * Categoría de entrenamiento
 */
data class TrainingCategory(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val isRequired: Boolean = false
)

/**
 * Item de entrenamiento (una lección)
 */
data class TrainingItem(
    val id: String,
    val categoryId: String,
    val title: String,
    val content: String,
    val keywords: List<String> = emptyList(),
    val examples: List<String> = emptyList(),
    val priority: Int = 0,
    val isActive: Boolean = true
)

/**
 * Configuración de entrenamiento para un cliente
 */
data class ClientTraining(
    val phone: String,
    val businessName: String,
    val businessType: String, // nicho:电商,餐厅,技术支持, etc.
    val tone: String, // formal, casual, profesional, amigable
    val language: String = "es",
    val trainingItems: List<TrainingItem> = emptyList(),
    val customRules: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val faq: List<QAPair> = emptyList(),
    val products: List<ProductInfo> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Pregunta frecuente
 */
data class QAPair(
    val id: String,
    val question: String,
    val answer: String,
    val keywords: List<String> = emptyList(),
    val category: String = "general"
)

/**
 * Información de producto para la IA
 */
data class ProductInfo(
    val id: String,
    val name: String,
    val description: String,
    val price: Double?,
    val features: List<String> = emptyList(),
    val keywords: List<String> = emptyList()
)

/**
 * Plantilla de entrenamiento
 */
data class TrainingTemplate(
    val id: String,
    val name: String,
    val businessType: String,
    val description: String,
    val categories: List<String>,
    val defaultItems: List<TrainingItem>
)

/**
 * Prompt generado automáticamente
 */
data class GeneratedPrompt(
    val fullPrompt: String,
    val systemPrompt: String,
    val contextPrompt: String,
    val examples: List<String>,
    val keywords: List<String>
)

// ==================== MANAGER ====================

class TrainingManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("nexus_training", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Entrenamiento por cliente
    private val clientTrainings = ConcurrentHashMap<String, ClientTraining>()

    // Plantillas disponibles
    private val templates = mutableListOf<TrainingTemplate>()

    companion object {
        private const val PREF_CLIENTS = "client_trainings"

        // Categorías de entrenamiento
        val DEFAULT_CATEGORIES = listOf(
            TrainingCategory(
                id = "business",
                name = "Información del Negocio",
                description = "Datos básicos de tu empresa",
                icon = "🏢",
                isRequired = true
            ),
            TrainingCategory(
                id = "products",
                name = "Productos y Servicios",
                description = "Qué vendes y cómo lo describís",
                icon = "📦"
            ),
            TrainingCategory(
                id = "responses",
                name = "Respuestas Predefinidas",
                description = "Cómo responder a situaciones comunes",
                icon = "💬"
            ),
            TrainingCategory(
                id = "keywords",
                name = "Palabras Clave",
                description = "Términos importantes de tu nicho",
                icon = "🔑"
            ),
            TrainingCategory(
                id = "faq",
                name = "Preguntas Frecuentes",
                description = "Preguntas y respuestas comunes",
                icon = "❓"
            ),
            TrainingCategory(
                id = "tone",
                name = "Tono y Estilo",
                description = "Cómo debe sonar la IA",
                icon = "🎭"
            ),
            TrainingCategory(
                id = "rules",
                name = "Reglas Personalizadas",
                description = "Normas específicas de tu negocio",
                icon = "📋"
            ),
            TrainingCategory(
                id = "objections",
                name = "Objectiones Comunes",
                description = "Cómo manejar objeciones",
                icon = "🛡️"
            ),
            TrainingCategory(
                id = "closing",
                name = "Cierre de Ventas",
                description = "Técnicas para cerrar",
                icon = "🤝"
            ),
            TrainingCategory(
                id = "forbidden",
                name = "Temas Prohibidos",
                description = "Lo que la IA NO debe decir",
                icon = "🚫"
            )
        )

        // Tipos de negocio
        val BUSINESS_TYPES = listOf(
            "ecommerce" to "Tienda Online",
            "restaurant" to "Restaurante",
            "support" to "Soporte Técnico",
            "realestate" to "Bienes Raíces",
            "health" to "Salud/Belleza",
            "education" to "Educación",
            "finance" to "Finanzas",
            "automotive" to "Automotriz",
            "food" to "Comida/Bebida",
            "retail" to "Tienda Física",
            "services" to "Servicios Profesionales",
            "other" to "Otro"
        )

        // Tonos disponibles
        val TONES = listOf(
            "formal" to "Formal - Profesional y serio",
            "casual" to "Casual - Amigable y relajado",
            "friendly" to "Amigable - Cercano y cálido",
            "professional" to "Profesional - Directo y eficiente",
            "funny" to "Divertido - Con humor",
            "luxury" to "Lujo - Exclusivo y elegante"
        )
    }

    // ==================== GESTIÓN DE ENTRENAMIENTO ====================

    /**
     * Crear entrenamiento para cliente
     */
    fun createClientTraining(
        phone: String,
        businessName: String,
        businessType: String,
        tone: String
    ): ClientTraining {
        val training = ClientTraining(
            phone = phone,
            businessName = businessName,
            businessType = businessType,
            tone = tone
        )

        // Cargar plantilla según tipo de negocio
        val template = getTemplateForBusinessType(businessType)
        if (template != null) {
            clientTrainings[phone] = training.copy(
                trainingItems = template.defaultItems
            )
        } else {
            clientTrainings[phone] = training
        }

        saveToPrefs()
        return clientTrainings[phone]!!
    }

    /**
     * Obtener entrenamiento de cliente
     */
    fun getClientTraining(phone: String): ClientTraining? = clientTrainings[phone]

    /**
     * Actualizar información del negocio
     */
    fun updateBusinessInfo(
        phone: String,
        businessName: String,
        businessType: String,
        tone: String
    ): Boolean {
        val current = clientTrainings[phone] ?: return false
        clientTrainings[phone] = current.copy(
            businessName = businessName,
            businessType = businessType,
            tone = tone,
            updatedAt = System.currentTimeMillis()
        )
        saveToPrefs()
        return true
    }

    /**
     * Agregar item de entrenamiento
     */
    fun addTrainingItem(phone: String, item: TrainingItem): Boolean {
        val current = clientTrainings[phone] ?: return false
        val items = current.trainingItems.toMutableList()
        items.add(item)
        clientTrainings[phone] = current.copy(
            trainingItems = items,
            updatedAt = System.currentTimeMillis()
        )
        saveToPrefs()
        return true
    }

    /**
     * Actualizar item de entrenamiento
     */
    fun updateTrainingItem(phone: String, item: TrainingItem): Boolean {
        val current = clientTrainings[phone] ?: return false
        val items = current.trainingItems.map {
            if (it.id == item.id) item else it
        }
        clientTrainings[phone] = current.copy(
            trainingItems = items,
            updatedAt = System.currentTimeMillis()
        )
        saveToPrefs()
        return true
    }

    /**
     * Eliminar item de entrenamiento
     */
    fun deleteTrainingItem(phone: String, itemId: String): Boolean {
        val current = clientTrainings[phone] ?: return false
        val items = current.trainingItems.filter { it.id != itemId }
        clientTrainings[phone] = current.copy(
            trainingItems = items,
            updatedAt = System.currentTimeMillis()
        )
        saveToPrefs()
        return true
    }

    /**
     * Agregar FAQ
     */
    fun addFAQ(phone: String, question: String, answer: String, keywords: List<String> = emptyList()): Boolean {
        val current = clientTrainings[phone] ?: return false
        val faq = current.faq.toMutableList()
        faq.add(QAPair(
            id = "faq_${System.currentTimeMillis()}",
            question = question,
            answer = answer,
            keywords = keywords
        ))
        clientTrainings[phone] = current.copy(
            faq = faq,
            updatedAt = System.currentTimeMillis()
        )
        saveToPrefs()
        return true
    }

    /**
     * Agregar regla personalizada
     */
    fun addCustomRule(phone: String, rule: String): Boolean {
        val current = clientTrainings[phone] ?: return false
        val rules = current.customRules.toMutableList()
        rules.add(rule)
        clientTrainings[phone] = current.copy(
            customRules = rules,
            updatedAt = System.currentTimeMillis()
        )
        saveToPrefs()
        return true
    }

    /**
     * Agregar palabra clave
     */
    fun addKeyword(phone: String, keyword: String): Boolean {
        val current = clientTrainings[phone] ?: return false
        if (keyword in current.keywords) return true
        val keywords = current.keywords + keyword
        clientTrainings[phone] = current.copy(
            keywords = keywords,
            updatedAt = System.currentTimeMillis()
        )
        saveToPrefs()
        return true
    }

    /**
     * Agregar producto
     */
    fun addProduct(phone: String, product: ProductInfo): Boolean {
        val current = clientTrainings[phone] ?: return false
        val products = current.products.toMutableList()
        products.add(product)
        clientTrainings[phone] = current.copy(
            products = products,
            updatedAt = System.currentTimeMillis()
        )
        saveToPrefs()
        return true
    }

    // ==================== GENERACIÓN DE PROMPT ====================

    /**
     * Generar prompt completo para la IA
     */
    fun generatePrompt(phone: String): GeneratedPrompt? {
        val training = clientTrainings[phone] ?: return null

        val systemPrompt = buildSystemPrompt(training)
        val contextPrompt = buildContextPrompt(training)
        val examples = buildExamples(training)
        val keywords = extractKeywords(training)

        val fullPrompt = buildFullPrompt(systemPrompt, contextPrompt, examples)

        return GeneratedPrompt(
            fullPrompt = fullPrompt,
            systemPrompt = systemPrompt,
            contextPrompt = contextPrompt,
            examples = examples,
            keywords = keywords
        )
    }

    /**
     * Construir system prompt
     */
    private fun buildSystemPrompt(training: ClientTraining): String {
        val toneDescription = when (training.tone) {
            "formal" -> "Sé formal, profesional y respetuoso en todo momento."
            "casual" -> "Sé casual, amigable y relajado. Usa un tono cercano."
            "friendly" -> "Sé muy amigable y cálido. Haz que el cliente se sienta cómodo."
            "professional" -> "Sé profesional, directo y eficiente. Ve al grano."
            "funny" -> "Sé divertido pero profesional. Usa humor ligero."
            "luxury" -> "Sé exclusivo y elegante. Trata al cliente como VIP."
            else -> "Sé profesional y amable."
        }

        return """
Eres ${training.businessName}, un asistente de ventas especializado en el nicho de ${getBusinessTypeName(training.businessType)}.
Tu tono debe ser: $toneDescription

IMPORTANTE:
- Siempre representa a ${training.businessName}
- Nunca reveles que eres una IA (actúas como un vendedor humano)
- Prioriza ayudar al cliente y cerrar ventas
- Mantén las respuestas concisas pero informativas
- Usa emojis con moderación según el tono
        """.trimIndent()
    }

    /**
     * Construir context prompt
     */
    private fun buildContextPrompt(training: ClientTraining): String {
        val sb = StringBuilder()

        // Productos
        if (training.products.isNotEmpty()) {
            sb.append("\n📦 PRODUCTOS/SERVICIOS:\n")
            training.products.forEach { product ->
                sb.append("- ${product.name}: ${product.description}")
                product.price?.let { sb.append(" ($$it)") }
                sb.append("\n")
            }
        }

        // FAQ
        if (training.faq.isNotEmpty()) {
            sb.append("\n❓ PREGUNTAS FRECUENTES:\n")
            training.faq.forEach { qa ->
                sb.append("P: ${qa.question}\n")
                sb.append("R: ${qa.answer}\n")
            }
        }

        // Reglas personalizadas
        if (training.customRules.isNotEmpty()) {
            sb.append("\n📋 REGLAS DEL NEGOCIO:\n")
            training.customRules.forEach { rule ->
                sb.append("- $rule\n")
            }
        }

        // Objections (cómo manejarlas)
        val objectionItems = training.trainingItems.filter { it.categoryId == "objections" }
        if (objectionItems.isNotEmpty()) {
            sb.append("\n🛡️ CÓMO MANEJAR OBJECTIONES:\n")
            objectionItems.forEach { item ->
                sb.append("- ${item.title}: ${item.content}\n")
            }
        }

        // Palabras clave
        if (training.keywords.isNotEmpty()) {
            sb.append("\n🔑 PALABRAS CLAVE DEL NEGOCIO:\n")
            sb.append(training.keywords.joinToString(", "))
            sb.append("\n")
        }

        return sb.toString()
    }

    /**
     * Construir ejemplos
     */
    private fun buildExamples(training: ClientTraining): List<String> {
        val examples = mutableListOf<String>()

        training.trainingItems
            .filter { it.examples.isNotEmpty() }
            .forEach { item ->
                examples.addAll(item.examples)
            }

        return examples.take(10) // Máximo 10 ejemplos
    }

    /**
     * Extraer todas las keywords
     */
    private fun extractKeywords(training: ClientTraining): List<String> {
        val allKeywords = mutableListOf<String>()

        // Keywords del negocio
        allKeywords.addAll(training.keywords)

        // Keywords de productos
        training.products.forEach { product ->
            allKeywords.addAll(product.keywords)
        }

        // Keywords de FAQ
        training.faq.forEach { qa ->
            allKeywords.addAll(qa.keywords)
        }

        // Keywords de items
        training.trainingItems.forEach { item ->
            allKeywords.addAll(item.keywords)
        }

        return allKeywords.distinct()
    }

    /**
     * Construir prompt completo
     */
    private fun buildFullPrompt(system: String, context: String, examples: List<String>): String {
        val sb = StringBuilder()

        sb.append(system)
        sb.append("\n")
        sb.append(context)

        if (examples.isNotEmpty()) {
            sb.append("\n📝 EJEMPLOS DE CONVERSACIÓN:\n")
            examples.forEach { example ->
                sb.append("- $example\n")
            }
        }

        sb.append("\n🎯 INSTRUCCIONES FINALES:\n")
        sb.append("- Usa el contexto acima para responder\n")
        sb.append("- Adapta las respuestas al tono de ${"{}"}\n")
        sb.append("- Si no sabes algo, pregunta al cliente\n")
        sb.append("- Siempre busca cerrar la venta o agendar seguimiento")

        return sb.toString()
    }

    /**
     * Obtener nombre del tipo de negocio
     */
    private fun getBusinessTypeName(type: String): String {
        return BUSINESS_TYPES.find { it.first == type }?.second ?: type
    }

    // ==================== PLANTILLAS ====================

    /**
     * Obtener plantilla para tipo de negocio
     */
    private fun getTemplateForBusinessType(type: String): TrainingTemplate? {
        return templates.find { it.businessType == type }
    }

    /**
     * Agregar plantilla
     */
    fun addTemplate(template: TrainingTemplate) {
        templates.add(template)
    }

    /**
     * Obtener todas las plantillas
     */
    fun getTemplates(): List<TrainingTemplate> = templates.toList()

    /**
     * Inicializar plantillas por defecto
     */
    fun initializeDefaultTemplates() {
        templates.clear()
        templates.addAll(listOf(
            createEcommerceTemplate(),
            createRestaurantTemplate(),
            createSupportTemplate(),
            createRealEstateTemplate()
        ))
    }

    private fun createEcommerceTemplate() = TrainingTemplate(
        id = "ecommerce",
        name = "Tienda Online",
        businessType = "ecommerce",
        description = "Para tiendas que venden productos online",
        categories = listOf("business", "products", "responses", "faq", "tone", "closing"),
        defaultItems = listOf(
            TrainingItem("e1", "responses", "Saludo inicial", "¡Hola! Bienvenido a ${0}. ¿En qué puedo ayudarte hoy?", listOf("hola", "buenos", "hola")),
            TrainingItem("e2", "responses", "Consultar producto", "Por supuesto, tenemos ese producto disponible. ¿Qué color/talla prefieres?", listOf("tienen", "tienen", "disponible")),
            TrainingItem("e3", "responses", "Precio", "El precio es $${0}. ¿Te gustaría proceder con la compra?", listOf("cuánto", "precio", "cuesta")),
            TrainingItem("e4", "objections", "Está muy caro", "Entiendo tu preocupación. Podemos ofrecerte un descuento del 10% por ser tu primera compra.", listOf("caro", " caro", "mucho")),
            TrainingItem("e5", "closing", "Cerrar venta", "Perfecto, procedamos con tu pedido. ¿Qué método de pago prefieres?", listOf("sí", "comprar", "proceder"))
        )
    )

    private fun createRestaurantTemplate() = TrainingTemplate(
        id = "restaurant",
        name = "Restaurante",
        businessType = "restaurant",
        description = "Para restaurantes y delivery",
        categories = listOf("business", "products", "responses", "faq", "tone"),
        defaultItems = listOf(
            TrainingItem("r1", "responses", "Saludo", "¡Bienvenido a ${0}! ¿Tienes reserva o es para delivery?", listOf("hola", "buenos")),
            TrainingItem("r2", "responses", "Menú", "Tenemos un menú muy variada. ¿Prefieres carnes, pescados o opciones vegetarianas?", listOf("menú", "comida", "qué hay")),
            TrainingItem("r3", "responses", "Reserva", "Perfecto, ¿para cuántas personas y a qué hora?", listOf("reserva", "reservar")),
            TrainingItem("r4", "faq", "Horarios", "Abrimos de martes a domingo, de 12pm a 10pm.", listOf("horario", "abren", "hora"))
        )
    )

    private fun createSupportTemplate() = TrainingTemplate(
        id = "support",
        name = "Soporte Técnico",
        businessType = "support",
        description = "Para empresas de soporte técnico",
        categories = listOf("business", "responses", "faq", "objections"),
        defaultItems = listOf(
            TrainingItem("s1", "responses", "Saludo", "¡Hola! Soy el asistente de soporte de ${0}. ¿En qué puedo ayudarte?", listOf("hola", "ayuda", "problema")),
            TrainingItem("s2", "responses", "Pedir detalles", "Para ayudarte mejor, ¿podrías darme más detalles sobre el problema?", listOf("detalles", "explicar")),
            TrainingItem("s3", "responses", "Escalar", "Entiendo que es un tema complejo. Te voy a transferir con un técnico especializado.", listOf("técnico", "humano"))
        )
    )

    private fun createRealEstateTemplate() = TrainingTemplate(
        id = "realestate",
        name = "Bienes Raíces",
        businessType = "realestate",
        description = "Para agentes inmobiliarios",
        categories = listOf("business", "products", "responses", "faq", "tone"),
        defaultItems = listOf(
            TrainingItem("re1", "responses", "Saludo", "¡Hola! Soy ${0}, tu agente inmobiliario. ¿Buscas comprar o alquilar?", listOf("hola", "busco")),
            TrainingItem("re2", "responses", "Consultar propiedad", "Tengo varias opciones que podrían interesarte. ¿Cuál es tu presupuesto y zona preferida?", listOf("casa", "apartamento", "propiedad")),
            TrainingItem("re3", "responses", "Agendar visita", "Podemos agendar una visita. ¿Qué día y horario te conviene mejor?", listOf("visita", "ver"))
        )
    )

    // ==================== BÚSQUEDA ====================

    /**
     * Buscar en FAQ
     */
    fun searchFAQ(phone: String, query: String): QAPair? {
        val training = clientTrainings[phone] ?: return null
        val lowerQuery = query.lowercase()

        return training.faq.find { faq ->
            faq.question.lowercase().contains(lowerQuery) ||
            faq.answer.lowercase().contains(lowerQuery) ||
            faq.keywords.any { it.lowercase().contains(lowerQuery) }
        }
    }

    /**
     * Buscar producto
     */
    fun searchProduct(phone: String, query: String): ProductInfo? {
        val training = clientTrainings[phone] ?: return null
        val lowerQuery = query.lowercase()

        return training.products.find { product ->
            product.name.lowercase().contains(lowerQuery) ||
            product.description.lowercase().contains(lowerQuery) ||
            product.keywords.any { it.lowercase().contains(lowerQuery) }
        }
    }

    // ==================== PERSISTENCIA ====================

    private fun saveToPrefs() {
        val json = gson.toJson(clientTrainings)
        prefs.edit().putString(PREF_CLIENTS, json).apply()
    }

    fun loadFromPrefs() {
        val json = prefs.getString(PREF_CLIENTS, null)
        if (json != null) {
            val type = object : TypeToken<Map<String, ClientTraining>>() {}.type
            val loaded: Map<String, ClientTraining> = gson.fromJson(json, type)
            clientTrainings.putAll(loaded)
        }
        initializeDefaultTemplates()
    }

    // ==================== ESTADÍSTICAS ====================

    /**
     * Obtener estadísticas de entrenamiento
     */
    fun getTrainingStats(): TrainingStats {
        val totalClients = clientTrainings.size
        val totalItems = clientTrainings.values.sumOf { it.trainingItems.size }
        val totalFAQ = clientTrainings.values.sumOf { it.faq.size }
        val totalProducts = clientTrainings.values.sumOf { it.products.size }

        val businessTypeCount = clientTrainings.values
            .groupingBy { it.businessType }
            .eachCount()

        return TrainingStats(
            totalClients = totalClients,
            totalTrainingItems = totalItems,
            totalFAQs = totalFAQ,
            totalProducts = totalProducts,
            businessTypeDistribution = businessTypeCount
        )
    }
}

data class TrainingStats(
    val totalClients: Int,
    val totalTrainingItems: Int,
    val totalFAQs: Int,
    val totalProducts: Int,
    val businessTypeDistribution: Map<String, Int>
)
