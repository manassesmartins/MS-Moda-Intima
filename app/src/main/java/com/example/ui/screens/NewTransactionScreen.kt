package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTransactionScreen(
    viewModel: TransactionViewModel,
    isCloudBackupEnabled: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, Double, String, String, String) -> Unit,
    transactionToEdit: TransactionEntity? = null
) {
    val Primary = MaterialTheme.colorScheme.primary
    val OnPrimary = MaterialTheme.colorScheme.onPrimary
    val Secondary = MaterialTheme.colorScheme.secondary
    val OnSecondary = MaterialTheme.colorScheme.onSecondary
    val Tertiary = MaterialTheme.colorScheme.tertiary
    val OnTertiary = MaterialTheme.colorScheme.onTertiary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val SurfaceDark = MaterialTheme.colorScheme.background
    val ErrorColor = MaterialTheme.colorScheme.error

    var description by remember(transactionToEdit) { mutableStateOf(transactionToEdit?.description ?: "") }
    var amountText by remember(transactionToEdit) { mutableStateOf(transactionToEdit?.amount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    val type = transactionToEdit?.type ?: "OUTFLOW"
    val toggleSync = remember(transactionToEdit) { mutableStateOf(isCloudBackupEnabled) }
    var selectedWeek by remember(transactionToEdit) { mutableStateOf(transactionToEdit?.week ?: "1ª Semana") }

    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle(emptyList())
    val existingExpenses = remember(transactions) {
        transactions.filter { it.type == "OUTFLOW" }.map { it.description }.distinct().sorted()
    }
    var expandedDescDropdown by remember { mutableStateOf(false) }

    var categoryText by remember(transactionToEdit) { mutableStateOf(transactionToEdit?.category ?: "") }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    val masterCategories by viewModel.allCategories.collectAsStateWithLifecycle(emptyList())
    val existingCategoryNames = remember(masterCategories) {
        masterCategories.filter { it.type == "OUTFLOW" }.map { it.name }.distinct().sortedBy { it.lowercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
    ) {
        // Title Bar Header
        TopAppBar(
            title = {
                Text(
                    text = if (transactionToEdit != null) "Editar Gasto" else "Novo Gasto",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            modifier = Modifier.drawBehindGlassBorder()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero info description
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Primary.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning, // Clear visual icon for Gastos/Outflows
                            contentDescription = "Gasto",
                            tint = ErrorColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (transactionToEdit != null) "Atualize as informações do gasto registrado para manter suas contas corretas." else "Registre e salve seus gastos na lista para ter total controle financeiro sem complicações.",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        lineHeight = 20.sp
                    )
                }
            }

            // 1. Categoria / Nome do Gasto (linked to master categories list "Recursos & Cadastros -> Nomes de Gastos")
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "CATEGORIA / NOME DO GASTO (DE RECURSOS)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedCategoryDropdown,
                        onExpandedChange = { expandedCategoryDropdown = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = categoryText,
                            onValueChange = { 
                                categoryText = it
                                expandedCategoryDropdown = true 
                            },
                            placeholder = { Text("Selecione ou digite um tipo de gasto registrado...", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedContainerColor = Color.White.copy(alpha = 0.04f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("category_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        val filteredCategories = existingCategoryNames.filter { it.contains(categoryText, ignoreCase = true) }
                        
                        ExposedDropdownMenu(
                            expanded = expandedCategoryDropdown,
                            onDismissRequest = { expandedCategoryDropdown = false },
                            modifier = Modifier.background(SurfaceDark).border(1.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            if (filteredCategories.isEmpty() && categoryText.isBlank()) {
                                DropdownMenuItem(
                                    text = { Text("Cadastre nomes de gastos no menu de Recursos", fontSize = 12.sp, color = OnSurfaceVariant) },
                                    onClick = {}
                                )
                            } else {
                                filteredCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, color = OnSurface) },
                                        onClick = {
                                            categoryText = cat
                                            expandedCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                            
                            val trimmedCat = categoryText.trim()
                            if (trimmedCat.isNotEmpty() && !existingCategoryNames.any { it.equals(trimmedCat, ignoreCase = true) }) {
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = "+ Cadastrar \"$trimmedCat\" no Catálogo", 
                                            color = Primary, 
                                            fontWeight = FontWeight.Bold 
                                        ) 
                                    },
                                    onClick = {
                                        viewModel.addCategory(trimmedCat, "OUTFLOW")
                                        expandedCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Descrição Opcional (Detalhes Adicionais: p.ex. "Fornecedor TexArt", "Energia do Mês")
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "HISTÓRICO / DETALHES DA SAÍDA (OPCIONAL)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedDescDropdown,
                        onExpandedChange = { expandedDescDropdown = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { 
                                description = it
                                expandedDescDropdown = true 
                            },
                            placeholder = { Text("Ex: Fornecedor TexArt, Conserto da Galoneira, etc.", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedContainerColor = Color.White.copy(alpha = 0.04f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("description_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        val filteredExpenses = existingExpenses.filter { it.contains(description, ignoreCase = true) }
                        if (filteredExpenses.isNotEmpty() && expandedDescDropdown) {
                            ExposedDropdownMenu(
                                expanded = expandedDescDropdown,
                                onDismissRequest = { expandedDescDropdown = false },
                                modifier = Modifier.background(SurfaceDark)
                            ) {
                                filteredExpenses.forEach { exp ->
                                    DropdownMenuItem(
                                        text = { Text(exp, color = OnSurface) },
                                        onClick = {
                                            description = exp
                                            expandedDescDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Amount Input Field
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "VALOR (R$)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        placeholder = { Text("0,00", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                        prefix = { Text("R$ ", color = Primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = Color.White.copy(alpha = 0.04f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amount_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Week Selection row of Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SEMANA DO REGISTRO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val weeks = listOf("1ª Semana", "2ª Semana", "3ª Semana", "4ª Semana", "5ª Semana")
                        items(weeks, key = { it }) { w ->
                            val isSelected = selectedWeek == w
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(
                                        if (isSelected) Primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(32.dp)
                                    )
                                    .clickable { selectedWeek = w }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = w,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Primary else OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Dynamic Synchronization status preview toggler
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Sincronizar na Nuvem",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSurface
                            )
                            Text(
                                text = "Sincronização em tempo real na sua conta",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = toggleSync.value,
                        onCheckedChange = { toggleSync.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnPrimary,
                            checkedTrackColor = Tertiary,
                            uncheckedThumbColor = OnSurfaceVariant,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val amt = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0
                        if (categoryText.isNotBlank() && amt > 0) {
                            // If description is empty, default it to the category name
                            val finalDesc = description.ifBlank { categoryText }
                            onSubmit(finalDesc, amt, categoryText.trim(), type, selectedWeek)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_transaction_button"),
                    enabled = categoryText.isNotBlank() && (amountText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Text(
                            text = if (transactionToEdit != null) "Atualizar Gasto" else "Salvar Gasto",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Pro Tip section card at the bottom
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = getGlassContainerColor()
                    ),
                    border = getGlassBorderStroke(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Os gastos salvos aqui impactam diretamente seus relatórios mensais de lucratividade do seu negócio.",
                            fontSize = 11.sp,
                            color = OnSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
