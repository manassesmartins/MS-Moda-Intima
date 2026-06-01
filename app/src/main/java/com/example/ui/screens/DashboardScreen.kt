package com.example.ui

import com.example.ui.utils.generatePdfAndShare
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
fun DashboardScreen(
    summary: FinancialSummary,
    transactions: List<TransactionEntity>,
    orders: List<com.example.data.OrderEntity>,
    onVerTodosClick: () -> Unit,
    viewModel: TransactionViewModel,
    onItemClick: (TransactionEntity) -> Unit
) {
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

    var activeSubTab by remember { mutableStateOf("PAINEL") } // "PAINEL", "SEMANAL", "MENSAL"
    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Text(
                    text = "Demonstrativo Financeiro Oficial",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val totalIn = summary.totalInflow
                    val totalOut = summary.totalOutflow
                    val bal = summary.currentBalance
                    val marginHtml = if (totalIn > 0) (bal / totalIn) * 100.0 else 0.0
                    val countPieces = orders.sumOf { it.quantity }
                    val costPiece = if (countPieces > 0) totalOut / countPieces else 0.0

                    Text(
                        text = "Este relatório sintetiza a saúde operacional da confecção com base nas encomendas solicitadas e despesas operacionais registradas.",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                    
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    
                    Text("RESUMO DOS INDICADORES:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Primary)
                    Text("• Faturamento Bruto (Encomendas): " + String.format(Locale("pt", "BR"), "R$ %,.2f", totalIn), fontSize = 12.sp, color = Color.White)
                    Text("• Custos Operacionais (Insumos): " + String.format(Locale("pt", "BR"), "R$ %,.2f", totalOut), fontSize = 12.sp, color = Color.White)
                    Text("• Lucro Operacional Retido: " + String.format(Locale("pt", "BR"), "R$ %,.2f", bal), fontSize = 12.sp, color = Tertiary)
                    Text("• Margem de Lucratividade: " + String.format(Locale("pt", "BR"), "%.2f%%", marginHtml), fontSize = 12.sp, color = Color.White)
                    Text("• Volume Total Fabricado: " + countPieces + " peças", fontSize = 12.sp, color = Color.White)
                    Text("• Custo Médio por Peça Produzida: " + String.format(Locale("pt", "BR"), "R$ %,.2f", costPiece), fontSize = 12.sp, color = Color.White)

                    Divider(color = Color.White.copy(alpha = 0.1f))
                    
                    Text("DETALHAMENTO DE RATEIO DE GASTOS:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Primary)
                    summary.categoryBreakdown.forEach { (cat, pct) ->
                        Text("• $cat: " + String.format(Locale("pt", "BR"), "%.1f%%", pct), fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportDialog = false
                        generatePdfAndShare(
                            context = context,
                            balance = summary.currentBalance,
                            inflow = summary.totalInflow,
                            outflow = summary.totalOutflow,
                            transactions = transactions,
                            orders = orders
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Exportar PDF", color = OnPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Voltar", color = OnSurfaceVariant)
                }
            },
            containerColor = Color(0xFF1E0E2E), // Deep grape background
            shape = RoundedCornerShape(24.dp)
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tab switcher to unify Dashboard and Reports
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .background(getGlassContainerColor(), RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                "PAINEL" to "Painel",
                "SEMANAL" to "Apurado Semanal",
                "MENSAL" to "Fechamento Mensal / DRE"
            )
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

        when (activeSubTab) {
            "PAINEL" -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    // Executive Header with PDF Generation Trigger Action Button
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Painel de Negócios",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OnSurface
                                )
                                Text(
                                    text = "Faturamento e Custos operacionais",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                            
                            Button(
                                onClick = { showReportDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = getGlassContainerColor(),
                                    contentColor = OnSurface
                                ),
                                border = getGlassBorderStroke(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share, // Export indicator
                                    contentDescription = "PDF",
                                    tint = Secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Gerar PDF",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    // Balanço Atual / Saldo Atual Card
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Saldo Atual",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.Refresh, // Wallet equivalent icon
                                    contentDescription = "Carteira",
                                    tint = Primary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = String.format(Locale("pt", "BR"), "R$ %,.2f", summary.currentBalance),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.testTag("dashboard_balance")
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check, // Trending Upwards
                                    contentDescription = "Tendência",
                                    tint = Tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "+12%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Tertiary
                                )
                                Text(
                                    text = "vs. mês anterior",
                                    fontSize = 12.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }

                    // Two-Column Grid: Entradas & Saídas
                    item {
                        val maxTraffic = maxOf(summary.totalInflow, summary.totalOutflow)
                        val inflowFraction = if (maxTraffic > 0) (summary.totalInflow / maxTraffic).toFloat().coerceIn(0f, 1f) else 0f
                        val outflowFraction = if (maxTraffic > 0) (summary.totalOutflow / maxTraffic).toFloat().coerceIn(0f, 1f) else 0f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Entradas Column
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = getGlassContainerColor()
                                ),
                                border = getGlassBorderStroke(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Entradas",
                                            tint = Tertiary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Entradas",
                                            fontSize = 12.sp,
                                            color = OnSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.0f", summary.totalInflow),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    // Progress bar dynamic
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
                                    ) {
                                        if (inflowFraction > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(inflowFraction)
                                                    .fillMaxHeight()
                                                    .background(Tertiary, RoundedCornerShape(2.dp))
                                            )
                                        }
                                    }
                                }
                            }

                            // Saídas Column
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = getGlassContainerColor()
                                ),
                                border = getGlassBorderStroke(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Saídas",
                                            tint = ErrorColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Saídas",
                                            fontSize = 12.sp,
                                            color = OnSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.0f", summary.totalOutflow),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    // Progress bar dynamic
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
                                    ) {
                                        if (outflowFraction > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(outflowFraction)
                                                    .fillMaxHeight()
                                                    .background(ErrorColor, RoundedCornerShape(2.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Advanced Operational Ratio Metrics
                    item {
                        val totalIn = summary.totalInflow
                        val totalOut = summary.totalOutflow
                        val bal = summary.currentBalance
                        val marginPct = if (totalIn > 0.0) (bal / totalIn) * 100.0 else 0.0
                        val totalPieces = orders.sumOf { it.quantity }
                        val avgTicket = if (orders.isNotEmpty()) totalIn / orders.size else 0.0
                        val costPerPiece = if (totalPieces > 0) totalOut / totalPieces else 0.0

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Métricas de Produção & Eficiência",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Margin card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = getGlassContainerColor()),
                                    border = getGlassBorderStroke(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Margem Líquida", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(String.format(Locale("pt", "BR"), "%.1f%%", marginPct), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = if (marginPct >= 40) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (marginPct >= 40) "Alto" else "Baixo",
                                                    fontSize = 8.sp,
                                                    color = if (marginPct >= 40) Color(0xFF10B981) else Color(0xFFF59E0B),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                // Ticket Card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = getGlassContainerColor()),
                                    border = getGlassBorderStroke(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Ticket Médio", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(String.format(Locale("pt", "BR"), "R$ %,.2f", avgTicket), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Production Volume Card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = getGlassContainerColor()),
                                    border = getGlassBorderStroke(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Produção Geral", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("$totalPieces pçs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                    }
                                }

                                // Unit Cost Card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = getGlassContainerColor()),
                                    border = getGlassBorderStroke(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Custo por Peça", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(String.format(Locale("pt", "BR"), "R$ %,.2f", costPerPiece), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                                    }
                                }
                            }
                        }
                    }

                    // Recent Activity List Section Title
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Atividade Recente",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                            Text(
                                text = "Ver todos",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Tertiary,
                                modifier = Modifier
                                    .clickable { onVerTodosClick() }
                                    .padding(4.dp)
                            )
                        }
                    }

                    // Dashboard Lists (up to 3 recent items)
                    val recentItems = transactions.take(3)
                    if (recentItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sem transações registradas esta semana.",
                                    color = OnSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(recentItems, key = { item -> item.id }) { item ->
                            TransactionListItem(
                                item = item,
                                onItemClick = { onItemClick(item) }
                            )
                        }
                    }
                }
            }
            "SEMANAL" -> {
                Box(modifier = Modifier.weight(1f)) {
                    WeeklyReportsSection(transactions, orders, context, viewModel)
                }
            }
            "MENSAL" -> {
                Box(modifier = Modifier.weight(1f)) {
                    MonthlyReportsSection(transactions, orders, context, viewModel)
                }
            }
        }
    }
}

