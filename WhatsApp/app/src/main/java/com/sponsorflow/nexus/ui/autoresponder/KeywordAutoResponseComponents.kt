/*
 * Keyword Auto-Response UI Components
 */
package com.sponsorflow.nexus.ui.autoresponder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sponsorflow.nexus.autoresponder.*

@Composable
fun KeywordCard(response: KeywordResponse) {
    val actionIcon = when (response.actionType) {
        KeywordActionType.SEND_MESSAGE -> Icons.AutoMirrored.Filled.Message
        KeywordActionType.SEND_PRODUCT -> Icons.Default.ShoppingCart
        KeywordActionType.SEND_CATALOG -> Icons.AutoMirrored.Filled.MenuBook
        KeywordActionType.SEND_PRICE_LIST -> Icons.Default.AttachMoney
        KeywordActionType.ESCALATE_HUMAN -> Icons.Default.Person
        KeywordActionType.START_CHECKOUT -> Icons.Default.ShoppingCart
        KeywordActionType.SEND_LOCATION -> Icons.Default.LocationOn
        KeywordActionType.SEND_WORKING_HOURS -> Icons.Default.Schedule
        KeywordActionType.SEND_SHIPPING_INFO -> Icons.Default.LocalShipping
        KeywordActionType.SEND_PAYMENT_INFO -> Icons.Default.CreditCard
        KeywordActionType.APPLY_DISCOUNT -> Icons.Default.LocalOffer
        KeywordActionType.CUSTOM -> Icons.Default.Build
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(actionIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        response.keywords.take(3).forEach { kw ->
                            Text("\"$kw\"", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                        if (response.keywords.size > 3) {
                            Text("+${response.keywords.size - 3} más", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (response.isActive) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)
                ) {
                    Text(
                        if (response.isActive) "Activo" else "Inactivo",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        color = if (response.isActive) Color(0xFF4CAF50) else Color.Gray
                    )
                }
            }

            if (response.responseMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(response.responseMessage, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Usado: ${response.responseCount} veces", fontSize = 12.sp, color = Color.Gray)
                Text("Prioridad: ${response.priority}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(message, color = Color.Gray)
        }
    }
}
