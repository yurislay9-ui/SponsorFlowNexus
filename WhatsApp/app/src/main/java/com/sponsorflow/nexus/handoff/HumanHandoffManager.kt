/*
 * SponsorFlow Nexus - Human Handoff Manager (Compact)
 */
package com.sponsorflow.nexus.handoff

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

enum class EscalationReason { KEYWORD_DETECTED, AI_FAILED, CONVERSATION_STALLED, FRUSTRATION_DETECTED }

class HumanHandoffManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("nexus_handoff", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val tickets = ConcurrentHashMap<String, HandoffTicket>()
    private val ticketsByPhone = ConcurrentHashMap<String, MutableList<String>>()
    private val agents = ConcurrentHashMap<String, Agent>()
    private val ticketCounter = AtomicInteger(1)
    private val config = HandoffConfig()

    fun createTicket(phone: String, customerName: String?, lastMessage: String, conversationSummary: String, intent: String, priority: HandoffPriority = HandoffPriority.MEDIUM): HandoffTicket {
        val ticket = HandoffTicket("TKT-${System.currentTimeMillis()}-${ticketCounter.getAndIncrement()}", phone, customerName, lastMessage, conversationSummary, intent, priority, HandoffStatus.PENDING, null, System.currentTimeMillis(), null, null, null, null)
        tickets[ticket.id] = ticket
        ticketsByPhone.getOrPut(phone) { mutableListOf() }.add(ticket.id)
        saveToPrefs()
        return ticket
    }

    fun shouldEscalate(message: String, aiFailedAttempts: Int, conversationLength: Int, lastIntent: String): EscalationReason? {
        val lower = message.lowercase()
        if (config.fallbackToHumanKeywords.any { lower.contains(it) }) return EscalationReason.KEYWORD_DETECTED
        if (aiFailedAttempts >= config.maxRetriesAI) return EscalationReason.AI_FAILED
        if (conversationLength > 20 && lastIntent == "GENERAL") return EscalationReason.CONVERSATION_STALLED
        if (listOf("nunca funciona", "ya intenté", "varias veces", "increíble").any { lower.contains(it) }) return EscalationReason.FRUSTRATION_DETECTED
        return null
    }

    fun incrementAIFailedAttempts(phone: String): Int { val current = prefs.getInt("ai_fail_$phone", 0) + 1; prefs.edit().putInt("ai_fail_$phone", current).apply(); return current }
    fun resetAIFailedAttempts(phone: String) = prefs.edit().remove("ai_fail_$phone").apply()

    fun registerAgent(agentId: String, name: String, maxTickets: Int = 5, specialties: List<String> = emptyList()): Agent {
        val agent = Agent(agentId, name, AgentStatus.ONLINE, 0, maxTickets, specialties, null)
        agents[agentId] = agent
        return agent
    }

    fun getAvailableAgent(specialty: String? = null): Agent? = agents.values.filter { it.status == AgentStatus.ONLINE && it.activeTickets < it.maxTickets }.let { list -> if (specialty != null) list.filter { it.specialties.contains(specialty) } else list }.minByOrNull { it.activeTickets }

    fun assignTicket(ticketId: String, agentId: String): Boolean {
        val agent = agents[agentId] ?: return false
        tickets[ticketId]?.let { ticket ->
            tickets[ticketId] = ticket.copy(status = HandoffStatus.ASSIGNED, assignedAgent = agentId, assignedAt = System.currentTimeMillis())
            agents[agentId] = agent.copy(activeTickets = agent.activeTickets + 1)
            return true
        }
        return false
    }

    fun resolveTicket(ticketId: String, rating: Int? = null, notes: String? = null): Boolean {
        tickets[ticketId]?.let { ticket ->
            tickets[ticketId] = ticket.copy(status = HandoffStatus.RESOLVED, resolvedAt = System.currentTimeMillis(), rating = rating, notes = notes)
            ticket.assignedAgent?.let { agents[it]?.let { a -> agents[it] = a.copy(activeTickets = a.activeTickets - 1) } }
            return true
        }
        return false
    }

    fun getTicket(ticketId: String): HandoffTicket? = tickets[ticketId]
    fun getTicketsByPhone(phone: String): List<HandoffTicket> = ticketsByPhone[phone]?.mapNotNull { tickets[it] } ?: emptyList()
    fun getPendingTickets(): List<HandoffTicket> = tickets.values.filter { it.status == HandoffStatus.PENDING }.sortedByDescending { it.priority.ordinal }
    fun getAgent(agentId: String): Agent? = agents[agentId]

    private fun notifyAgents(ticket: HandoffTicket) { /* Notification logic */ }
    private fun saveToPrefs() { prefs.edit().putString("tickets", gson.toJson(tickets)).putString("agents", gson.toJson(agents)).apply() }
    fun loadFromPrefs() {
        prefs.getString("tickets", null)?.let { tickets.putAll(gson.fromJson(it)) }
        prefs.getString("agents", null)?.let { agents.putAll(gson.fromJson(it)) }
    }
}
