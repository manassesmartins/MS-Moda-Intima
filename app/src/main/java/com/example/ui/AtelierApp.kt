package com.example.ui

import java.util.Locale
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.data.*
import com.example.ui.theme.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MsModaIntimaApp(viewModel: TransactionViewModel) {
    val context = LocalContext.current
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    val updaterStatus by viewModel.updaterStatus.collectAsStateWithLifecycle()
    val appName by viewModel.appName.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val updater = remember { viewModel.getUpdater(context) }

    LaunchedEffect(Unit) {
        viewModel.checkForUpdatesSilently(context)
    }

    if (!isUserLoggedIn) {
        Box(modifier = Modifier.fillMaxSize()) {
            MsModaIntimaLoginScreen(viewModel = viewModel)

            // Overlays on top of Login Screen
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
                                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
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
                                    progress = { stat.progress },
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
        }
        return
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
                        appName = appName,
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
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
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
                                progress = { stat.progress },
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
    appName: String,
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
                    text = appName,
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
