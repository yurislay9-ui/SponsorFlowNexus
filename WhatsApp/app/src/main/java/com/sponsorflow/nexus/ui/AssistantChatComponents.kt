/*
 * SponsorFlow Nexus - Assistant Chat Components
 */
package com.sponsorflow.nexus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MessageBubble(message: ChatMessage) {
    val bgColor = if (message.isFromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isFromUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bgColor,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(text = message.text, color = textColor, modifier = Modifier.padding(12.dp))
        }
    }
}

@Composable
fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe tu pregunta...") },
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(onClick = onSend, enabled = text.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, "Enviar")
            }
        }
    }
}