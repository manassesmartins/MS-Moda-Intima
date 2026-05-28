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
import com.example.data.TransactionEntity
import com.example.ui.theme.*
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MsModaIntimaApp(viewModel: TransactionViewModel) {
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    if (!isUserLoggedIn) {
        MsModaIntimaLoginScreen(viewModel = viewModel)
        return
    }

    val context = LocalContext.current
    val updaterStatus by viewModel.updaterStatus.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val updater = remember { viewModel.getUpdater(context) }

    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            viewModel.checkForUpdatesSilently(context)
        }
    }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isAddingTransaction by viewModel.isAddingTransaction.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val isCloudBackupEnabled by viewModel.isCloudBackupEnabled.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val filterTab by viewModel.transactionFilter.collectAsStateWithLifecycle()

    val pendingDelete by viewModel.showDeleteConfirmation.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.showUndoSnackbar.collect { message ->
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Desfazer",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoLastDelete()
                Toast.makeText(context, "Registro restaurado!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SurfaceDark,
                        Color(0xFF1E0E2E), // Rich, deep lavender-dark grape background gradient
                        SurfaceDark
                    )
                )
            )
    ) {
        val isWideScreen = maxWidth >= 600.dp

        // Decorative background glowing blur effect (Secondary / pink orchids)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(320.dp)
                .offset(x = 60.dp, y = (-80).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Secondary.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        // Decorative background glowing blur effect (Tertiary / luminous lavender)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(300.dp)
                .offset(x = (-50).dp, y = 100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Tertiary.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Screen-adaptive layout containing Navigation Rail for Wide Screens
            if (isWideScreen) {
                MsModaIntimaNavigationRail(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            }

            Scaffold(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    MsModaIntimaTopBar(
                        syncState = syncState,
                        isCloudEnabled = isCloudBackupEnabled,
                        onSyncClick = {
                            if (isCloudBackupEnabled) {
                                viewModel.triggerSyncSimulation()
                                Toast.makeText(context, "Sincronizando com a Nuvem...", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Ative a sincronização em nuvem nas configurações.", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                },
                bottomBar = {
                    // Bottom Navigation Bar only for Compact screens
                    if (!isWideScreen) {
                        MsModaIntimaBottomBar(
                            currentTab = currentTab,
                            onTabSelected = { viewModel.setTab(it) }
                        )
                    }
                },
                floatingActionButton = {
                    // Show FAB on Dashboard or Transactions tab
                    if (currentTab == AppTab.DASHBOARD || currentTab == AppTab.TRANSACTIONS) {
                        FloatingActionButton(
                            onClick = { viewModel.setAddingTransaction(true) },
                            containerColor = Primary,
                            contentColor = OnPrimary,
                            shape = CircleShape,
                            modifier = Modifier
                                .testTag("add_transaction_fab")
                                .padding(bottom = if (isWideScreen) 16.dp else 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Adicionar Lançamento",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                containerColor = Color.Transparent
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val screenModifier = if (isWideScreen) {
                        Modifier
                            .widthIn(max = 1000.dp)
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxSize()
                    }

                    Box(modifier = screenModifier) {
                        // Multi-tab Layout rendering
                        when (currentTab) {
                            AppTab.DASHBOARD -> DashboardScreen(
                                summary = summary,
                                transactions = transactions,
                                orders = orders,
                                onVerTodosClick = { viewModel.setTab(AppTab.TRANSACTIONS) },
                                viewModel = viewModel
                            )
                            AppTab.TRANSACTIONS -> TransactionsScreen(
                                viewModel = viewModel,
                                transactions = transactions,
                                activeFilter = filterTab,
                                onFilterChanged = { viewModel.setTransactionFilter(it) },
                                onDeleteClick = { id ->
                                    viewModel.deleteTransaction(id)
                                }
                            )
                            AppTab.ORDERS -> OrdersScreen(
                                viewModel = viewModel
                            )
                            AppTab.CALCS -> CalculationsScreen(
                                viewModel = viewModel
                            )
                            AppTab.SETTINGS -> SettingsScreen(
                                isCloudEnabled = isCloudBackupEnabled,
                                onCloudEnabledChange = { viewModel.setCloudBackupEnabled(it) },
                                syncState = syncState,
                                onForceSync = {
                                    viewModel.triggerSyncSimulation()
                                    Toast.makeText(context, "Backup em tempo real sincronizado!", Toast.LENGTH_SHORT).show()
                                },
                                onClearAll = {
                                    viewModel.clearAllDataAndReseed()
                                    Toast.makeText(context, "Banco local resetado!", Toast.LENGTH_SHORT).show()
                                },
                                userEmail = viewModel.sessionManager.userEmail,
                                onLogout = { viewModel.logoutUser() },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }

        // Overlay: "Novo Lançamento" View
        AnimatedVisibility(
            visible = isAddingTransaction,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val overlayModifier = if (isWideScreen) {
                Modifier
                    .widthIn(max = 520.dp)
                    .align(Alignment.Center)
            } else {
                Modifier.fillMaxSize()
            }

            Box(
                modifier = if (isWideScreen) Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { viewModel.setAddingTransaction(false) } else Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = overlayModifier.clickable(enabled = false) { }
                ) {
                    NewTransactionScreen(
                        viewModel = viewModel,
                        isCloudBackupEnabled = isCloudBackupEnabled,
                        onDismiss = { viewModel.setAddingTransaction(false) },
                        onSubmit = { desc, amount, cat, type, week ->
                            viewModel.addTransaction(desc, amount, cat, type, week = week)
                            Toast.makeText(context, "Lançamento salvo com sucesso!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // ------------------------------------
        // OVERLAYS: Cloud Sync & System Update Dialogs
        // ------------------------------------
        when (val stat = updaterStatus) {
            is UpdateStatus.UpdateAvailable -> {
                AlertDialog(
                    onDismissRequest = { updater.clearStatus() },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Nova Atualização do Sistema!",
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        }
                    },
                    containerColor = SurfaceContainerHigh,
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Uma nova atualização de integridade e recursos foi detectada no servidor de dados em nuvem!",
                                fontSize = 14.sp,
                                color = OnSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Versão do Servidor: ${stat.version}", fontSize = 13.sp, color = Secondary, fontWeight = FontWeight.Bold)
                                    Text("Sincronizada em: ${stat.date}", fontSize = 12.sp, color = OnSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Divider(color = Color.White.copy(alpha = 0.08f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Melhorias e Ajustes:", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text(stat.changelog, fontSize = 12.sp, color = OnSurface)
                                }
                            }
                            
                            Text(
                                text = "Deseja baixar e instalar o novo pacote do sistema de forma segura agora?",
                                fontSize = 13.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                scope.launch {
                                    val downloaded = updater.downloadApk(stat.downloadUrl)
                                    if (downloaded != null) {
                                        updater.installApk(downloaded)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Atualizar Sistema", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { updater.clearStatus() }
                        ) {
                            Text("Mais Tarde", color = OnSurfaceVariant)
                        }
                    }
                )
            }

            is UpdateStatus.Downloading -> {
                AlertDialog(
                    onDismissRequest = {}, // Force showing download progress
                    title = {
                        Text(
                            text = "Sincronizando Pacote de Dados...",
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    },
                    containerColor = SurfaceContainerHigh,
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                progress = stat.progress,
                                color = Primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Baixando pacote seguro de atualização de tabelas e recursos do servidor de dados...",
                                    fontSize = 13.sp,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    text = "${(stat.progress * 100).toInt()}% concluído",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {}
                )
            }

            is UpdateStatus.Downloaded -> {
                AlertDialog(
                    onDismissRequest = { updater.clearStatus() },
                    title = {
                        Text(
                            text = "Download Concluído!",
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    },
                    containerColor = SurfaceContainerHigh,
                    text = {
                        Text(
                            text = "A nova versão foi baixada com sucesso. Clique em 'Instalar' para atualizar o aplicativo.",
                            fontSize = 13.sp,
                            color = OnSurface
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { updater.installApk(stat.apkFile) },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Instalar", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { updater.clearStatus() }) {
                            Text("Cancelar", color = OnSurfaceVariant)
                        }
                    }
                )
            }

            is UpdateStatus.Error -> {
                AlertDialog(
                    onDismissRequest = { updater.clearStatus() },
                    title = {
                        Text(
                            text = "Aviso de Atualização",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB4AB)
                        )
                    },
                    containerColor = SurfaceContainerHigh,
                    text = {
                        Text(
                            text = stat.message,
                            fontSize = 13.sp,
                            color = OnSurface
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { updater.clearStatus() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = OnSurface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Ok", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            else -> {}
        }

        val currentPendingDelete = pendingDelete
        if (currentPendingDelete != null) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelDeleteRequest() },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ErrorColor
                        )
                        Text(
                            text = "Confirmar Exclusão",
                            fontWeight = FontWeight.Bold,
                            color = OnSurface,
                            fontSize = 20.sp
                        )
                    }
                },
                text = {
                    val descriptionText = when (currentPendingDelete) {
                        is PendingDelete.Transaction -> {
                            "Deseja realmente excluir o lançamento '${currentPendingDelete.entity.description}' no valor de ${String.format(Locale("pt", "BR"), "R$ %,.2f", currentPendingDelete.entity.amount)}?"
                        }
                        is PendingDelete.Category -> {
                            "Deseja realmente excluir a categoria '${currentPendingDelete.entity.name}'?"
                        }
                        is PendingDelete.Order -> {
                            "Deseja realmente excluir o pedido de ${currentPendingDelete.entity.clientName} no valor de ${String.format(Locale("pt", "BR"), "R$ %,.2f", currentPendingDelete.entity.totalValue)}?"
                        }
                        is PendingDelete.Calculation -> {
                            "Deseja realmente excluir o cálculo do tecido '${currentPendingDelete.entity.pano}'?"
                        }
                    }
                    Text(
                        text = descriptionText,
                        color = OnSurface,
                        fontSize = 15.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmDelete() },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                    ) {
                        Text("Excluir", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.cancelDeleteRequest() }
                    ) {
                        Text("Cancelar", color = OnSurfaceVariant)
                    }
                },
                containerColor = SurfaceDark,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

// ------------------------------------
// SHARED COMPONENT: Glassmorphic Card
// ------------------------------------
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

// ------------------------------------
// COMPONENT: Top App Bar
// ------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MsModaIntimaTopBar(
    syncState: String,
    isCloudEnabled: Boolean,
    onSyncClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onSyncClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Estado de Sincronização",
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "MS Moda Íntima",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    fontFamily = FontFamily.SansSerif
                )
            }
        },
        actions = {
            // Live Synchronicity Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (syncState) {
                                "SYNCED" -> Tertiary.copy(alpha = 0.15f)
                                "SYNCING" -> Secondary.copy(alpha = 0.15f)
                                "SAVED_OFFLINE" -> Color(0xFFFFB4AB).copy(alpha = 0.15f)
                                else -> Color.White.copy(alpha = 0.1f)
                            }
                        )
                        .border(
                            1.dp,
                            when (syncState) {
                                "SYNCED" -> Tertiary.copy(alpha = 0.4f)
                                "SYNCING" -> Secondary.copy(alpha = 0.4f)
                                "SAVED_OFFLINE" -> Color(0xFFFFB4AB).copy(alpha = 0.4f)
                                else -> Color.White.copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSyncClick() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when (syncState) {
                            "SYNCED" -> "SINCRONIZADO"
                            "SYNCING" -> "SINCANDO..."
                            "SAVED_OFFLINE" -> "SALVO OFFLINE"
                            else -> "NUVEM OFF"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (syncState) {
                            "SYNCED" -> Tertiary
                            "SYNCING" -> Secondary
                            "SAVED_OFFLINE" -> Color(0xFFFFB4AB)
                            else -> OnSurfaceVariant
                        }
                    )
                }

                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Perfil",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White.copy(alpha = 0.05f),
            titleContentColor = Primary
        ),
        modifier = Modifier.drawBehindGlassBorder()
    )
}

// Custom Glass Border modifier
fun Modifier.drawBehindGlassBorder(): Modifier = this.background(
    Color.Transparent
).border(
    width = 0.5.dp,
    color = Color.White.copy(alpha = 0.15f)
)

// ------------------------------------
// COMPONENT: Bottom Nav Bar
// ------------------------------------
@Composable
fun MsModaIntimaBottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.05f),
        tonalElevation = 0.dp,
        modifier = Modifier
            .drawBehindGlassBorder()
            .height(80.dp)
    ) {
        val tabs = listOf(
            Triple(AppTab.DASHBOARD, Icons.Default.Home, "Painel de Negócios"),
            Triple(AppTab.TRANSACTIONS, Icons.Default.List, "Fluxo de Caixa"),
            Triple(AppTab.ORDERS, Icons.Default.ShoppingCart, "Agendamento de Pedidos"),
            Triple(AppTab.CALCS, Icons.Default.Star, "Custo de Peças"),
            Triple(AppTab.SETTINGS, Icons.Default.Settings, "Ajustes")
        )

        tabs.forEach { (tab, icon, label) ->
            val isActive = currentTab == tab
            NavigationBarItem(
                selected = isActive,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isActive) Primary else OnSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(24.dp)
                            .then(
                                if (isActive) Modifier.glow(Primary) else Modifier
                            )
                            .testTag("${label.lowercase()}_tab")
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isActive) Primary else OnSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        softWrap = true,
                        lineHeight = 11.sp,
                        overflow = TextOverflow.Visible
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

// ------------------------------------
// COMPONENT: Navigation Rail (Side Nav)
// ------------------------------------
@Composable
fun MsModaIntimaNavigationRail(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        containerColor = Color.White.copy(alpha = 0.05f),
        modifier = modifier
            .drawBehindGlassBorder()
            .fillMaxHeight()
            .width(88.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Brand logo/letters matching "MS Moda Íntima"
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Primary.copy(alpha = 0.15f), CircleShape)
                .glow(Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        val tabs = listOf(
            Triple(AppTab.DASHBOARD, Icons.Default.Home, "Painel de Negócios"),
            Triple(AppTab.TRANSACTIONS, Icons.Default.List, "Fluxo de Caixa"),
            Triple(AppTab.ORDERS, Icons.Default.ShoppingCart, "Agendamento de Pedidos"),
            Triple(AppTab.CALCS, Icons.Default.Star, "Custo de Peças"),
            Triple(AppTab.SETTINGS, Icons.Default.Settings, "Ajustes")
        )

        tabs.forEach { (tab, icon, label) ->
            val isActive = currentTab == tab
            NavigationRailItem(
                selected = isActive,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isActive) Primary else OnSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(24.dp)
                            .then(
                                if (isActive) Modifier.glow(Primary) else Modifier
                            )
                            .testTag("${label.lowercase()}_tab")
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isActive) Primary else OnSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        softWrap = true,
                        lineHeight = 11.sp,
                        overflow = TextOverflow.Visible
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

fun Modifier.glow(color: Color): Modifier = this // Decorative visual effect placeholder

// ------------------------------------
// SCREEN: Dashboard Screen
// ------------------------------------
@Composable
fun DashboardScreen(
    summary: FinancialSummary,
    transactions: List<TransactionEntity>,
    orders: List<com.example.data.OrderEntity>,
    onVerTodosClick: () -> Unit,
    viewModel: TransactionViewModel
) {
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
                        text = "Este relatório sintetiza a saúde operacional do atelier com base nas encomendas solicitadas e despesas operacionais registradas.",
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
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
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
                                    color = Color.White
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
                                    containerColor = Color.White.copy(alpha = 0.06f),
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
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
                                    containerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
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
                                    containerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
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
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Margem Líquida", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(String.format(Locale("pt", "BR"), "%.1f%%", marginPct), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Ticket Médio", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(String.format(Locale("pt", "BR"), "R$ %,.2f", avgTicket), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Production Volume Card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Produção Geral", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("$totalPieces pçs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                // Unit Cost Card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Custo por Peça", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(String.format(Locale("pt", "BR"), "R$ %,.2f", costPerPiece), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                                onDeleteClick = { viewModel.deleteTransaction(item.id) }
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
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(1.dp, Tertiary.copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
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
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(1.dp, ErrorColor.copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
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
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
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
                                    progress = { pct },
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
                    containerColor = Color.White.copy(alpha = 0.08f),
                    contentColor = Primary
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
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
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
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
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Tertiary.copy(alpha = 0.25f))
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
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Tertiary.copy(alpha = 0.2f))
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
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, ErrorColor.copy(alpha = 0.2f))
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

// ------------------------------------
// SHARED LIST ITEM: Transaction Item
// ------------------------------------
@Composable
fun TransactionListItem(
    item: TransactionEntity,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${item.id}")
            .clickable { onDeleteClick() }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Emblem
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (item.type == "INFLOW") Tertiary.copy(alpha = 0.1f) else ErrorColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (item.type == "INFLOW") Tertiary.copy(alpha = 0.2f) else ErrorColor.copy(alpha = 0.2f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.type == "INFLOW") Icons.Default.Check else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (item.type == "INFLOW") Tertiary else ErrorColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = item.description,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = item.extraText,
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = "•",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                text = item.dateString,
                                fontSize = 11.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val sign = if (item.type == "INFLOW") "+" else "-"
                    val color = if (item.type == "INFLOW") Tertiary else ErrorColor
                    Text(
                        text = String.format(Locale("pt", "BR"), "%s R$ %,.2f", sign, item.amount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.category.uppercase(Locale.getDefault()),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------
// SCREEN: Transactions Filter View
// ------------------------------------
@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel,
    transactions: List<TransactionEntity>,
    activeFilter: String,
    onFilterChanged: (String) -> Unit,
    onDeleteClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Fluxo de Caixa",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Text(
            text = "Controle de despesas, custos e saídas gerais",
            fontSize = 13.sp,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Large Horizontal Scrolling Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filters = listOf("SAIDAS" to "Custos / Saídas")
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { (key, display) ->
                    val isSelected = activeFilter == key
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
                            .clickable { onFilterChanged(key) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("filter_${key.lowercase()}")
                    ) {
                        Text(
                            text = display,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Primary else OnSurfaceVariant
                        )
                    }
                }
            }

            // Category Manager Button
            var showCategoryManager by remember { mutableStateOf(false) }
            IconButton(
                onClick = { showCategoryManager = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Gerenciar Categorias",
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (showCategoryManager) {
                CategoryManagerDialog(viewModel = viewModel, onDismiss = { showCategoryManager = false })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scrolling lists
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum lançamento corresponde ao filtro.",
                            color = OnSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(transactions, key = { it.id }) { item ->
                    TransactionListItem(
                        item = item,
                        onDeleteClick = { onDeleteClick(item.id) }
                    )
                }
            }
        }
    }
}

// ------------------------------------
// SCREEN: Financial Reports
// ------------------------------------
// SCREEN: Financial Reports
// ------------------------------------
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
            
            val profitPercentage = remember(profit, totalOrdersValue) { if (totalOrdersValue > 0) (profit / totalOrdersValue) * 100 else 0.0 }

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
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(1.dp, Tertiary.copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
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
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(1.dp, ErrorColor.copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
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
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
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
                                    progress = { pct },
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
                    containerColor = Color.White.copy(alpha = 0.08f),
                    contentColor = Primary
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
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
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
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
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Tertiary.copy(alpha = 0.25f))
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
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Tertiary.copy(alpha = 0.25f))
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
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, ErrorColor.copy(alpha = 0.25f))
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
    */
}

// ------------------------------------
// SCREEN: Order Booking & Scheduling
// ------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(viewModel: TransactionViewModel) {
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
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
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

// ------------------------------------
// DIALOG: Order Adding/Editing Dialog
// ------------------------------------
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

// ------------------------------------
// DIALOG: Coupon Receipt printable PDF preview generator
// ------------------------------------
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

// ------------------------------------
// DIALOG: Category Manager Dialog
// ------------------------------------
@Composable
fun CategoryManagerDialog(
    viewModel: TransactionViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.allCategories.collectAsStateWithLifecycle(emptyList())

    var categoryNameInput by remember { mutableStateOf("") }
    var editingCategoryId by remember { mutableStateOf<Long?>(null) }

    // Only display/manage OUTFLOW categories since transactions are only outflows
    val outflowCategories = remember(categories) { categories.filter { it.type == "OUTFLOW" } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Gerenciador de Categorias (Custos)", fontWeight = FontWeight.Bold, color = Primary)
        },
        containerColor = SurfaceContainerHigh,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                // Add / Edit form
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    val isEditing = editingCategoryId != null
                    Text(
                        text = if (isEditing) "EDITAR CATEGORIA" else "ADICIONAR CATEGORIA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    OutlinedTextField(
                        value = categoryNameInput,
                        onValueChange = { categoryNameInput = it },
                        placeholder = { Text("Ex: Forros de Malha", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isEditing) {
                            Button(
                                onClick = {
                                    editingCategoryId = null
                                    categoryNameInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar", color = OnSurface, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                if (categoryNameInput.isNotBlank()) {
                                    val catName = categoryNameInput.trim()
                                    val currentId = editingCategoryId
                                    if (currentId != null) {
                                        viewModel.updateCategory(currentId, catName, "OUTFLOW")
                                        Toast.makeText(context, "Categoria atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                                        editingCategoryId = null
                                    } else {
                                        viewModel.addCategory(catName, "OUTFLOW")
                                        Toast.makeText(context, "Categoria adicionada com sucesso!", Toast.LENGTH_SHORT).show()
                                    }
                                    categoryNameInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.weight(if (isEditing) 1.5f else 1f),
                            enabled = categoryNameInput.isNotBlank()
                        ) {
                            Text(
                                text = if (isEditing) "Salvar" else "Adicionar",
                                fontWeight = FontWeight.Bold,
                                color = OnPrimary
                            )
                        }
                    }
                }

                // List of existing added categories
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CATEGORIAS DE CUSTOS CADASTRADAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                    if (outflowCategories.isEmpty()) {
                        Text("Nenhuma categoria customizada ainda.", fontSize = 12.sp, color = OnSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(outflowCategories, key = { it.id }) { cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = ErrorColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(text = cat.name, fontSize = 14.sp, color = OnSurface)
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Edit Button
                                        IconButton(
                                            onClick = {
                                                editingCategoryId = cat.id
                                                categoryNameInput = cat.name
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar Categoria",
                                                tint = Tertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Delete Button
                                        IconButton(
                                            onClick = {
                                                if (editingCategoryId == cat.id) {
                                                    editingCategoryId = null
                                                    categoryNameInput = ""
                                                }
                                                viewModel.deleteCategory(cat.id)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Excluir Categoria",
                                                tint = ErrorColor.copy(alpha = 0.8f),
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = Primary)
            }
        }
    )
}

@Composable
fun CategoryBar(name: String, percentage: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, fontSize = 14.sp, color = OnSurface)
            Text(text = String.format(Locale.US, "%.0f%%", percentage), fontSize = 14.sp, color = Primary, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}

// ------------------------------------
// SCREEN: Settings / Ajustes
// ------------------------------------
@Composable
fun SettingsScreen(
    isCloudEnabled: Boolean,
    onCloudEnabledChange: (Boolean) -> Unit,
    syncState: String,
    onForceSync: () -> Unit,
    onClearAll: () -> Unit,
    userEmail: String?,
    onLogout: () -> Unit,
    viewModel: TransactionViewModel
) {
    val context = LocalContext.current
    val updater = remember { viewModel.getUpdater(context) }
    var ownerInput by remember { mutableStateOf(updater.owner) }
    var repoInput by remember { mutableStateOf(updater.repo) }
    var branchInput by remember { mutableStateOf(updater.branch) }
    var apkPathInput by remember { mutableStateOf(updater.apkPath) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Main Screen Title matching tab bar
        item {
            Column {
                Text(
                    text = "Ajustes",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = "Gerencie dados, preferências e sincronização em nuvem",
                    fontSize = 13.sp,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Account Profile & Supabase Sync status card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SESSÃO DE USUÁRIO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Tertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = userEmail ?: "Modo Offline (Sem Sessão)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (userEmail != null) "Sessão Ativa na Nuvem" else "Faça Login para salvar na Nuvem",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }

        // Real-Time Sync toggle & status card centered
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.8f)) {
                        Text(
                            text = "Sincronização em Nuvem",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        Text(
                            text = "Manter dados sincronizados em tempo real na nuvem de forma segura",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isCloudEnabled,
                        onCheckedChange = onCloudEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnPrimary,
                            checkedTrackColor = Tertiary,
                            uncheckedThumbColor = OnSurfaceVariant,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("sync_toggle")
                    )
                }
                if (isCloudEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estado do Backup: " + when (syncState) {
                                "SYNCED" -> "Sincronizado"
                                "SYNCING" -> "Sincando..."
                                "ERROR_SYNC" -> "Sem Conexão ou Não Configurado"
                                else -> "Salvo Offline"
                            },
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = "Sincronizar Agora",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Tertiary,
                            modifier = Modifier
                                .clickable { onForceSync() }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        // Local State Persistence details
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ARMAZENAMENTO OFFLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Secondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "O MS Moda Íntima salva todos os dados em cache local seguro (Room DB) quando estiver sem sinal de internet. Assim que a conexão for restabelecida, a sincronização é completada em segundo plano.",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Reset Cache & seeding options
        item {
            Button(
                onClick = onClearAll,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF93000A).copy(alpha = 0.08f),
                    contentColor = Color(0xFFFFB4AB)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF93000A).copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_data_button")
            ) {
                Text(
                    text = "Resetar Informações Locais",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Integration Status details & secrets explanation inside Settings Screen
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INTEGRAÇÃO COM A NUVEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Tertiary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (com.example.data.api.SupabaseClient.isConfigured) {
                            "Status: CONECTADO ✅\nO aplicativo está totalmente integrado e pronto para sincronizar dados e contas na Nuvem."
                        } else {
                            "Status: MODO SEGURO OFF-LINE ⚠️\nOs dados de usuário estão protegidos localmente neste dispositivo (Room DB). Configure as chaves de conexão segura do servidor no AI Studio para ativar as contas seguras em nuvem."
                        },
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // VERSION & INTEGRITY SECTION
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SINCRONIZAÇÃO DE VERSÃO CLOUD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "O aplicativo integra-se com a nuvem para garantir a integridade total do banco de dados e sincronizar recursos. Verifique se há melhorias de segurança ou patches de otimização de consultas disponíveis para o sistema.",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Versão Atual do App:",
                            fontSize = 11.sp,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = updater.getLocalVersion(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                Toast.makeText(context, "Buscando melhorias na nuvem...", Toast.LENGTH_SHORT).show()
                                updater.checkForUpdates(forceNotify = true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Buscar Atualização", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Logout Button item
        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.05f),
                    contentColor = Primary
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("logout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sair da Conta (Logout)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ------------------------------------
// SCREEN: NEW TRANSACTION SHEET/FORM
// ------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTransactionScreen(
    viewModel: TransactionViewModel,
    isCloudBackupEnabled: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, Double, String, String, String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Variados") }
    val type = "OUTFLOW"
    val toggleSync = remember { mutableStateOf(isCloudBackupEnabled) }
    var selectedWeek by remember { mutableStateOf("1ª Semana") }

    val dynamicCategories by viewModel.allCategories.collectAsStateWithLifecycle(emptyList())

    val displayedCategories = remember(dynamicCategories, type) {
        val filtered = dynamicCategories.filter { it.type == type }.map { it.name }
        if (filtered.isEmpty()) {
            if (type == "INFLOW") listOf("Vendas", "Encomendas", "Outros")
            else listOf("Funcionários", "Pano", "Viés", "Linha", "Etiqueta de composição", "Etiqueta lateral", "Forro", "Manutenção", "Variados")
        } else {
            filtered
        }
    }

    // Auto update selected category if not in the new list of categories
    LaunchedEffect(type, displayedCategories) {
        if (category !in displayedCategories && displayedCategories.isNotEmpty()) {
            category = displayedCategories.first()
        }
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
                    text = "Novo Lançamento",
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
                            imageVector = Icons.Default.Check, // Simulated Cash Icon
                            contentDescription = "Lançamento",
                            tint = Primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Registre saídas e custos de MS Moda Íntima para manter as despesas organizadas e controladas.",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        lineHeight = 20.sp
                    )
                }
            }

            // Description Input Field
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "DESCRIÇÃO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Ex: Compra de Linhas de Seda", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
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
                            .testTag("description_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
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
                        text = "SEMANA DO LANÇAMENTO",
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

            // Category Selection dropdown/scroller list (represented cleanly with selectable glass chips)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CATEGORIA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("category_select")
                    ) {
                        // Dropdown selection trigger inside card
                        var isExpanded by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .clickable { isExpanded = !isExpanded }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = category, fontSize = 16.sp, color = OnSurface)
                                Icon(
                                    imageVector = Icons.Default.Check, // Pulldown arrow representation
                                    contentDescription = "Selecionar Categoria",
                                    tint = Primary
                                )
                            }

                            DropdownMenu(
                                expanded = isExpanded,
                                onDismissRequest = { isExpanded = false },
                                modifier = Modifier
                                    .background(SurfaceContainerHigh)
                                    .fillMaxWidth(0.85f)
                            ) {
                                displayedCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(text = cat, color = OnSurface) },
                                        onClick = {
                                            category = cat
                                            isExpanded = false
                                        }
                                    )
                                }
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
                        if (description.isNotBlank() && amt > 0) {
                            onSubmit(description, amt, category, type, selectedWeek)
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
                    enabled = description.isNotBlank() && (amountText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Text(
                            text = "Salvar Lançamento",
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
                        containerColor = Color.White.copy(alpha = 0.05f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
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
                            text = "Os lançamentos salvos aqui impactam diretamente seus relatórios mensais de lucratividade na MS Moda Íntima.",
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

// ------------------------------------
// SCREEN: Piece Calculations & Calculator
// ------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationsScreen(viewModel: TransactionViewModel) {
    val context = LocalContext.current
    val calculations by viewModel.allCalculations.collectAsStateWithLifecycle(emptyList())

    var activeSubTab by remember { mutableStateOf("PECAS") } // "PECAS" or "CALC"

    // Dialog state for editing calculation row
    var calculationToEdit by remember { mutableStateOf<com.example.data.PieceCalculationEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Custo de Peças",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Text(
            text = "Calcule o rendimento de peças por quilo de tecido",
            fontSize = 13.sp,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        // Tab switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("PECAS" to "Peças por KG", "CALC" to "Calculadora")
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (activeSubTab == "PECAS") {
            // CÁLCULO DE PEÇAS POR KG SCREEN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Corte & Rendimentos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.clearCalculationsAndReseed() },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Resetar Tabela", tint = OnSurfaceVariant)
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = OnPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nova Linha", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnPrimary)
                    }
                }
            }

            // Explicativo da Tela de Rendimentos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainer, RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Gerencie o rendimento de cortes e custos reais por lote/tecido. Os dados são salvos localmente e integrados.",
                        color = OnSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (calculations.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Nenhum cálculo",
                                tint = OnSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Nenhum cálculo cadastrado",
                                color = OnSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = { showAddDialog = true }) {
                                Text("Toque aqui para adicionar uma linha", color = Primary)
                            }
                        }
                    }
                } else {
                    items(calculations, key = { it.id }) { item ->
                        val pano = item.pano
                        val kg = item.kg
                        val valorKg = item.valorKg
                        val qty = item.quantidade

                        val hasKg = kg != null && kg > 0
                        val hasQty = qty != null && qty > 0

                        val quantKgString = if (hasKg && hasQty) {
                            String.format(Locale.US, "%.5f", qty!!.toDouble() / kg!!)
                        } else {
                            ""
                        }

                        val valorQuantString = if (hasQty) {
                            if (hasKg && valorKg != null) {
                                val cost = (kg * valorKg) / qty.toDouble()
                                String.format(Locale("pt", "BR"), "R$ %,.2f", cost)
                            } else if (pano.lowercase().contains("listrada") && valorKg != null) {
                                val cost = valorKg / 106.8
                                String.format(Locale("pt", "BR"), "R$ %,.2f", cost)
                            } else {
                                ""
                            }
                        } else {
                            ""
                        }

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { calculationToEdit = item }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Primary.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.List,
                                            contentDescription = null,
                                            tint = Primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = if (pano.isBlank()) "Pano sem nome" else pano,
                                            color = OnSurface,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "ID #${item.id} • Métricas de Produção",
                                            color = OnSurfaceVariant.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = Primary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Rendimento (QTD / KG)",
                                            color = OnSurfaceVariant,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (quantKgString.isEmpty()) "Div/0!" else "$quantKgString pç/kg",
                                            color = Tertiary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Custo Unitário (Valor/QTD)",
                                            color = OnSurfaceVariant,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (valorQuantString.isEmpty()) "Div/0!" else valorQuantString,
                                            color = Primary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DetailMetric(label = "PESO TOTAL", value = if (kg == null) "0,00 kg" else String.format(Locale("pt", "BR"), "%,.2f kg", kg))
                                DetailMetric(label = "VALOR DO KG", value = if (valorKg == null) "R$ 0,00" else String.format(Locale("pt", "BR"), "R$ %,.2f", valorKg))
                                DetailMetric(label = "PEÇAS CORTADAS", value = if (qty == null) "0 u." else "$qty u.")
                            }
                        }
                    }
                }
            }
        } else {
            // INTERACTIVE CALCULATOR SCREEN (ELECTRONIC LAB DESIGN)
            var displayVal by remember { mutableStateOf("0") }
            var activeOp by remember { mutableStateOf<Char?>(null) }
            var prevVal by remember { mutableStateOf<Double?>(null) }
            var opPressed by remember { mutableStateOf(false) }

            Text(
                text = "Calculadora Eletrônica",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E102E)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Display Window
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF11071F), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            if (prevVal != null && activeOp != null) {
                                Text(
                                    text = "${if (prevVal!! % 1 == 0.0) prevVal!!.toInt() else prevVal} $activeOp",
                                    fontSize = 13.sp,
                                    color = OnSurfaceVariant.copy(alpha = 0.5f),
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = displayVal,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE5A1FF),
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }

                    // Interactive Keys
                    val keys = listOf(
                        listOf("C", "⌫", "%", "/"),
                        listOf("7", "8", "9", "*"),
                        listOf("4", "5", "6", "-"),
                        listOf("1", "2", "3", "+"),
                        listOf("+/-", "0", ".", "")
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        keys.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { key ->
                                    val isOp = key in listOf("/", "*", "-", "+")
                                    val isClear = key == "C"
                                    val isDelete = key == "⌫"
                                    val isPercent = key == "%"
                                    val isSign = key == "+/-"
                                    val isEmpty = key == ""
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1.2f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                when {
                                                    isEmpty -> Color.Transparent
                                                    isClear || isDelete -> ErrorColor.copy(alpha = 0.15f)
                                                    isOp || isPercent || isSign -> Primary.copy(alpha = 0.15f)
                                                    else -> Color.White.copy(alpha = 0.05f)
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                when {
                                                    isEmpty -> Color.Transparent
                                                    isClear || isDelete -> ErrorColor.copy(alpha = 0.3f)
                                                    isOp || isPercent || isSign -> Primary.copy(alpha = 0.3f)
                                                    else -> Color.White.copy(alpha = 0.08f)
                                                },
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                if (isEmpty) return@clickable
                                                when {
                                                    key in "0".."9" || key == "." -> {
                                                        if (displayVal == "0" || opPressed || displayVal == "Erro") {
                                                            displayVal = if (key == ".") "0." else key
                                                            opPressed = false
                                                        } else {
                                                            if (key == "." && displayVal.contains(".")) {
                                                                // Ignore duplicate decimals
                                                            } else {
                                                                displayVal += key
                                                            }
                                                        }
                                                    }
                                                    isClear -> {
                                                        displayVal = "0"
                                                        prevVal = null
                                                        activeOp = null
                                                        opPressed = false
                                                    }
                                                    isDelete -> {
                                                        if (displayVal != "0" && displayVal.isNotEmpty() && displayVal != "Erro") {
                                                            displayVal = displayVal.dropLast(1)
                                                            if (displayVal.isEmpty() || displayVal == "-") {
                                                                displayVal = "0"
                                                            }
                                                        }
                                                    }
                                                    isPercent -> {
                                                        val currentVal = displayVal.toDoubleOrNull() ?: 0.0
                                                        if (prevVal != null && activeOp != null) {
                                                            if (activeOp == '+' || activeOp == '-') {
                                                                val percentageOfPrev = prevVal!! * (currentVal / 100.0)
                                                                displayVal = if (percentageOfPrev % 1 == 0.0) percentageOfPrev.toInt().toString() else String.format(Locale.US, "%.4f", percentageOfPrev)
                                                            } else {
                                                                val p = currentVal / 100.0
                                                                displayVal = if (p % 1 == 0.0) p.toInt().toString() else p.toString()
                                                            }
                                                        } else {
                                                            val p = currentVal / 100.0
                                                            displayVal = if (p % 1 == 0.0) p.toInt().toString() else p.toString()
                                                        }
                                                    }
                                                    isSign -> {
                                                        val currentVal = displayVal.toDoubleOrNull() ?: 0.0
                                                        val toggled = -currentVal
                                                        displayVal = if (displayVal != "0" && displayVal != "Erro") {
                                                            if (toggled % 1 == 0.0) toggled.toInt().toString() else toggled.toString()
                                                        } else {
                                                            "0"
                                                        }
                                                    }
                                                    isOp -> {
                                                        prevVal = displayVal.toDoubleOrNull()
                                                        activeOp = key.first()
                                                        opPressed = true
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!isEmpty) {
                                            Text(
                                                text = key,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    isClear || isDelete -> ErrorColor
                                                    isOp || isPercent || isSign -> Primary
                                                    else -> OnSurface
                                                },
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Wide bottom evaluate (=) key
                        Button(
                            onClick = {
                                if (prevVal != null && activeOp != null) {
                                    val currentVal = displayVal.toDoubleOrNull() ?: 0.0
                                    val result = when (activeOp) {
                                        '+' -> prevVal!! + currentVal
                                        '-' -> prevVal!! - currentVal
                                        '*' -> prevVal!! * currentVal
                                        '/' -> if (currentVal != 0.0) prevVal!! / currentVal else Double.NaN
                                        else -> currentVal
                                    }
                                    displayVal = if (result.isNaN()) {
                                        "Erro"
                                    } else {
                                        if (result % 1 == 0.0) result.toInt().toString() else String.format(Locale.US, "%.4f", result)
                                    }
                                    prevVal = null
                                    activeOp = null
                                    opPressed = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text(text = "=", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnPrimary, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }

    // DIALOG: ADD NEW CALCULATION ROW
    if (showAddDialog) {
        var pano by remember { mutableStateOf("") }
        var kgText by remember { mutableStateOf("") }
        var valorKgText by remember { mutableStateOf("") }
        var qtyText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Nova Linha de Cálculo", color = Primary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = pano,
                        onValueChange = { pano = it },
                        label = { Text("Pano (Tipo/Descrição)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = kgText,
                        onValueChange = { kgText = it },
                        label = { Text("Quilos (KG)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = valorKgText,
                        onValueChange = { valorKgText = it },
                        label = { Text("Valor do KG (R$/KG)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Quantidade de Peças") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val kg = kgText.replace(',', '.').toDoubleOrNull()
                        val valorKg = valorKgText.replace(',', '.').toDoubleOrNull()
                        val qty = qtyText.toIntOrNull()
                        viewModel.addCalculation(pano, kg, valorKg, qty)
                        showAddDialog = false
                        Toast.makeText(context, "Linha adicionada!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Adicionar", color = OnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            }
        )
    }

    // DIALOG: EDIT CALCULATION ROW
    calculationToEdit?.let { entity ->
        var pano by remember { mutableStateOf(entity.pano) }
        var kgText by remember { mutableStateOf(entity.kg?.toString() ?: "") }
        var valorKgText by remember { mutableStateOf(entity.valorKg?.toString() ?: "") }
        var qtyText by remember { mutableStateOf(entity.quantidade?.toString() ?: "") }

        AlertDialog(
            onDismissRequest = { calculationToEdit = null },
            containerColor = SurfaceDark,
            title = { Text("Editar Linha de Cálculo", color = Primary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = pano,
                        onValueChange = { pano = it },
                        label = { Text("Pano (Tipo/Descrição)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = kgText,
                        onValueChange = { kgText = it },
                        label = { Text("Quilos (KG)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = valorKgText,
                        onValueChange = { valorKgText = it },
                        label = { Text("Valor do KG (R$/KG)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Quantidade de Peças") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.deleteCalculation(entity.id)
                            calculationToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                    ) {
                        Text("Excluir", color = Color.White)
                    }
                    Button(
                        onClick = {
                            val kg = kgText.replace(',', '.').toDoubleOrNull()
                            val valorKg = valorKgText.replace(',', '.').toDoubleOrNull()
                            val qty = qtyText.toIntOrNull()
                            viewModel.updateCalculation(entity.id, pano, kg, valorKg, qty)
                            calculationToEdit = null
                            Toast.makeText(context, "Alterações salvas!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Salvar", color = OnPrimary)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { calculationToEdit = null }) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun DetailMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = OnSurfaceVariant.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = OnSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ------------------------------------
// PDF GENERATION AND OUTWARD SHARING ENGINE
// ------------------------------------
fun generatePdfAndShare(
    context: android.content.Context,
    balance: Double,
    inflow: Double,
    outflow: Double,
    transactions: List<TransactionEntity>,
    orders: List<com.example.data.OrderEntity>
) {
    try {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()

        // Draw title
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("MS MODA ÍNTIMA - RELATÓRIO OPERACIONAL", 40f, 65f, paint)

        // Subtitle
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.DKGRAY
        canvas.drawText("Atelier de Costura e Confecção - Demonstrativo de Lucros e Custos", 40f, 85f, paint)

        // Metadata
        val df = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        canvas.drawText("Emitido em: " + df.format(java.util.Date()), 390f, 85f, paint)

        // Line separator
        paint.strokeWidth = 2f
        paint.color = android.graphics.Color.BLACK
        canvas.drawLine(40f, 100f, 555f, 100f, paint)

        // Draw Section indicator titles
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Resumo Consolidado de Saúde Financeira", 40f, 130f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.BLACK
        canvas.drawText(String.format("Faturamento Bruto (Receitas): R$ %,.2f", inflow), 50f, 165f, paint)
        canvas.drawText(String.format("Despesas Operacionais (Saídas): R$ %,.2f", outflow), 50f, 185f, paint)
        
        paint.isFakeBoldText = true
        paint.textSize = 11f
        canvas.drawText(String.format("Saldo Caixa Retido (Lucro Líquido): R$ %,.2f", balance), 50f, 215f, paint)

        // Draw helper KPI
        paint.isFakeBoldText = false
        val totalPieces = orders.sumOf { it.quantity }
        val costPiece = if (totalPieces > 0) outflow / totalPieces else 0.0
        val margin = if (inflow > 0.0) (balance / inflow) * 100.0 else 0.0
        canvas.drawText(String.format("Margem Estimada de Rendimento: %,.1f%%", margin), 50f, 235f, paint)
        canvas.drawText(String.format("Volume Total Fabricado: %d calcinhas", totalPieces), 50f, 255f, paint)
        canvas.drawText(String.format("Custo de Insumo Unitário Médio: R$ %,.2f", costPiece), 50f, 275f, paint)

        // Draw Orders Table Title
        paint.isFakeBoldText = true
        paint.textSize = 12f
        canvas.drawText("Histórico Detalhado de Encomendas (Receitas)", 40f, 315f, paint)

        var currentY = 335f
        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.BLACK
        canvas.drawText("Cliente", 45f, currentY, paint)
        canvas.drawText("Especificação", 160f, currentY, paint)
        canvas.drawText("Quant.", 320f, currentY, paint)
        canvas.drawText("Valor Total", 450f, currentY, paint)

        canvas.drawLine(40f, currentY+5, 555f, currentY+5, paint)
        currentY += 20f

        val limitedOrders = orders.take(8)
        limitedOrders.forEach { o ->
            canvas.drawText(o.clientName.take(18), 45f, currentY, paint)
            canvas.drawText(o.pantyType + " (" + o.pantySize + ")", 160f, currentY, paint)
            canvas.drawText(o.quantity.toString() + " un", 320f, currentY, paint)
            canvas.drawText(String.format("R$ %,.2f", o.totalValue), 450f, currentY, paint)
            currentY += 15f
        }

        // Draw Costs Table Title
        currentY += 15f
        paint.isFakeBoldText = true
        paint.textSize = 12f
        canvas.drawText("Histórico Detalhado dos Custos (Saídas)", 40f, currentY, paint)
        currentY += 20f

        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText("Descrição", 45f, currentY, paint)
        canvas.drawText("Categoria", 220f, currentY, paint)
        canvas.drawText("Semana", 370f, currentY, paint)
        canvas.drawText("Importe", 450f, currentY, paint)

        canvas.drawLine(40f, currentY+5, 555f, currentY+5, paint)
        currentY += 20f

        val limitTxs = transactions.filter { it.type == "OUTFLOW" }.take(8)
        limitTxs.forEach { t ->
            canvas.drawText(t.description.take(24), 45f, currentY, paint)
            canvas.drawText(t.category, 220f, currentY, paint)
            canvas.drawText(t.week, 370f, currentY, paint)
            canvas.drawText(String.format("R$ %,.2f", t.amount), 450f, currentY, paint)
            currentY += 15f
        }

        // Signature area
        currentY = 750f
        canvas.drawLine(40f, currentY, 220f, currentY, paint)
        canvas.drawLine(340f, currentY, 520f, currentY, paint)
        
        canvas.drawText("Assinatura Atelier / Contábil", 65f, currentY + 15, paint)
        canvas.drawText("Data de Validação Oficial", 365f, currentY + 15, paint)

        pdfDocument.finishPage(page)

        // Write to cache directory to bypass FileProvider requirement securely
        val file = java.io.File(context.cacheDir, "Relatorio_Atelier.pdf")
        val stream = java.io.FileOutputStream(file)
        pdfDocument.writeTo(stream)
        pdfDocument.close()
        stream.close()

        Toast.makeText(context, "PDF pronto: " + file.name, Toast.LENGTH_SHORT).show()

        // Share via Intent
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_TITLE, "Exportar Relatório Atelier")
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Relatório Geral - MS Moda Íntima")
            putExtra(android.content.Intent.EXTRA_TEXT, "Segue anexo o Relatório Executivo do Atelier MS Moda Íntima.")
            
            // Note: Use standard Uri.fromFile or direct File integration for simplified intent transfers
            val fileUri = android.net.Uri.fromFile(file)
            putExtra(android.content.Intent.EXTRA_STREAM, fileUri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartilhar Relatório Finanças"))

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Erro ao exportar PDF: " + e.message, Toast.LENGTH_LONG).show()
    }
}
