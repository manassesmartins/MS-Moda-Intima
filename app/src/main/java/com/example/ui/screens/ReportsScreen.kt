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

@Composable
fun ReportsScreen(viewModel: TransactionViewModel) {
    /* Commented out old redundant screen to avoid unused code conflicts
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle(emptyList())
    val orders by viewModel.allOrders.collectAsStateWithLifecycle(emptyList())

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    var activeSubTab by remember { mutableStateOf("SEMANAL") } // "SEMANAL" or "MENSAL"

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Switcher Tab Bar at top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("SEMANAL" to "Apurado Semanal", "MENSAL" to "Histórico Mensal / Anual")
            tabs.forEach { (subId, label) ->
                val isSel = activeSubTab == subId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) Primary else Color.Transparent)
                        .clickable { activeSubTab = subId }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSel) OnPrimary else OnSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (activeSubTab == "SEMANAL") {
            var selectedReportWeek by remember { mutableStateOf("Tudo") }

            // Grouping / filtering of active items optimized with remember to improve fluid execution and conserve RAM
            val outflowTransactions = remember(transactions) { transactions.filter { it.type == "OUTFLOW" } }
            
            val filteredOrders = remember(selectedReportWeek, orders) {
                if (selectedReportWeek == "Tudo") orders else orders.filter { it.week == selectedReportWeek }
            }
            val filteredOutflows = remember(selectedReportWeek, outflowTransactions) {
                if (selectedReportWeek == "Tudo") outflowTransactions else outflowTransactions.filter { it.week == selectedReportWeek }
            }

            // Financial calculations cached using remember to avoid heap allocation on every recomposition
            val totalOrdersValue = remember(filteredOrders) { filteredOrders.sumOf { it.totalValue } }
            val totalExpensesValue = remember(filteredOutflows) { filteredOutflows.sumOf { it.amount } }
            
            val profit = remember(totalOrdersValue, totalExpensesValue) { totalOrdersValue - totalExpensesValue }
            val tithing = remember(profit) { if (profit > 0) profit * 0.10 else 0.0 }
            val tithingSplit = remember(tithing) { tithing / 2.0 }
            val totalPantyFabricated = remember(filteredOrders) { filteredOrders.sumOf { it.quantity } }
            
            val profitPercentage = remember(profit, totalOrdersValue) { if (totalOrdersValue > 0) (profit / totalOrdersValue) * 105 else 0.0 }

            // Categorized outflows mapping cached in memory
            val expensesByCategory = remember(filteredOutflows) {
                val standardCategories = listOf(
                    "Funcionários", "Pano", "Viés", "Linha", 
                    "Etiqueta de composição", "Etiqueta lateral", "Forro", 
                    "Manutenção", "Variados"
                )
                val map = standardCategories.associateWith { 0.0 }.toMutableMap()
                filteredOutflows.forEach { out ->
                    val matchedCat = if (out.category in standardCategories) out.category else "Variados"
                    map[matchedCat] = (map[matchedCat] ?: 0.0) + out.amount
                }
                map
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                // Week filter chip bar
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Filtrar por Semana",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val weeks = listOf("Tudo", "1ª Semana", "2ª Semana", "3ª Semana", "4ª Semana", "5ª Semana")
                            items(weeks, key = { it }) { w ->
                                val isSelected = selectedReportWeek == w
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
                                        .clickable { selectedReportWeek = w }
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

                // Summary Statistics Cards
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Apurado do Período",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = getGlassContainerColor()),
                                border = getGlassBorderStroke(),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(getGlassBorderStroke(1.dp), RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("ENTRADAS (Faturamento)", fontSize = 11.sp, color = OnSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", totalOrdersValue),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Tertiary
                                    )
                                }
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = getGlassContainerColor()),
                                border = getGlassBorderStroke(),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(getGlassBorderStroke(1.dp), RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("SAÍDAS (Despesas)", fontSize = 11.sp, color = OnSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", totalExpensesValue),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Detailed spreadsheet indicators
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "DRE / Fechamento do Período",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )

                        Divider(color = Color.White.copy(alpha = 0.08f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("LUCRO BRUTO", fontSize = 13.sp, color = OnSurfaceVariant)
                            Text(
                                text = String.format(Locale("pt", "BR"), "R$ %,.2f", profit),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profit >= 0) Tertiary else ErrorColor
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("DÍZIMO (10% do Lucro)", fontSize = 13.sp, color = OnSurfaceVariant)
                            Text(
                                text = String.format(Locale("pt", "BR"), "R$ %,.2f", tithing),
                                fontSize = 13.sp,
                                color = OnSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("DÍZIMO DIVIDIDO POR 2", fontSize = 13.sp, color = OnSurfaceVariant)
                            Text(
                                text = String.format(Locale("pt", "BR"), "R$ %,.2f", tithingSplit),
                                fontSize = 13.sp,
                                color = OnSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TOTAL DE CALCINHAS FABRICADAS", fontSize = 13.sp, color = OnSurfaceVariant)
                            Text(
                                text = "$totalPantyFabricated calcinhas",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Secondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PERCENTUAL DO LUCRO", fontSize = 13.sp, color = OnSurfaceVariant)
                            Text(
                                text = String.format(Locale("pt", "BR"), "%.2f %%", profitPercentage),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profitPercentage >= 35.0) Tertiary else ErrorColor
                            )
                        }

                        // Status banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (profitPercentage >= 35.0) Tertiary.copy(alpha = 0.12f)
                                    else ErrorColor.copy(alpha = 0.12f)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (profitPercentage >= 35.0) 
                                    "OPERAÇÃO ALTAMENTE LUCRATIVA 👍" 
                                    else "ALERTA: MARGEM DE LUCRO BAIXA OU PREJUÍZO ⚠️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profitPercentage >= 35.0) Tertiary else ErrorColor,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                // Detailed Outflows (Gastos com matérias-primas e equipe)
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Detalhamento de Saídas (Custos)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = getGlassContainerColor()),
                            border = getGlassBorderStroke(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val maxExpense = expensesByCategory.values.maxOrNull() ?: 1.0
                                expensesByCategory.forEach { (cat, amt) ->
                                    val pct = if (maxExpense > 0) (amt / maxExpense).toFloat() else 0f
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(cat, fontSize = 13.sp, color = OnSurface, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = String.format(Locale("pt", "BR"), "R$ %,.2f", amt),
                                                fontSize = 13.sp,
                                                color = OnSurfaceVariant
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = pct,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = if (cat == "Funcionários") ErrorColor else Primary,
                                            trackColor = Color.White.copy(alpha = 0.1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Export Button
                item {
                    Button(
                        onClick = {
                            Toast.makeText(
                                context,
                                "Relatório consolidado exportado com sucesso localmente!",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = getGlassContainerColor(),
                            contentColor = Primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = getGlassBorderStroke(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_pdf_button")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Exportar Relatório",
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Exportar Relatório Mensal",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        } else {
            // MENSAL HISTORY TAB WITH SEARCH AND DATES OF LAST ALTERATION!
            var selectedMonth by remember { mutableStateOf(4) } // Default to 4 (Maio / May)
            var selectedYear by remember { mutableStateOf(2026) } // Default to 2026
            var searchQuery by remember { mutableStateOf("") }

            val monthNames = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
            val yearList = listOf(2026, 2025, 2024)

            val calendar = remember { java.util.Calendar.getInstance() }

            val monthlyOrders = remember(orders, selectedMonth, selectedYear, searchQuery) {
                orders.filter { order ->
                    calendar.timeInMillis = order.timestamp
                    val m = calendar.get(java.util.Calendar.MONTH)
                    val y = calendar.get(java.util.Calendar.YEAR)
                    m == selectedMonth && y == selectedYear && (
                        searchQuery.isEmpty() ||
                        order.clientName.contains(searchQuery, ignoreCase = true) ||
                        order.pantyType.contains(searchQuery, ignoreCase = true) ||
                        order.pantySize.contains(searchQuery, ignoreCase = true)
                    )
                }
            }

            val monthlyInflowTransactions = remember(transactions, selectedMonth, selectedYear, searchQuery) {
                transactions.filter { t ->
                    calendar.timeInMillis = t.timestamp
                    val m = calendar.get(java.util.Calendar.MONTH)
                    val y = calendar.get(java.util.Calendar.YEAR)
                    t.type == "INFLOW" && m == selectedMonth && y == selectedYear && (
                        searchQuery.isEmpty() ||
                        t.description.contains(searchQuery, ignoreCase = true) ||
                        t.category.contains(searchQuery, ignoreCase = true) ||
                        t.extraText.contains(searchQuery, ignoreCase = true)
                    )
                }
            }

            val monthlyOutflowTransactions = remember(transactions, selectedMonth, selectedYear, searchQuery) {
                transactions.filter { t ->
                    calendar.timeInMillis = t.timestamp
                    val m = calendar.get(java.util.Calendar.MONTH)
                    val y = calendar.get(java.util.Calendar.YEAR)
                    t.type == "OUTFLOW" && m == selectedMonth && y == selectedYear && (
                        searchQuery.isEmpty() ||
                        t.description.contains(searchQuery, ignoreCase = true) ||
                        t.category.contains(searchQuery, ignoreCase = true) ||
                        t.extraText.contains(searchQuery, ignoreCase = true)
                    )
                }
            }

            // Calculations optimized with remember
            val totalInflowValue = remember(monthlyOrders, monthlyInflowTransactions) {
                monthlyOrders.sumOf { it.totalValue } + monthlyInflowTransactions.sumOf { it.amount }
            }
            val totalOutflowValue = remember(monthlyOutflowTransactions) {
                monthlyOutflowTransactions.sumOf { it.amount }
            }
            val monthlyBalance = remember(totalInflowValue, totalOutflowValue) {
                totalInflowValue - totalOutflowValue
            }
            val monthlyTithing = remember(monthlyBalance) { if (monthlyBalance > 0) monthlyBalance * 0.10 else 0.0 }
            val monthlyTithingSplit = remember(monthlyTithing) { monthlyTithing / 2.0 }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
            ) {
                // Dropdown selectors for Month and Year
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Month Dropdown Trigger
                        var showMonthDropdown by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .clickable { showMonthDropdown = true }
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Mês: " + monthNames[selectedMonth],
                                color = OnSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            DropdownMenu(
                                expanded = showMonthDropdown,
                                onDismissRequest = { showMonthDropdown = false },
                                modifier = Modifier.background(SurfaceDark).border(1.dp, Color.White.copy(alpha = 0.12f))
                            ) {
                                monthNames.forEachIndexed { index, name ->
                                    DropdownMenuItem(
                                        text = { Text(name, color = OnSurface) },
                                        onClick = {
                                            selectedMonth = index
                                            showMonthDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Year Dropdown Trigger
                        var showYearDropdown by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .clickable { showYearDropdown = true }
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Ano: " + selectedYear,
                                color = OnSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            DropdownMenu(
                                expanded = showYearDropdown,
                                onDismissRequest = { showYearDropdown = false },
                                modifier = Modifier.background(SurfaceDark).border(1.dp, Color.White.copy(alpha = 0.12f))
                            ) {
                                yearList.forEach { yr ->
                                    DropdownMenuItem(
                                        text = { Text(yr.toString(), color = OnSurface) },
                                        onClick = {
                                            selectedYear = yr
                                            showYearDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar pedidos ou compras (ex: Sandro, Pano)", fontSize = 13.sp, color = OnSurfaceVariant.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = OnSurfaceVariant) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar", tint = OnSurfaceVariant)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color.White.copy(alpha = 0.02f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Financial Overview Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = getGlassContainerColor()),
                        border = getGlassBorderStroke()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Apurado Geral do Mês", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Primary)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Entradas (Faturamento)", color = OnSurfaceVariant, fontSize = 13.sp)
                                Text(
                                    text = String.format(Locale("pt", "BR"), "R$ %,.2f", totalInflowValue),
                                    color = Tertiary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Saídas (Despesas/Compras)", color = OnSurfaceVariant, fontSize = 13.sp)
                                Text(
                                    text = String.format(Locale("pt", "BR"), "R$ %,.2f", totalOutflowValue),
                                    color = ErrorColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Resultado Líquido", color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format(Locale("pt", "BR"), "R$ %,.2f", monthlyBalance),
                                    color = if (monthlyBalance >= 0) Tertiary else ErrorColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("DÍZIMO (10% do Resultado)", fontSize = 13.sp, color = OnSurfaceVariant)
                                Text(
                                    text = String.format(Locale("pt", "BR"), "R$ %,.2f", monthlyTithing),
                                    fontSize = 13.sp,
                                    color = OnSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("DÍZIMO DIVIDIDO POR 2", fontSize = 13.sp, color = OnSurfaceVariant)
                                Text(
                                    text = String.format(Locale("pt", "BR"), "R$ %,.2f", monthlyTithingSplit),
                                    fontSize = 13.sp,
                                    color = OnSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Title Section
                item {
                    Text(
                        text = "Resultados Encontrados (${monthlyOrders.size + monthlyInflowTransactions.size + monthlyOutflowTransactions.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface.copy(alpha = 0.9f)
                    )
                }

                // List items
                if (monthlyOrders.isEmpty() && monthlyInflowTransactions.isEmpty() && monthlyOutflowTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhum lançamento ou pedido encontrado para este mês.", color = OnSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    // 1. Orders
                    items(monthlyOrders, key = { it.id }) { order ->
                        val dateFormatted = dateFormatter.format(Date(order.timestamp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = getGlassContainerColor()),
                            border = getGlassBorderStroke()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                                        Text("Pedido: ${order.clientName}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurface)
                                    }
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", order.totalValue),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Tertiary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${order.quantity} un. x ${order.pantyType} (${order.pantySize}) - R$ ${String.format(Locale("pt", "BR"), "%.2f", order.pantyValue)} / un.",
                                    fontSize = 12.sp,
                                    color = OnSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Semana de faturamento: ${order.week}",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = OnSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                                    Text("Última alteração: $dateFormatted", fontSize = 11.sp, color = OnSurfaceVariant.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }

                    // 2. Inflow Transactions
                    items(monthlyInflowTransactions, key = { it.id }) { trans ->
                        val dateFormatted = dateFormatter.format(Date(trans.timestamp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = getGlassContainerColor()),
                            border = getGlassBorderStroke()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                                        Text("Entrada: ${trans.description}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurface)
                                    }
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", trans.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Tertiary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Categoria: ${trans.category} | ${trans.extraText}", fontSize = 12.sp, color = OnSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = OnSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                                    Text("Últma alteração: $dateFormatted", fontSize = 11.sp, color = OnSurfaceVariant.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }

                    // 3. Outflow Transactions (Compras/Despesas)
                    items(monthlyOutflowTransactions, key = { it.id }) { trans ->
                        val dateFormatted = dateFormatter.format(Date(trans.timestamp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = getGlassContainerColor()),
                            border = getGlassBorderStroke()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = ErrorColor, modifier = Modifier.size(16.dp))
                                        Text("Compra / Saída: ${trans.description}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurface)
                                    }
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "- R$ %,.2f", trans.amount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = ErrorColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Categoria: ${trans.category} ${if (trans.extraText.isNotEmpty()) "| " + trans.extraText else ""}", fontSize = 12.sp, color = OnSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = OnSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                                    Text("Últma alteração: $dateFormatted", fontSize = 11.sp, color = OnSurfaceVariant.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    */
}
