package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CategoryEntity
import com.example.data.ClientEntity
import com.example.data.EmployeeEntity
import com.example.data.EmployeePaymentEntity
import com.example.data.ProductModelEntity
import com.example.ui.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(viewModel: TransactionViewModel) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf("PAGAMENTOS") } // "PAGAMENTOS" or "CADASTROS"

    val Primary = MaterialTheme.colorScheme.primary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val SurfaceDark = MaterialTheme.colorScheme.background
    val SurfaceContainerHigh = MaterialTheme.colorScheme.surfaceVariant
    val ErrorColor = MaterialTheme.colorScheme.error

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Upper Title Page
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Recursos & Cadastros",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = "Gestão de pessoal, pagamentos e tabelas mestras",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sub Tabs Selector (PAGAMENTOS | CADASTROS)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .padding(4.dp)
        ) {
            val tabs = listOf(
                "PAGAMENTOS" to "Pagamentos Semanais",
                "CADASTROS" to "Gerenciar Listas"
            )
            tabs.forEach { (key, label) ->
                val isSelected = activeSubTab == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Primary.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { activeSubTab = key }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Primary else OnSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeSubTab) {
            "PAGAMENTOS" -> EmployeePaymentsTab(viewModel = viewModel)
            "CADASTROS" -> MasterRegistriesTab(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeePaymentsTab(viewModel: TransactionViewModel) {
    val context = LocalContext.current
    val employees by viewModel.allEmployees.collectAsStateWithLifecycle()
    val payments by viewModel.allPayments.collectAsStateWithLifecycle()

    var selectedEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }
    var employeeDropdownExpanded by remember { mutableStateOf(false) }
    var paymentAmountText by remember { mutableStateOf("") }
    var selectedWeek by remember { mutableStateOf("1ª Semana") }
    var weekDropdownExpanded by remember { mutableStateOf(false) }

    val Primary = MaterialTheme.colorScheme.primary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val SurfaceContainerHigh = MaterialTheme.colorScheme.surfaceVariant
    val ErrorColor = MaterialTheme.colorScheme.error

    val weeks = listOf("1ª Semana", "2ª Semana", "3ª Semana", "4ª Semana", "5ª Semana")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // FORM CARD to register a payment note
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Anotar Pagamento Semanal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )

                    // Employee autocomplete dropdown selection
                    ExposedDropdownMenuBox(
                        expanded = employeeDropdownExpanded,
                        onExpandedChange = { employeeDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedEmployee?.name ?: "Selecione o Funcionário...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Funcionário beneficiário", color = OnSurfaceVariant) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = employeeDropdownExpanded,
                            onDismissRequest = { employeeDropdownExpanded = false },
                            modifier = Modifier.background(SurfaceContainerHigh)
                        ) {
                            if (employees.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Nenhum funcionário cadastrado", color = OnSurfaceVariant) },
                                    onClick = { employeeDropdownExpanded = false }
                                )
                            } else {
                                employees.forEach { emp ->
                                    DropdownMenuItem(
                                        text = { Text("${emp.name} (${emp.role})", color = OnSurface) },
                                        onClick = {
                                            selectedEmployee = emp
                                            employeeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Amount Field
                        OutlinedTextField(
                            value = paymentAmountText,
                            onValueChange = { paymentAmountText = it },
                            label = { Text("Valor Pago (R$)", color = OnSurfaceVariant) },
                            placeholder = { Text("Ex: 350.00", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Week Selector
                        ExposedDropdownMenuBox(
                            expanded = weekDropdownExpanded,
                            onExpandedChange = { weekDropdownExpanded = it },
                            modifier = Modifier.weight(1.5f)
                        ) {
                            OutlinedTextField(
                                value = selectedWeek,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Semana de referência", color = OnSurfaceVariant) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = OnSurface,
                                    unfocusedTextColor = OnSurface,
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = weekDropdownExpanded,
                                onDismissRequest = { weekDropdownExpanded = false },
                                modifier = Modifier.background(SurfaceContainerHigh)
                            ) {
                                weeks.forEach { wk ->
                                    DropdownMenuItem(
                                        text = { Text(wk, color = OnSurface) },
                                        onClick = {
                                            selectedWeek = wk
                                            weekDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val emp = selectedEmployee
                            val amount = paymentAmountText.replace(',', '.').toDoubleOrNull()
                            if (emp == null) {
                                Toast.makeText(context, "Por favor, selecione um funcionário!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (amount == null || amount <= 0.0) {
                                Toast.makeText(context, "Por favor, digite um valor de pagamento válido!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR"))
                            val dateStr = sdf.format(Date()).uppercase(Locale.getDefault())

                            viewModel.addEmployeePayment(
                                employeeId = emp.id,
                                employeeName = emp.name,
                                amount = amount,
                                week = selectedWeek,
                                dateString = dateStr
                            )

                            paymentAmountText = ""
                            selectedEmployee = null
                            Toast.makeText(context, "Pagamento registrado seguro!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("record_payment_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrar Saída de Pagamento", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // TITLE FOR HISTORY
        item {
            Text(
                text = "Histórico de Pagamentos Realizados",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (payments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum pagamento registrado nesta partição.",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(payments, key = { it.id }) { pay ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
                                Text(pay.employeeName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Semana: ${pay.week}", fontSize = 12.sp, color = OnSurfaceVariant)
                                Text("Data: ${pay.paymentDate}", fontSize = 12.sp, color = OnSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Anotado no Fluxo de Caixa Geral", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = String.format(Locale("pt", "BR"), "R$ %,.2f", pay.amount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ErrorColor
                            )

                            IconButton(
                                onClick = {
                                    viewModel.deleteEmployeePayment(pay.id)
                                    Toast.makeText(context, "Pagamento excluído!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir Pagamento", tint = ErrorColor.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MasterRegistriesTab(viewModel: TransactionViewModel) {
    val context = LocalContext.current
    var activeListSection by remember { mutableStateOf("CLIENTES") } // "CLIENTES", "FUNCIONARIOS", "MODELOS", "CATEGS"

    val Primary = MaterialTheme.colorScheme.primary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val SurfaceContainerHigh = MaterialTheme.colorScheme.surfaceVariant
    val ErrorColor = MaterialTheme.colorScheme.error

    // Lists loaded reactively
    val clients by viewModel.allClients.collectAsStateWithLifecycle()
    val employees by viewModel.allEmployees.collectAsStateWithLifecycle()
    val productModels by viewModel.allProductModels.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()

    // Dialog state
    var showAddEditClientDialog by remember { mutableStateOf<ClientEntity?>(null) }
    var showAddClientDialog by remember { mutableStateOf(false) }

    var showAddEditEmployeeDialog by remember { mutableStateOf<EmployeeEntity?>(null) }
    var showAddEmployeeDialog by remember { mutableStateOf(false) }

    var showAddEditModelDialog by remember { mutableStateOf<ProductModelEntity?>(null) }
    var showAddModelDialog by remember { mutableStateOf(false) }

    var showAddEditCategoryDialog by remember { mutableStateOf<CategoryEntity?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Dropdown or Row of Pill selectors for sub-directories
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val listDirs = listOf(
                "CLIENTES" to "Clientes",
                "FUNCIONARIOS" to "Funcionários",
                "MODELOS" to "Modelos de Peça",
                "CATEGS" to "Nomes de Gastos"
            )
            items(listDirs, key = { it.first }) { (key, label) ->
                val isSelected = activeListSection == key
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
                        .clickable { activeListSection = key }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Primary else OnSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cadastros Registrados",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )

            Button(
                onClick = {
                    when (activeListSection) {
                        "CLIENTES" -> showAddClientDialog = true
                        "FUNCIONARIOS" -> showAddEmployeeDialog = true
                        "MODELOS" -> showAddModelDialog = true
                        "CATEGS" -> showAddCategoryDialog = true
                    }
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha = 0.15f), contentColor = Primary),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CRUD LISTINGS FOR THE SECTIONS
        when (activeListSection) {
            "CLIENTES" -> {
                if (clients.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum cliente cadastrado ainda.", fontSize = 13.sp, color = OnSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(clients, key = { it.id }) { client ->
                            ItemRowCard(
                                title = client.name,
                                subtitle = if (client.phone.isNotEmpty()) "Tel: ${client.phone}" else "Sem telefone",
                                icon = Icons.Default.Person,
                                onEdit = { showAddEditClientDialog = client },
                                onDelete = {
                                    viewModel.deleteClient(client.id)
                                    Toast.makeText(context, "Cliente apagado!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
            "FUNCIONARIOS" -> {
                if (employees.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum funcionário cadastrado ainda.", fontSize = 13.sp, color = OnSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(employees, key = { it.id }) { emp ->
                            ItemRowCard(
                                title = emp.name,
                                subtitle = emp.role,
                                icon = Icons.Default.AccountBox,
                                onEdit = { showAddEditEmployeeDialog = emp },
                                onDelete = {
                                    viewModel.deleteEmployee(emp.id)
                                    Toast.makeText(context, "Funcionário apagado!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
            "MODELOS" -> {
                if (productModels.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum modelo cadastrado ainda.", fontSize = 13.sp, color = OnSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(productModels, key = { it.id }) { model ->
                            ItemRowCard(
                                title = model.name,
                                subtitle = "Modelo de Peça registrado",
                                icon = Icons.Default.Build,
                                onEdit = { showAddEditModelDialog = model },
                                onDelete = {
                                    viewModel.deleteProductModel(model.id)
                                    Toast.makeText(context, "Modelo apagado!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
            "CATEGS" -> {
                val expenseCategories = categories.filter { it.type == "OUTFLOW" }
                if (expenseCategories.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhuma categoria de despesa cadastrada.", fontSize = 13.sp, color = OnSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(expenseCategories, key = { it.id }) { cat ->
                            ItemRowCard(
                                title = cat.name,
                                subtitle = "Nome de Gasto / Despesa",
                                icon = Icons.Default.List,
                                onEdit = { showAddEditCategoryDialog = cat },
                                onDelete = {
                                    viewModel.deleteCategory(cat.id)
                                    Toast.makeText(context, "Gasto / Despesa removido!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- CLIENT DIALOGS ---
    if (showAddClientDialog) {
        AddEditClientDialog(
            client = null,
            onDismiss = { showAddClientDialog = false },
            onSave = { name, phone ->
                viewModel.addClient(name, phone)
                showAddClientDialog = false
                Toast.makeText(context, "Cliente adicionado com sucesso!", Toast.LENGTH_SHORT).show()
            }
        )
    }
    showAddEditClientDialog?.let { client ->
        AddEditClientDialog(
            client = client,
            onDismiss = { showAddEditClientDialog = null },
            onSave = { name, phone ->
                viewModel.updateClient(client.id, name, phone)
                showAddEditClientDialog = null
                Toast.makeText(context, "Cliente atualizado com sucesso!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- EMPLOYEE DIALOGS ---
    if (showAddEmployeeDialog) {
        AddEditEmployeeDialog(
            employee = null,
            onDismiss = { showAddEmployeeDialog = false },
            onSave = { name, role ->
                viewModel.addEmployee(name, role)
                showAddEmployeeDialog = false
                Toast.makeText(context, "Funcionário adicionado com sucesso!", Toast.LENGTH_SHORT).show()
            }
        )
    }
    showAddEditEmployeeDialog?.let { emp ->
        AddEditEmployeeDialog(
            employee = emp,
            onDismiss = { showAddEditEmployeeDialog = null },
            onSave = { name, role ->
                viewModel.updateEmployee(emp.id, name, role)
                showAddEditEmployeeDialog = null
                Toast.makeText(context, "Funcionário atualizado com sucesso!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- PRODUCT MODEL DIALOGS ---
    if (showAddModelDialog) {
        AddEditProductModelDialog(
            model = null,
            onDismiss = { showAddModelDialog = false },
            onSave = { name ->
                viewModel.addProductModel(name)
                showAddModelDialog = false
                Toast.makeText(context, "Modelo adicionado com sucesso!", Toast.LENGTH_SHORT).show()
            }
        )
    }
    showAddEditModelDialog?.let { model ->
        AddEditProductModelDialog(
            model = model,
            onDismiss = { showAddEditModelDialog = null },
            onSave = { name ->
                viewModel.updateProductModel(model.id, name)
                showAddEditModelDialog = null
                Toast.makeText(context, "Modelo de peça atualizado!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- CATEGORY DIALOGS ---
    if (showAddCategoryDialog) {
        AddEditCategoryDialog(
            category = null,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name ->
                viewModel.addCategory(name, "OUTFLOW")
                showAddCategoryDialog = false
                Toast.makeText(context, "Gasto adicionado ao catálogo!", Toast.LENGTH_SHORT).show()
            }
        )
    }
    showAddEditCategoryDialog?.let { cat ->
        AddEditCategoryDialog(
            category = cat,
            onDismiss = { showAddEditCategoryDialog = null },
            onSave = { name ->
                viewModel.updateCategory(cat.id, name, "OUTFLOW")
                showAddEditCategoryDialog = null
                Toast.makeText(context, "Gasto catálogo atualizado!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun ItemRowCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val Primary = MaterialTheme.colorScheme.primary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val ErrorColor = MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Primary)
                Column {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text(subtitle, fontSize = 12.sp, color = OnSurfaceVariant)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = Primary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", tint = ErrorColor.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// --- MICRO DIALOG COMPOSABLES ---

@Composable
fun AddEditClientDialog(
    client: ClientEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(client?.name ?: "") }
    var phone by remember { mutableStateOf(client?.phone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (client == null) "Novo Cliente" else "Editar Cliente", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Cliente", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone (Opcional)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.trim().isNotEmpty()) onSave(name.trim(), phone.trim()) },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddEditEmployeeDialog(
    employee: EmployeeEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(employee?.name ?: "") }
    var role by remember { mutableStateOf(employee?.role ?: "Costureira") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (employee == null) "Novo Funcionário" else "Editar Funcionário", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Funcionário", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Cargo (Ex: Costureira)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.trim().isNotEmpty()) onSave(name.trim(), role.trim()) },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddEditProductModelDialog(
    model: ProductModelEntity?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(model?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (model == null) "Novo Modelo de Peça" else "Editar Modelo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Modelo / Produto", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.trim().isNotEmpty()) onSave(name.trim()) },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddEditCategoryDialog(
    category: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Novo Tipo de Gasto / Despesa" else "Editar Gasto", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome da Categoria de Gasto", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.trim().isNotEmpty()) onSave(name.trim()) },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
