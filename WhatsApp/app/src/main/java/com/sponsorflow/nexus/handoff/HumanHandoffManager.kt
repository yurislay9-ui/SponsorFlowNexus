/*
 * SponsorFlow Nexus - Human Handoff Manager
 * Escala conversaciones de IA a agentes humanos
 * Cumple con anti-detección de WhatsApp
 */
package com.sponsorflow.nexus.handoff

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

data class HandoffTicket(
    val id: String,
    val phone: String,
    val customerName: String?,
    val lastMessage: String,
    val conversationSummary: String,
    val intent: String,
    val priority: HandoffPriority,
    val status: HandoffStatus,
    val assignedAgent: String?,
    val createdAt: Long,
    val assignedAt: Long?,
    val resolvedAt: Long?,
    val rating: Int?,
    val notes: String?
)

enum class HandoffPriority {
    LOW,      // Consultas generales
    MEDIUM,   // Problemas que la IA no puede resolver
    HIGH,     // Ventas importantes, clientes VIP
    URGENT    // Problemas técnicos, quejas
}

enum class HandoffStatus {
    PENDING,      // En cola esperando agente
    ASSIGNED,     // Asignado a un agente
    IN_PROGRESS,  // Agente atendiendo
    RESOLVED,     // Resuelto
    ESCALATED,    // Escalado a nivel superior
    CLOSED        // Cerrado
}

data class Agent(
    val id: String,
    val name: String,
    val status: AgentStatus,
    val activeTickets: Int,
    val maxTickets: Int,
    val specialties: List<String>,
    val notificationToken: String?
)

enum class AgentStatus {
    ONLINE,      // Disponible
    BUSY,        // Atendiendo
    AWAY,        // No disponible
    OFFLINE      // Desconectado
}

data class HandoffConfig(
    val maxRetriesAI: Int = 2,              // Intentos de la IA antes de escalar
    val fallbackToHumanKeywords: List<String> = listOf(
        "hablar con persona", "hablar con humano", "no funciona",
        "problema técnico", "error", "queja", "gerente", "dueño"
    ),
    val autoEscalateAfterMinutes: Int = 30, // Auto-escalar si la IA no puede después de 30 min
    val notifyAgentsOnNewTicket: Boolean = true,
    val allowCustomerRate: Boolean = true,
    val minRatingToImprove: Int = 3
)

class HumanHandoffManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("nexus_handoff", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Tickets en memoria
    private val tickets = ConcurrentHashMap<String, HandoffTicket>()
    private val ticketsByPhone = ConcurrentHashMap<String, MutableList<String>>()

    // Agentes disponibles
    private val agents = ConcurrentHashMap<String, Agent>()

    // Contadores
    private val ticketCounter = AtomicInteger(1)
    private val activeTickets = AtomicInteger(0)

    // Configuración
    private val config = HandoffConfig()

    companion object {
        private const val PREF_TICKETS = "tickets"
        private const val PREF_AGENTS = "agents"
    }

    // ==================== CREAR TICKET ====================

    /**
     * Crear ticket de escalamiento
     * Se llama cuando la IA no puede resolver
     */
    fun createTicket(
        phone: String,
        customerName: String?,
        lastMessage: String,
        conversationSummary: String,
        intent: String,
        priority: HandoffPriority = HandoffPriority.MEDIUM
    ): HandoffTicket {
        val ticketId = "TKT-${System.currentTimeMillis()}-${ticketCounter.getAndIncrement()}"

        val ticket = HandoffTicket(
            id = ticketId,
            phone = phone,
            customerName = customerName,
            lastMessage = lastMessage,
            conversationSummary = conversationSummary,
            intent = intent,
            priority = priority,
            status = HandoffStatus.PENDING,
            assignedAgent = null,
            createdAt = System.currentTimeMillis(),
            assignedAt = null,
            resolvedAt = null,
            rating = null,
            notes = null
        )

        // Guardar ticket
        tickets[ticketId] = ticket
        ticketsByPhone.getOrPut(phone) { mutableListOf() }.add(ticketId)

        // Incrementar contador
        activeTickets.incrementAndGet()

        // Persistir
        saveToPrefs()

        // Notificar agentes
        if (config.notifyAgentsOnNewTicket) {
            notifyAgents(ticket)
        }

        return ticket
    }

    // ==================== DETECTAR CUANDO ESCALAR ====================

    /**
     * Analizar si la conversación debe escalar a humano
     * Basado en keywords, intentos fallidos, etc.
     */
    fun shouldEscalate(
        message: String,
        aiFailedAttempts: Int,
        conversationLength: Int,
        lastIntent: String
    ): EscalationReason? {

        // 1. Verificar keywords de escalamiento
        val lowerMessage = message.lowercase()
        for (keyword in config.fallbackToHumanKeywords) {
            if (lowerMessage.contains(keyword)) {
                return EscalationReason.KEYWORD_DETECTED
            }
        }

        // 2. Verificar intentos fallidos de la IA
        if (aiFailedAttempts >= config.maxRetriesAI) {
            return EscalationReason.AI_FAILED
        }

        // 3. Conversación muy larga sin resolución
        if (conversationLength > 20 && lastIntent == "GENERAL") {
            return EscalationReason.CONVERSATION_STALLED
        }

        // 4. Keywords de frustración
        val frustrationKeywords = listOf("nunca funciona", "ya intenté", "varias veces", "increíble")
        for (keyword in frustrationKeywords) {
            if (lowerMessage.contains(keyword)) {
                return EscalationReason.FRUSTRATION_DETECTED
            }
        }

        return null
    }

    /**
     * Contador de intentos fallidos de la IA
     */
    fun incrementAIFailedAttempts(phone: String): Int {
        val key = "ai_fail_$phone"
        val current = prefs.getInt(key, 0)
        val newValue = current + 1
        prefs.edit().putInt(key, newValue).apply()
        return newValue
    }

    fun resetAIFailedAttempts(phone: String) {
        prefs.edit().remove("ai_fail_$phone").apply()
    }

    // ==================== GESTIÓN DE AGENTES ====================

    /**
     * Registrar agente
     */
    fun registerAgent(
        agentId: String,
        name: String,
        maxTickets: Int = 5,
        specialties: List<String> = emptyList()
    ): Agent {
        val agent = Agent(
            id = agentId,
            name = name,
            status = AgentStatus.ONLINE,
            activeTickets = 0,
            maxTickets = maxTickets,
            specialties = specialties,
            notificationToken = null
        )

        agents[agentId] = agent
        saveToPrefs()

        return agent
    }

    /**
     * Obtener agente disponible para un ticket
     */
    fun getAvailableAgent(specialty: String? = null): Agent? {
        // Filtrar por especialidad si se requiere
        val availableAgents = agents.values
            .filter { it.status == AgentStatus.ONLINE && it.activeTickets < it.maxTickets }
            .let { list ->
                if (specialty != null) {
                    list.filter { it.specialties.contains(specialty) }
                } else list
            }

        // Retornar el que tenga menos tickets activos
        return availableAgents.minByOrNull { it.activeTickets }
    }

    /**
     * Asignar ticket a agente
     */
    fun assignTicket(ticketId: String, agentId: String): Boolean {
        val ticket = tickets[ticketId] ?: return false
        val agent = agents[agentId] ?: return false

        if (agent.status == AgentStatus.OFFLINE || agent.activeTickets >= agent.maxTickets) {
            return false
        }

        // Actualizar ticket
        tickets[ticketId] = ticket.copy(
            status = HandoffStatus.ASSIGNED,
            assignedAgent = agentId,
            assignedAt = System.currentTimeMillis()
        )

        // Actualizar agente
        agents[agentId] = agent.copy(
            status = AgentStatus.BUSY,
            activeTickets = agent.activeTickets + 1
        )

        saveToPrefs()
        return true
    }

    /**
     * Actualizar estado del agente
     */
    fun updateAgentStatus(agentId: String, status: AgentStatus) {
        agents[agentId]?.let { agent ->
            agents[agentId] = agent.copy(status = status)
            saveToPrefs()
        }
    }

    // ==================== RESOLVER TICKET ====================

    /**
     * Resolver ticket
     */
    fun resolveTicket(ticketId: String, notes: String? = null): Boolean {
        val ticket = tickets[ticketId] ?: return false

        // Actualizar ticket
        tickets[ticketId] = ticket.copy(
            status = HandoffStatus.RESOLVED,
            resolvedAt = System.currentTimeMillis(),
            notes = notes
        )

        // Liberar agente
        ticket.assignedAgent?.let { agentId ->
            agents[agentId]?.let { agent ->
                agents[agentId] = agent.copy(
                    status = AgentStatus.ONLINE,
                    activeTickets = maxOf(0, agent.activeTickets - 1)
                )
            }
        }

        activeTickets.decrementAndGet()
        saveToPrefs()

        return true
    }

    /**
     * Calificar ticket
     */
    fun rateTicket(ticketId: String, rating: Int): Boolean {
        val ticket = tickets[ticketId] ?: return false

        tickets[ticketId] = ticket.copy(rating = rating)
        saveToPrefs()

        return true
    }

    // ==================== CONSULTAS ====================

    /**
     * Obtener ticket por ID
     */
    fun getTicket(ticketId: String): HandoffTicket? = tickets[ticketId]

    /**
     * Obtener tickets de un teléfono
     */
    fun getTicketsByPhone(phone: String): List<HandoffTicket> {
        return ticketsByPhone[phone]?.mapNotNull { tickets[it] } ?: emptyList()
    }

    /**
     * Obtener tickets pendientes
     */
    fun getPendingTickets(): List<HandoffTicket> {
        return tickets.values.filter { it.status == HandoffStatus.PENDING }
            .sortedByDescending { it.priority.ordinal }
    }

    /**
     * Obtener tickets del agente
     */
    fun getAgentTickets(agentId: String): List<HandoffTicket> {
        return tickets.values.filter { it.assignedAgent == agentId }
            .filter { it.status != HandoffStatus.RESOLVED && it.status != HandoffStatus.CLOSED }
    }

    /**
     * Obtener todos los agentes
     */
    fun getAgents(): List<Agent> = agents.values.toList()

    /**
     * Obtener métricas de handoff
     */
    fun getHandoffMetrics(): HandoffMetrics {
        val allTickets = tickets.values.toList()
        val resolvedTickets = allTickets.filter { it.status == HandoffStatus.RESOLVED }

        val avgResponseTime = if (resolvedTickets.isNotEmpty()) {
            resolvedTickets.mapNotNull { ticket ->
                ticket.assignedAt?.let { assigned ->
                    assigned - ticket.createdAt
                }
            }.average()
        } else 0.0

        val avgResolutionTime = if (resolvedTickets.isNotEmpty()) {
            resolvedTickets.mapNotNull { ticket ->
                ticket.resolvedAt?.let { resolved ->
                    resolved - (ticket.assignedAt ?: ticket.createdAt)
                }
            }.average()
        } else 0.0

        val ratings = resolvedTickets.mapNotNull { it.rating }
        val avgRating = if (ratings.isNotEmpty()) ratings.average() else 0.0

        return HandoffMetrics(
            totalTickets = allTickets.size,
            pendingTickets = allTickets.count { it.status == HandoffStatus.PENDING },
            activeTickets = activeTickets.get(),
            resolvedTickets = resolvedTickets.size,
            avgResponseTimeMs = avgResponseTime.toLong(),
            avgResolutionTimeMs = avgResolutionTime.toLong(),
            avgRating = avgRating,
            escalationRate = if (allTickets.isNotEmpty()) {
                allTickets.size.toDouble() / allTickets.size
            } else 0.0
        )
    }

    // ==================== NOTIFICACIONES ====================

    /**
     * Notificar a agentes sobre nuevo ticket
     * (Implementar con Firebase/OneSignal/etc)
     */
    private fun notifyAgents(ticket: HandoffTicket) {
        // Placeholder para notificaciones push
        // Implementar con: Firebase Cloud Messaging, OneSignal, etc.
        prefs.edit()
            .putString("last_notification", "New ticket: ${ticket.id}")
            .apply()
    }

    // ==================== PERSISTENCIA ====================

    private fun saveToPrefs() {
        val ticketsJson = gson.toJson(tickets)
        val agentsJson = gson.toJson(agents)
        prefs.edit()
            .putString(PREF_TICKETS, ticketsJson)
            .putString(PREF_AGENTS, agentsJson)
            .apply()
    }

    fun loadFromPrefs() {
        val ticketsJson = prefs.getString(PREF_TICKETS, null)
        val agentsJson = prefs.getString(PREF_AGENTS, null)

        if (ticketsJson != null) {
            val loaded = gson.fromJson(ticketsJson, tickets.javaClass)
            tickets.putAll(loaded)
        }

        if (agentsJson != null) {
            val loaded = gson.fromJson(agentsJson, agents.javaClass)
            agents.putAll(loaded)
        }
    }
}

enum class EscalationReason {
    KEYWORD_DETECTED,      // Cliente pidió humano
    AI_FAILED,            // IA falló múltiples veces
    CONVERSATION_STALLED,  // Conversación sin progreso
    FRUSTRATION_DETECTED, // Cliente frustrado
    MANUAL_REQUEST        // Agente lo solicitó
}

data class HandoffMetrics(
    val totalTickets: Int,
    val pendingTickets: Int,
    val activeTickets: Int,
    val resolvedTickets: Int,
    val avgResponseTimeMs: Long,
    val avgResolutionTimeMs: Long,
    val avgRating: Double,
    val escalationRate: Double
)
