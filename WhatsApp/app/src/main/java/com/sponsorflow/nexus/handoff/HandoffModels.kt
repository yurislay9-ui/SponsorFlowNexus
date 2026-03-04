/*
 * Human Handoff Models
 */
package com.sponsorflow.nexus.handoff

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

enum class HandoffPriority { LOW, MEDIUM, HIGH, URGENT }
enum class HandoffStatus { PENDING, ASSIGNED, IN_PROGRESS, RESOLVED, ESCALATED, CLOSED }

data class Agent(
    val id: String,
    val name: String,
    val status: AgentStatus,
    val activeTickets: Int,
    val maxTickets: Int,
    val specialties: List<String>,
    val notificationToken: String?
)

enum class AgentStatus { ONLINE, BUSY, AWAY, OFFLINE }

data class HandoffConfig(
    val maxRetriesAI: Int = 2,
    val fallbackToHumanKeywords: List<String> = listOf("hablar con persona", "hablar con humano", "no funciona", "problema técnico", "error", "queja", "gerente", "dueño"),
    val autoEscalateAfterMinutes: Int = 30,
    val notifyAgentsOnNewTicket: Boolean = true,
    val allowCustomerRate: Boolean = true,
    val minRatingToImprove: Int = 3
)