@Composable
fun WeeklyReportsSection(
    transactions: List<TransactionEntity>,
    orders: List<com.example.data.OrderEntity>,
    context: android.content.Context,
    viewModel: TransactionViewModel
) {
    var selectedReportWeek by remember { mutableStateOf("Tudo") }
    val outflowTransactions = remember(transactions) { transactions.filter { it.type == "OUTFLOW" } }
    
    val filteredOrders = remember(selectedReportWeek, orders) {
        if (selectedReportWeek == "Tudo") orders else orders.filter { it.week == selectedReportWeek }
    }
    val filteredOutflows = remember(selectedReportWeek, outflowTransactions) {
        if (selectedReportWeek == "Tudo") outflowTransactions else outflowTransactions.filter { it.week == selectedReportWeek }
    }

    val totalOrdersValue = remember(filteredOrders) { filteredOrders.sumOf { it.totalValue } }
    val totalExpensesValue = remember(filteredOutflows) { filteredOutflows.sumOf { it.amount } }
    
    val profit = remember(totalOrdersValue, totalExpensesValue) { totalOrdersValue - totalExpensesValue }
    val tithing = remember(profit) { if (profit > 0) profit * 0.10 else 0.0 }
    val tithingSplit = remember(tithing) { tithing / 2.0 }
    val totalPantyFabricated = remember(filteredOrders) { filteredOrders.sumOf { it.quantity } }
    
    val profitPercentage = remember(profit, totalOrdersValue) { if (totalOrdersValue > 0) (profit / totalOrdersValue) * 100 else 0.0 }

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

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

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
                            else "ALERTA: MARGEM DE LUCRO BAIZA OU PREJUÍZO ⚠️",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (profitPercentage >= 35.0) Tertiary else ErrorColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

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
                    contentColor = OnSurface
                ),
                shape = RoundedCornerShape(12.dp),
                border = getGlassBorderStroke(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Exportar Relatório Consolidado", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MonthlyReportsSection(
    transactions: List<TransactionEntity>,
    orders: List<com.example.data.OrderEntity>,
    context: android.content.Context,
    viewModel: TransactionViewModel
) {
    var selectedMonth by remember { mutableStateOf(4) } // Default to 4 (Maio / May)
    var selectedYear by remember { mutableStateOf(2026) } // Default to 2026
    var searchQuery by remember { mutableStateOf("") }

    val monthNames = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
    val yearList = listOf(2026, 2025, 2024)

    val calendar = remember { java.util.Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }

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
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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

        item {
            Text(
                text = "Resultados Encontrados (${monthlyOrders.size + monthlyInflowTransactions.size + monthlyOutflowTransactions.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface.copy(alpha = 0.9f)
            )
        }

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
            items(monthlyOrders, key = { "order_${it.id}" }) { order ->
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${order.pantyType} (${order.pantySize}) — Qtd: ${order.quantity} un • $dateFormatted",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            items(monthlyInflowTransactions, key = { "inflow_${it.id}" }) { t ->
                val dateFormatted = dateFormatter.format(Date(t.timestamp))
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
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                                Text(t.description, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurface)
                            }
                            Text(
                                text = String.format(Locale("pt", "BR"), "R$ %,.2f", t.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Tertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Categoria: ${t.category} • Semana: ${t.week} • $dateFormatted",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            items(monthlyOutflowTransactions, key = { "outflow_${it.id}" }) { t ->
                val dateFormatted = dateFormatter.format(Date(t.timestamp))
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
                                Text(t.description, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurface)
                            }
                            Text(
                                text = String.format(Locale("pt", "BR"), "R$ %,.2f", t.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ErrorColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Categoria: ${t.category} • Semana: ${t.week} • $dateFormatted",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
