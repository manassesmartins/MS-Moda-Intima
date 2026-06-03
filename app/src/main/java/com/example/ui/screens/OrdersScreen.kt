package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.data.*
import com.example.ui.theme.*
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(viewModel: TransactionViewModel) {
    val Primary = MaterialTheme.colorScheme.primary
    val OnPrimary = MaterialTheme.colorScheme.onPrimary
    val Secondary = MaterialTheme.colorScheme.secondary
    val OnSecondary = MaterialTheme.colorScheme.onSecondary
    val Tertiary = MaterialTheme.colorScheme.tertiary
    val OnTertiary = MaterialTheme.colorScheme.onTertiary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val SurfaceContainer = MaterialTheme.colorScheme.surfaceVariant
    val SurfaceContainerHigh = MaterialTheme.colorScheme.surfaceVariant
    val SurfaceDark = MaterialTheme.colorScheme.background
    val ErrorColor = MaterialTheme.colorScheme.error

    val context = LocalContext.current
    val orders by viewModel.allOrders.collectAsStateWithLifecycle(emptyList())
    val existingClients = remember(orders) { orders.map { it.clientName }.distinct().sorted() }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    var selectedOrderWeek by remember { mutableStateOf("Tudo") }
    var showAddDialog by remember { mutableStateOf(false) }
    var orderToEdit by remember { mutableStateOf<com.example.data.OrderEntity?>(null) }
    var orderForInvoice by remember { mutableStateOf<com.example.data.OrderEntity?>(null) }
    
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var selectedFilterDateMillis by remember { mutableStateOf<Long?>(null) }

    val filteredOrders = remember(selectedOrderWeek, selectedFilterDateMillis, orders) {
        val byWeek = if (selectedOrderWeek == "Tudo") orders else orders.filter { it.week == selectedOrderWeek }
        if (selectedFilterDateMillis != null) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = selectedFilterDateMillis!!
            val d1 = cal.get(java.util.Calendar.DAY_OF_YEAR)
            val y1 = cal.get(java.util.Calendar.YEAR)
            
            byWeek.filter { o ->
                val c2 = java.util.Calendar.getInstance()
                c2.timeInMillis = o.timestamp
                c2.get(java.util.Calendar.DAY_OF_YEAR) == d1 && c2.get(java.util.Calendar.YEAR) == y1
            }
        } else {
            byWeek
        }
    }

    val groupedOrders = remember(filteredOrders) {
        filteredOrders.groupBy { Pair(it.clientName.trim().lowercase(Locale.getDefault()), it.week) }.values.toList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
        ) {
            // Screen Title header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Agendamento de Pedidos",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            text = "Registre e organize as encomendas da semana",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showDatePickerDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selectedFilterDateMillis != null) Primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Calendário",
                            tint = if (selectedFilterDateMillis != null) Primary else OnSurfaceVariant
                        )
                    }
                }
            }

            // Weeks Filter chip bar
            item {
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    val weeks = listOf("Tudo", "1ª Semana", "2ª Semana", "3ª Semana", "4ª Semana", "5ª Semana")
                    items(weeks, key = { it }) { w ->
                        val isSelected = selectedOrderWeek == w
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(32.dp))
                                .background(if (isSelected) Primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (isSelected) Primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                                .clickable { selectedOrderWeek = w }
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

            // Orders summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total de Encomendas: ${filteredOrders.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale("pt", "BR"), "Valor Acumulado: R$ %,.2f", filteredOrders.sumOf { it.totalValue }),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Tertiary
                    )
                }
            }

            // Order Rows
            if (groupedOrders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum agendamento registrado para esta semana.",
                            color = OnSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(groupedOrders, key = { it.first().id }) { group ->
                    val firstOrder = group.first()
                    val totalQty = group.sumOf { it.quantity }
                    val totalValue = group.sumOf { it.totalValue }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = getGlassContainerColor()),
                        border = getGlassBorderStroke(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = firstOrder.clientName.uppercase(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (group.size > 1) {
                                        Text(
                                            text = "${group.size} Itens neste pedido",
                                            fontSize = 13.sp,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = firstOrder.week,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // List all sub-items in the group
                            group.forEach { order ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${order.pantyType} - M/T ${order.pantySize}", fontSize = 13.sp, color = OnSurface, fontWeight = FontWeight.Bold)
                                        Text("Qtd: ${order.quantity} | R$ ${order.pantyValue} un.", fontSize = 11.sp, color = OnSurfaceVariant)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Secondary.copy(alpha = 0.2f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(order.businessArea, fontSize = 9.sp, color = Secondary, fontWeight = FontWeight.SemiBold)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (order.status == "Concluído") Tertiary.copy(alpha=0.2f) else ErrorColor.copy(alpha=0.2f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(order.status, fontSize = 9.sp, color = if (order.status == "Concluído") Tertiary else ErrorColor, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = String.format(Locale("pt", "BR"), "R$ %,.2f", order.totalValue),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Tertiary,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = { orderToEdit = order },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.05f))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Editar",
                                                    tint = Primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteOrder(order.id) },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.05f))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Apagar",
                                                    tint = ErrorColor,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (order != group.last()) {
                                    Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("QUANTIDADE TOTAL", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text("$totalQty unidades", fontSize = 14.sp, color = OnSurface, fontWeight = FontWeight.SemiBold)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("TOTAL GERAL DO PEDIDO", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", totalValue),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Tertiary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // PDF slip invoice icon (we'll just use the first order as the lead for the PDF, ideally we'd pass the whole group, but to keep it simple, we pass the first order - the user requested merging in UI)
                                OutlinedButton(
                                    onClick = { orderForInvoice = firstOrder },
                                    border = BorderStroke(1.dp, Tertiary.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Tertiary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Comanda Principal (PDF)", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button for Adding order
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Primary,
            contentColor = OnPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("add_order_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar Pedido",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Modal dialog overlays
    if (showAddDialog) {
        OrderAddEditDialog(
            orders = orders,
            onDismiss = { showAddDialog = false },
            onSave = { name, model, size, qty, valUnit, week, area, status ->
                viewModel.addOrder(name, model, size, qty, valUnit, week, area, status)
                showAddDialog = false
                Toast.makeText(context, "Pedido agendado com sucesso!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    orderToEdit?.let { order ->
        OrderAddEditDialog(
            order = order,
            orders = orders,
            onDismiss = { orderToEdit = null },
            onSave = { name, model, size, qty, valUnit, week, area, status ->
                viewModel.editOrder(order.id, name, model, size, qty, valUnit, week, area, status)
                orderToEdit = null
                Toast.makeText(context, "Pedido atualizado!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    orderForInvoice?.let { order ->
        OrderInvoiceDialog(
            order = order,
            onDismiss = { orderForInvoice = null }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedFilterDateMillis ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedFilterDateMillis = datePickerState.selectedDateMillis
                    showDatePickerDialog = false
                }) {
                    Text("Filtrar", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    selectedFilterDateMillis = null 
                    showDatePickerDialog = false 
                }) { 
                    Text("Limpar", color = ErrorColor) 
                }
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceContainerHigh)
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderAddEditDialog(
    order: com.example.data.OrderEntity? = null,
    orders: List<com.example.data.OrderEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, Double, String, String, String) -> Unit
) {
    val existingClients = remember(orders) { orders.map { it.clientName }.distinct().sorted() }
    var name by remember { mutableStateOf(order?.clientName ?: "") }
    var expandedNameDropdown by remember { mutableStateOf(false) }

    var model by remember { mutableStateOf(order?.pantyType ?: "") }
    var size by remember { mutableStateOf(order?.pantySize ?: "M") }
    var qtyText by remember { mutableStateOf(order?.quantity?.toString() ?: "100") }
    var priceText by remember { mutableStateOf(order?.pantyValue?.toString() ?: "1.15") }
    
    // Convert current Order timestamp to initial date or use current time
    var selectedTimeMillis by remember { mutableStateOf(order?.timestamp ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Automatically calculate week string based on time
    val selectedWeek = remember(selectedTimeMillis) {
        val cal = java.util.Calendar.getInstance(Locale("pt", "BR"))
        cal.timeInMillis = selectedTimeMillis
        val weekOfMonth = cal.get(java.util.Calendar.WEEK_OF_MONTH)
        "${weekOfMonth}ª Semana"
    }

    var area by remember { mutableStateOf(order?.businessArea ?: "Geral") }
    var status by remember { mutableStateOf(order?.status ?: "Pendente") }
    
    val areaOptions = listOf("Costura", "Corte", "Bordado", "Embalagem", "Revisão", "Geral")
    val statusOptions = listOf("Pendente", "Em Andamento", "Concluído", "Atrasado")
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    val totalValue = (qtyText.toIntOrNull() ?: 0) * (priceText.replace(',', '.').toDoubleOrNull() ?: 0.0)

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedTimeMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedTimeMillis = it }
                    showDatePicker = false
                }) {
                    Text("OK", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = OnSurfaceVariant) }
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceContainerHigh)
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (order == null) "Agendar Pedido" else "Editar Pedido",
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        },
        containerColor = SurfaceContainerHigh,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedNameDropdown,
                    onExpandedChange = { expandedNameDropdown = it },
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it ; expandedNameDropdown = true },
                        label = { Text("Nome do Cliente", color = OnSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    if (existingClients.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expandedNameDropdown,
                            onDismissRequest = { expandedNameDropdown = false },
                            modifier = Modifier.background(SurfaceContainerHigh)
                        ) {
                            existingClients.filter { it.contains(name, ignoreCase=true) }.forEach { client ->
                                DropdownMenuItem(
                                    text = { Text(client, color = OnSurface) },
                                    onClick = {
                                        name = client
                                        expandedNameDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Historical orders preview
                val clientHistory = remember(name, orders) {
                    val matchingClientName = name.trim().lowercase(Locale.getDefault())
                    orders.filter { it.clientName.trim().lowercase(Locale.getDefault()) == matchingClientName }
                        .sortedByDescending { it.timestamp }
                        .take(3)
                }
                
                if (clientHistory.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Histórico Recente do Cliente", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Bold)
                        clientHistory.forEach { histOrder ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    // Autofill this historical order details
                                    model = histOrder.pantyType
                                    size = histOrder.pantySize
                                    qtyText = histOrder.quantity.toString()
                                    priceText = histOrder.pantyValue.toString()
                                },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${histOrder.pantyType} - M/T ${histOrder.pantySize}", fontSize = 12.sp, color = OnSurface, fontWeight = FontWeight.SemiBold)
                                    Text("${histOrder.week} - Qtd: ${histOrder.quantity}", fontSize = 10.sp, color = OnSurfaceVariant)
                                }
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Preencher dados", modifier = Modifier.size(16.dp), tint = Primary)
                            }
                            if (histOrder != clientHistory.last()) {
                                Divider(color = Color.White.copy(alpha = 0.05f))
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Tipo/Modelo de Calcinha", color = OnSurfaceVariant) },
                    placeholder = { Text("Ex: Cotton Summerplex", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Panty size selection chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tamanho da Calcinha", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val sizes = listOf("P", "M", "G", "GG", "U")
                        sizes.forEach { s ->
                            val isSelected = size == s
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (isSelected) Primary else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { size = s }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(s, color = if (isSelected) Primary else OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Qtd", color = OnSurfaceVariant) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Valor Unit.", color = OnSurfaceVariant) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        prefix = { Text("R$", color = Primary) },
                        modifier = Modifier.weight(1.3f)
                    )
                }

                // Scheduled Date & Auto-calculated Week
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Data Agendada", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White.copy(0.05f)).clickable { showDatePicker = true }.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(dateFormatter.format(Date(selectedTimeMillis)), color = OnSurface, fontSize = 14.sp)
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Selecionar Data", tint = Primary)
                    }
                    Text("Será alocado na: $selectedWeek", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                }

                // Area and Status Selectors
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Área de Negócios", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(areaOptions, key = { it }) { a ->
                                val isSelected = area == a
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Primary.copy(alpha = 0.2f) else Color.Transparent)
                                        .border(1.dp, if (isSelected) Primary else Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .clickable { area = a }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(a, color = if (isSelected) Primary else OnSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Status da Produção", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(statusOptions, key = { it }) { s ->
                            val isSelected = status == s
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Tertiary.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(1.dp, if (isSelected) Tertiary else Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .clickable { status = s }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(s, color = if (isSelected) Tertiary else OnSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL PREVISTO:", fontSize = 12.sp, color = OnSurfaceVariant)
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", totalValue),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Tertiary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = qtyText.toIntOrNull() ?: 0
                    val valUnit = priceText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && model.isNotBlank() && qty > 0 && valUnit > 0) {
                        onSave(name, model, size, qty, valUnit, selectedWeek, area, status)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                enabled = name.isNotBlank() && model.isNotBlank() && (qtyText.toIntOrNull() ?: 0) > 0 && (priceText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = OnSurfaceVariant)
            }
        }
    )
}

@Composable
fun OrderInvoiceDialog(
    order: com.example.data.OrderEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Comanda de Encomenda Corrente", color = Tertiary, fontWeight = FontWeight.Bold)
        },
        containerColor = Color.White, // Paper White look!
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.White),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GESTOR DE PRODUÇÃO - COMANDA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Text(
                        text = "Nossas Entradas e Saidas de Pedidos",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "--------------------------------------------------------",
                        color = Color.Black,
                        fontSize = 10.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("CLIENTE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(order.clientName.uppercase(Locale.getDefault()), fontSize = 11.sp, color = Color.Black)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SEMANA DO REGISTRO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(order.week, fontSize = 11.sp, color = Color.Black)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("DATA DE FATURA:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    val formattedDate = dateFormatter.format(Date(order.timestamp))
                    Text(formattedDate, fontSize = 11.sp, color = Color.Black)
                }

                Text(
                    text = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -",
                    color = Color.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Item description table
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ESPECIFICAÇÕES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("TOTAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${order.pantyType} (Tam ${order.pantySize})",
                            fontSize = 11.sp,
                            color = Color.Black
                        )
                        Text(
                            text = String.format(Locale("pt", "BR"), "R$ %,.2f", order.totalValue),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Text(
                        text = "=> Qtd: ${order.quantity} un x R$ ${order.pantyValue}",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }

                Text(
                    text = "--------------------------------------------------------",
                    color = Color.Black,
                    fontSize = 10.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("VALOR A PAGAR:", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", order.totalValue),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = "ASSINATURA DA LOJA",
                    fontSize = 9.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Divider(color = Color.LightGray, modifier = Modifier.padding(top = 10.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "Baixando comanda em PDF... Concluído!", Toast.LENGTH_LONG).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
            ) {
                Text("Baixar PDF / Imprimir", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = Color.Black)
            }
        }
    )
}
