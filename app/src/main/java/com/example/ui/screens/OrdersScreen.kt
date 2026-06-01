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

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    var selectedOrderWeek by remember { mutableStateOf("Tudo") }
    var showAddDialog by remember { mutableStateOf(false) }
    var orderToEdit by remember { mutableStateOf<com.example.data.OrderEntity?>(null) }
    var orderForInvoice by remember { mutableStateOf<com.example.data.OrderEntity?>(null) }

    val filteredOrders = remember(selectedOrderWeek, orders) {
        if (selectedOrderWeek == "Tudo") orders else orders.filter { it.week == selectedOrderWeek }
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
                                .background(
                                    if (isSelected) Primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(32.dp)
                                )
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
            if (filteredOrders.isEmpty()) {
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
                items(filteredOrders, key = { it.id }) { order ->
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
                                Column {
                                    Text(
                                        text = order.clientName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Text(
                                        text = "${order.pantyType} - Tam ${order.pantySize}",
                                        fontSize = 13.sp,
                                        color = OnSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = order.week,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
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
                                    Text("QUANTIDADE", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text("${order.quantity} unidades", fontSize = 14.sp, color = OnSurface, fontWeight = FontWeight.SemiBold)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("TOTAL DO PEDIDO", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", order.totalValue),
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
                                // PDF slip invoice icon
                                OutlinedButton(
                                    onClick = { orderForInvoice = order },
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
                                    Text("Comanda (PDF)", fontSize = 12.sp)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { orderToEdit = order },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.05f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar Pedido",
                                            tint = Primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.deleteOrder(order.id)
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.05f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Apagar Pedido",
                                            tint = ErrorColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
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
            onDismiss = { showAddDialog = false },
            onSave = { name, model, size, qty, valUnit, week ->
                viewModel.addOrder(name, model, size, qty, valUnit, week)
                showAddDialog = false
                Toast.makeText(context, "Pedido agendado com sucesso!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    orderToEdit?.let { order ->
        OrderAddEditDialog(
            order = order,
            onDismiss = { orderToEdit = null },
            onSave = { name, model, size, qty, valUnit, week ->
                viewModel.editOrder(order.id, name, model, size, qty, valUnit, week)
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
}

@Composable
fun OrderAddEditDialog(
    order: com.example.data.OrderEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, Double, String) -> Unit
) {
    var name by remember { mutableStateOf(order?.clientName ?: "") }
    var model by remember { mutableStateOf(order?.pantyType ?: "") }
    var size by remember { mutableStateOf(order?.pantySize ?: "M") }
    var qtyText by remember { mutableStateOf(order?.quantity?.toString() ?: "100") }
    var priceText by remember { mutableStateOf(order?.pantyValue?.toString() ?: "1.15") }
    var selectedWeek by remember { mutableStateOf(order?.week ?: "1ª Semana") }

    val totalValue = (qtyText.toIntOrNull() ?: 0) * (priceText.replace(',', '.').toDoubleOrNull() ?: 0.0)

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
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Cliente", color = OnSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

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

                // Week selector Dropdown or chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Semana do Pedido", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val weeks = listOf("1ª Semana", "2ª Semana", "3ª Semana", "4ª Semana", "5ª Semana")
                        items(weeks, key = { it }) { w ->
                            val isSelected = selectedWeek == w
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (isSelected) Primary else Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                    .clickable { selectedWeek = w }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(w, color = if (isSelected) Primary else OnSurface, fontSize = 12.sp)
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
                        onSave(name, model, size, qty, valUnit, selectedWeek)
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
                        text = "MS MODA ÍNTIMA - PRODUÇÃO",
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
