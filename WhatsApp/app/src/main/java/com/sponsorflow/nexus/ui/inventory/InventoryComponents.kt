/*
 * SponsorFlow Nexus - Inventory Components
 */
package com.sponsorflow.nexus.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sponsorflow.nexus.data.entity.ProductEntity
import com.sponsorflow.nexus.data.entity.StockStatus

@Composable
fun StatCard(label: String, value: String, color: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    val statusColor = when (product.getStockStatus()) {
        StockStatus.IN_STOCK -> Color(0xFF4CAF50)
        StockStatus.LOW_STOCK -> Color(0xFFFFC107)
        StockStatus.OUT_OF_STOCK -> Color(0xFFF44336)
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(12.dp).background(statusColor, RoundedCornerShape(50)))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text("SKU: ${product.sku}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("$${String.format("%.2f", product.price)}", color = MaterialTheme.colorScheme.primary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease) { Icon(Icons.Default.Remove, "Reducir") }
                Text("${product.stockQuantity}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = onIncrease) { Icon(Icons.Default.Add, "Aumentar") }
            }
        }
    }
}

