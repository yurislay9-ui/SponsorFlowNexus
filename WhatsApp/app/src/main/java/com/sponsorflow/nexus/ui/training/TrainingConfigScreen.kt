/*
 * SponsorFlow Nexus - Training Configuration Screen
 * Dashboard para configurar el entrenamiento de la IA
 */
package com.sponsorflow.nexus.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.sponsorflow.nexus.training.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingConfigScreen(
    onBack: () -> Unit,
    trainingManager: TrainingManager,
    phone: String
) {
    var training by remember { mutableStateOf<ClientTraining?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var addDialogType by remember { mutableStateOf("") }

    LaunchedEffect(phone) {
        training = trainingManager.getClientTraining(phone)
        if (training == null) {
            // Crear entrenamiento por defecto
            trainingManager.createClientTraining(
                phone = phone,
                businessName = "Mi Negocio",
                businessType = "ecommerce",
                tone = "friendly"
            )
            training = trainingManager.getClientTraining(phone)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎓 Entrena tu IA") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info del negocio
            item {
                BusinessInfoCard(
                    training = training,
                    onUpdate = { name, type, tone ->
                        trainingManager.updateBusinessInfo(phone, name, type, tone)
                        training = trainingManager.getClientTraining(phone)
                    }
                )
            }

            // Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("📦 Productos") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("❓ FAQ") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("💬 Respuestas") }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("📋 Reglas") }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text("🔑 Keywords") }
                    )
                }
            }

            // Contenido según tab
            when (selectedTab) {
                0 -> { // Productos
                    item {
                        AddButton(
                            text = "Agregar Producto",
                            onClick = { showAddDialog = true; addDialogType = "product" }
                        )
                    }
                    training?.products?.forEach { product ->
                        item {
                            ProductCard(
                                product = product,
                                onDelete = {
                                    trainingManager.getClientTraining(phone)?.let { t ->
                                        val products = t.products.filter { it.id != product.id }
                                        // Actualizar...
                                    }
                                }
                            )
                        }
                    }
                }
                1 -> { // FAQ
                    item {
                        AddButton(
                            text = "Agregar FAQ",
                            onClick = { showAddDialog = true; addDialogType = "faq" }
                        )
                    }
                    training?.faq?.forEach { faq ->
                        item {
                            FAQCard(faq = faq)
                        }
                    }
                }
                2 -> { // Respuestas
                    item {
                        AddButton(
                            text = "Agregar Respuesta",
                            onClick = { showAddDialog = true; addDialogType = "response" }
                        )
                    }
                    training?.trainingItems?.filter { it.categoryId == "responses" }?.forEach { item ->
                        item {
                            ResponseCard(item = item)
                        }
                    }
                }
                3 -> { // Reglas
                    item {
                        AddButton(
                            text = "Agregar Regla",
                            onClick = { showAddDialog = true; addDialogType = "rule" }
                        )
                    }
                    training?.customRules?.forEach { rule ->
                        item {
                            RuleCard(rule = rule)
                        }
                    }
                }
                4 -> { // Keywords
                    item {
                        AddButton(
                            text = "Agregar Keyword",
                            onClick = { showAddDialog = true; addDialogType = "keyword" }
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(training?.keywords ?: emptyList()) { keyword ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(keyword) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Botón generar prompt
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        trainingManager.generatePrompt(phone)?.let { prompt ->
                            // Mostrar o guardar prompt
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generar Prompt para la IA")
                }
            }
        }
    }
}

@Composable
fun BusinessInfoCard(
    training: ClientTraining?,
    onUpdate: (String, String, String) -> Unit
) {
    var businessName by remember { mutableStateOf(training?.businessName ?: "") }
    var businessType by remember { mutableStateOf(training?.businessType ?: "ecommerce") }
    var tone by remember { mutableStateOf(training?.tone ?: "friendly") }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("🏢 Información del Negocio", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            // Nombre del negocio
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Nombre del negocio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            // Tipo de negocio
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = TrainingManager.BUSINESS_TYPES.find { it.first == businessType }?.second ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Tipo de negocio") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    TrainingManager.BUSINESS_TYPES.forEach { (type, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                businessType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Tono
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎭 Tono:", fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                TONES.forEach { (t, name) ->
                    FilterChip(
                        selected = tone == t,
                        onClick = { tone = t },
                        label = { Text(name.split(" - ")[0]) },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { onUpdate(businessName, businessType, tone) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar")
            }
        }
    }
}

@Composable
fun AddButton(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = Color(0xFF4CAF50)
            )
            Spacer(Modifier.width(8.dp))
            Text(text, color = Color(0xFF4CAF50))
        }
    }
}

@Composable
fun ProductCard(product: ProductInfo, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text(product.description, fontSize = 12.sp, color = Color.Gray)
                product.price?.let {
                    Text("💰 $$it", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}

@Composable
fun FAQCard(faq: QAPair) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = Color(0xFF2196F3))
                Spacer(Modifier.width(8.dp))
                Text("❓ ${faq.question}", fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(4.dp))
            Text("💬 ${faq.answer}", fontSize = 12.sp)
        }
    }
}

@Composable
fun ResponseCard(item: TrainingItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(item.content, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun RuleCard(rule: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFFF9800))
            Spacer(Modifier.width(8.dp))
            Text(rule)
        }
    }
}

val TONES = TrainingManager.TONES
