/*
 * SponsorFlow Nexus v2.4 - Subscription Screen
 */
package com.sponsorflow.nexus.ui.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PlanInfo(
    val name: String,
    val price: String,
    val color: Color,
    val features: List<String>,
    val recommended: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    onSubscribe: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedPlan by remember { mutableStateOf<String?>(null) }
    
    val plans = listOf(
        PlanInfo(
            name = "GRATIS",
            price = "$0",
            color = Color.Gray,
            features = listOf(
                "Respuestas básicas",
                "Sin memoria de conversación",
                "Sin inventario"
            )
        ),
        PlanInfo(
            name = "OBSERVADOR",
            price = "$9/mes",
            color = Color(0xFF2196F3),
            features = listOf(
                "Memoria 5 conversaciones",
                "Inventario de productos",
                "Prompts personalizados",
                "Plugins básicos"
            )
        ),
        PlanInfo(
            name = "DESARROLLO",
            price = "$19/mes",
            color = Color(0xFF9C27B0),
            features = listOf(
                "Memoria 20 conversaciones",
                "Inventario ilimitado",
                "Todas las categorías",
                "Plugins avanzados"
            ),
            recommended = true
        ),
        PlanInfo(
            name = "EMPRESARIO",
            price = "$29/mes",
            color = Color(0xFFFF9800),
            features = listOf(
                "Memoria ilimitada",
                "SDK de plugins",
                "Soporte prioritario",
                "API completa"
            )
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Subscriptions,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Planes de Suscripción",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Elige el plan perfecto para tu negocio",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        // Plans
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            plans.forEach { plan ->
                PlanCard(
                    plan = plan,
                    isSelected = selectedPlan == plan.name,
                    onSelect = { selectedPlan = plan.name }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subscribe Button
            if (selectedPlan != null) {
                Button(
                    onClick = { 
                        onSubscribe(selectedPlan ?: "")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Suscribirse a $selectedPlan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Payment Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Payment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pago seguro con USDT (TRC20)",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Aceptamos criptomonedas para mayor seguridad y privacidad",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlanCard(
    plan: PlanInfo,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                plan.color.copy(alpha = 0.1f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected || plan.recommended) {
            androidx.compose.foundation.BorderStroke(2.dp, plan.color)
        } else null,
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(plan.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = plan.name.first().toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = plan.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = plan.price,
                            color = plan.color,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (plan.recommended) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = plan.color
                    ) {
                        Text(
                            text = " POPULAR ",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            plan.features.forEach { feature ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = plan.color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}