/*
 * SponsorFlow Nexus - Agent Dashboard Screen
 * Interfaz para que los agentes atiendan tickets
 */
package com.sponsorflow.nexus.ui.handoff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sponsorflow.nexus.handoff.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDashboardScreen(
    onBack: () -> Unit,
    agentId: String,
    handoffManager: HumanHandoffManager
) {
    var selectedTab by remember { mutableStateOf(0) }
    var agent by remember { mutableStateOf<Agent?>(null) }
    var tickets by remember { mutableStateOf<List<HandoffTicket>>(emptyList()) }
    var metrics by remember { mutableStateOf<HandoffMetrics?>(null) }

    LaunchedEffect(agentId) {
        agent = handoffManager.getAgents().find { it.id == agentId }
        tickets = handoffManager.getAgentTickets(agentId)
        metrics = handoffManager.getHandoffMetrics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎧 Panel de Agente") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    // Estado del agente
                    IconButton(onClick = {
                        val currentStatus = agent?.status ?: AgentStatus.OFFLINE
                        val newStatus = when (currentStatus) {
                            AgentStatus.ONLINE -> AgentStatus.AWAY
                            AgentStatus.AWAY -> AgentStatus.ONLINE
                            else -> AgentStatus.ONLINE
                        }
                        handoffManager.updateAgentStatus(agentId, newStatus)
                        agent = agent?.copy(status = newStatus)
                    }) {
                        Icon(
                            if (agent?.status == AgentStatus.ONLINE) Icons.Default.NotificationsActive
                            else Icons.Default.NotificationsOff,
                            contentDescription = "Estado"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Estado del agente
            AgentStatusBar(agent)

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Tickets (${tickets.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Cola Global") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Métricas") }
                )
            }

            when (selectedTab) {
                0 -> MyTicketsTab(tickets, handoffManager)
                1 -> GlobalQueueTab(handoffManager)
                2 -> MetricsTab(metrics)
            }
        }
    }
}

@Composable
fun AgentStatusBar(agent: Agent?) {
    val statusColor = when (agent?.status) {
        AgentStatus.ONLINE -> Color(0xFF4CAF50)
        AgentStatus.BUSY -> Color(0xFFFF9800)
        AgentStatus.AWAY -> Color(0xFFFFEB3B)
        AgentStatus.OFFLINE -> Color(0xFFF44336)
        null -> Color.Gray
    }

    val statusText = when (agent?.status) {
        AgentStatus.ONLINE -> "En línea"
        AgentStatus.BUSY -> "Ocupado"
        AgentStatus.AWAY -> "Ausente"
        AgentStatus.OFFLINE -> "Desconectado"
        null -> "Cargando..."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor)
            )
            Spacer(Modifier.width(8.dp))
            Text(agent?.name ?: "Agente", fontWeight = FontWeight.Bold)
        }

        Row {
            Text("$statusText • ", color = Color.Gray)
            Text("Tickets: ${agent?.activeTickets ?: 0}/${agent?.maxTickets ?: 5}")
        }
    }
}

@Composable
fun MyTicketsTab(tickets: List<HandoffTicket>, handoffManager: HumanHandoffManager) {
    if (tickets.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
                Text("No tienes tickets activos", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tickets) { ticket ->
                TicketCard(ticket, handoffManager, isAgentView = true)
            }
        }
    }
}

@Composable
fun GlobalQueueTab(handoffManager: HumanHandoffManager) {
    val pendingTickets = handoffManager.getPendingTickets()

    if (pendingTickets.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay tickets pendientes", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pendingTickets) { ticket ->
                TicketCard(ticket, handoffManager, isAgentView = false)
            }
        }
    }
}

@Composable
fun MetricsTab(metrics: HandoffMetrics?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 Métricas", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricBox("Total", "${metrics?.totalTickets ?: 0}")
                        MetricBox("Pendientes", "${metrics?.pendingTickets ?: 0}")
                        MetricBox("Resueltos", "${metrics?.resolvedTickets ?: 0}")
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricBox(
                            "Tiempo Prom.",
                            "${((metrics?.avgResponseTimeMs ?: 0) / 60000).toInt()} min"
                        )
                        MetricBox(
                            "Resolución",
                            "${((metrics?.avgResolutionTimeMs ?: 0) / 60000).toInt()} min"
                        )
                        MetricBox(
                            "Rating",
                            String.format("%.1f", metrics?.avgRating ?: 0.0)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun TicketCard(
    ticket: HandoffTicket,
    handoffManager: HumanHandoffManager,
    isAgentView: Boolean
) {
    val priorityColor = when (ticket.priority) {
        HandoffPriority.URGENT -> Color(0xFFF44336)
        HandoffPriority.HIGH -> Color(0xFFFF9800)
        HandoffPriority.MEDIUM -> Color(0xFF2196F3)
        HandoffPriority.LOW -> Color(0xFF4CAF50)
    }

    val statusIcon = when (ticket.status) {
        HandoffStatus.PENDING -> Icons.Default.HourglassEmpty
        HandoffStatus.ASSIGNED -> Icons.Default.Person
        HandoffStatus.IN_PROGRESS -> Icons.Default.PlayArrow
        HandoffStatus.RESOLVED -> Icons.Default.CheckCircle
        HandoffStatus.ESCALATED -> Icons.Default.ArrowUpward
        HandoffStatus.CLOSED -> Icons.Default.Cancel
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = priorityColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(ticket.id, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = priorityColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        ticket.priority.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        color = priorityColor
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Cliente
            Text(
                ticket.customerName ?: ticket.phone,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(4.dp))

            // Último mensaje
            Text(
                ticket.lastMessage,
                color = Color.Gray,
                maxLines = 2,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(8.dp))

            // Intención detectada
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.Gray
                )
                Spacer(Modifier.width(4.dp))
                Text(ticket.intent, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(12.dp))

            // Acciones
            if (isAgentView && ticket.status == HandoffStatus.ASSIGNED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { /* Ver conversación */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ver Chat")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = { handoffManager.resolveTicket(ticket.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Resolver")
                    }
                }
            } else if (!isAgentView && ticket.status == HandoffStatus.PENDING) {
                Button(
                    onClick = { /* Asignarme ticket */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tomar Ticket")
                }
            }
        }
    }
}
