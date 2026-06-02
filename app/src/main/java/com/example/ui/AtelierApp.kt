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
import coil.compose.AsyncImage
import androidx.compose.ui.composed
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.utils.rememberBitmapFromBase64
import com.example.ui.screens.ProfileSettingsPopup


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MsModaIntimaApp(viewModel: TransactionViewModel) {
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
        }
        return
    }

    val isBrandLoaded by viewModel.isBrandLoaded.collectAsStateWithLifecycle()
    val brandConfig by viewModel.brandConfig.collectAsStateWithLifecycle()

    if (!isBrandLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    text = "Carregando configurações...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        return
    }

    if (brandConfig == null || !brandConfig!!.isConfigured) {
        Box(modifier = Modifier.fillMaxSize()) {
            com.example.ui.screens.BusinessSetupScreen(viewModel = viewModel)
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

    var transactionForOptions by remember { mutableStateOf<com.example.data.TransactionEntity?>(null) }
    var transactionToEdit by remember { mutableStateOf<com.example.data.TransactionEntity?>(null) }
    var showProfileSettingsDialog by remember { mutableStateOf(false) }

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

    if (showProfileSettingsDialog) {
        ProfileSettingsPopup(
            onDismiss = { showProfileSettingsDialog = false },
            viewModel = viewModel
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SurfaceDark,
                        androidx.compose.ui.graphics.lerp(SurfaceDark, Primary, 0.07f),
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
                    onTabSelected = { viewModel.setTab(it) },
                    brandConfig = brandConfig
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
                        brandConfig = brandConfig,
                        onProfileClick = { showProfileSettingsDialog = true },
                        context = context
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
                                viewModel = viewModel,
                                onItemClick = { tx -> transactionForOptions = tx }
                            )
                            AppTab.TRANSACTIONS -> TransactionsScreen(
                                viewModel = viewModel,
                                transactions = transactions,
                                activeFilter = filterTab,
                                onFilterChanged = { viewModel.setTransactionFilter(it) },
                                onItemClick = { tx -> transactionForOptions = tx }
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

        // Options Dialog overlay for true cash flow CRUD system
        transactionForOptions?.let { tx ->
            AlertDialog(
                onDismissRequest = { transactionForOptions = null },
                title = {
                    Column {
                        Text(
                            text = tx.description,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Opções do Lançamento",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Valor:", fontSize = 14.sp, color = OnSurfaceVariant)
                            Text(
                                text = String.format(Locale("pt", "BR"), "R$ %,.2f", tx.amount),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.type == "INFLOW") Tertiary else ErrorColor
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Categoria:", fontSize = 14.sp, color = OnSurfaceVariant)
                            Text(text = tx.category, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Período:", fontSize = 14.sp, color = OnSurfaceVariant)
                            Text(text = tx.week, fontSize = 14.sp, color = OnSurface)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Data:", fontSize = 14.sp, color = OnSurfaceVariant)
                            Text(text = tx.dateString, fontSize = 14.sp, color = OnSurface)
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Edit button using weight(1f)
                        Button(
                            onClick = {
                                transactionToEdit = tx
                                transactionForOptions = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = OnPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar", fontSize = 13.sp)
                        }

                        // Delete button using weight(1f)
                        Button(
                            onClick = {
                                viewModel.deleteTransaction(tx.id)
                                transactionForOptions = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ErrorColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excluir", fontSize = 13.sp)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { transactionForOptions = null }
                    ) {
                        Text("Fechar", color = OnSurfaceVariant)
                    }
                },
                containerColor = SurfaceContainerHigh
            )
        }

        // Overlay: "Novo Lançamento / Editar Lançamento" View
        AnimatedVisibility(
            visible = isAddingTransaction || (transactionToEdit != null),
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
                modifier = if (isWideScreen) Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { 
                    viewModel.setAddingTransaction(false)
                    transactionToEdit = null
                } else Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = overlayModifier.clickable(enabled = false) { }
                ) {
                    NewTransactionScreen(
                        viewModel = viewModel,
                        isCloudBackupEnabled = isCloudBackupEnabled,
                        onDismiss = { 
                            viewModel.setAddingTransaction(false)
                            transactionToEdit = null
                        },
                        onSubmit = { desc, amount, cat, type, week ->
                            val toEdit = transactionToEdit
                            if (toEdit != null) {
                                viewModel.editTransaction(toEdit.id, desc, amount, cat, type, toEdit.dateString, week)
                                Toast.makeText(context, "Lançamento atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                                transactionToEdit = null
                            } else {
                                viewModel.addTransaction(desc, amount, cat, type, week = week)
                                Toast.makeText(context, "Lançamento salvo com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        transactionToEdit = transactionToEdit
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

            is UpdateStatus.UpToDate -> {
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
                                tint = Color(0xFF34D399), // custom beautiful emerald green
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Sem Atualizações",
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        }
                    },
                    containerColor = SurfaceContainerHigh,
                    text = {
                        Text(
                            text = "Seu aplicativo está totalmente atualizado com a nuvem na versão mais recente!\n\n(Versão instalada: ${updater.getLocalVersion()})",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { updater.clearStatus() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Concluído", fontWeight = FontWeight.Bold)
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
            containerColor = getGlassContainerColor()
        ),
        border = getGlassBorderStroke(1.5.dp)
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
    brandConfig: com.example.data.BrandConfigEntity?,
    onProfileClick: () -> Unit,
    context: android.content.Context
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

    // Active network checker
    var isConnected by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            isConnected = try {
                val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
                val activeNetwork = cm?.activeNetwork
                val caps = cm?.getNetworkCapabilities(activeNetwork)
                caps?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            } catch (e: Exception) {
                false
            }
            kotlinx.coroutines.delay(4000)
        }
    }

    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = appName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    fontFamily = FontFamily.SansSerif
                )
                // Small indicator dot (green connected / gray disconnected)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) Color(0xFF10B981) else Color(0xFF9CA3AF))
                )
            }
        },
        actions = {
            // Profile icon or custom brand logo trigger button
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.12f))
                    .border(1.dp, Primary.copy(alpha = 0.25f), CircleShape)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                val decodedLogo = rememberBitmapFromBase64(brandConfig?.logoImage)
                if (decodedLogo != null) {
                    androidx.compose.foundation.Image(
                        bitmap = decodedLogo,
                        contentDescription = "Logo",
                        modifier = Modifier.size(34.dp).clip(CircleShape)
                    )
                } else {
                    val iconsMap = mapOf(
                        "CROWN" to Icons.Default.Star,
                        "BAG" to Icons.Default.ShoppingCart,
                        "HEART" to Icons.Default.Favorite,
                        "BUILD" to Icons.Default.Build,
                        "PERSON" to Icons.Default.Person
                    )
                    val iconVec = iconsMap[brandConfig?.logoIcon ?: "CROWN"] ?: Icons.Default.Star
                    Icon(
                        imageVector = iconVec,
                        contentDescription = "Ajustes",
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = if (MaterialTheme.colorScheme.background.isColorLight()) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            } else {
                Color.White.copy(alpha = 0.05f)
            },
            titleContentColor = Primary
        ),
        modifier = Modifier.drawBehindGlassBorder()
    )
}


// Custom Glass Border modifier
fun Modifier.drawBehindGlassBorder(): Modifier = this.composed {
    val isLight = MaterialTheme.colorScheme.background.isColorLight()
    val borderColor = if (isLight) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    } else {
        Color.White.copy(alpha = 0.15f)
    }
    this.border(
        width = 1.dp,
        color = borderColor
    )
}

// ------------------------------------
// COMPONENT: Bottom Nav Bar
// ------------------------------------
@Composable
fun MsModaIntimaBottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
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

    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.05f),
        tonalElevation = 0.dp,
        modifier = Modifier
            .drawBehindGlassBorder()
            .height(80.dp)
    ) {
        val tabs = listOf(
            Triple(AppTab.DASHBOARD, Icons.Default.Home, "Painel"),
            Triple(AppTab.TRANSACTIONS, Icons.Default.List, "Fluxo Caixa"),
            Triple(AppTab.ORDERS, Icons.Default.ShoppingCart, "Pedidos"),
            Triple(AppTab.CALCS, Icons.Default.Star, "Custo Peças")
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
    brandConfig: com.example.data.BrandConfigEntity?,
    modifier: Modifier = Modifier
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

    NavigationRail(
        containerColor = Color.White.copy(alpha = 0.05f),
        modifier = modifier
            .drawBehindGlassBorder()
            .fillMaxHeight()
            .width(88.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Brand logo/letters matching user business style
        val logoText = brandConfig?.logoText ?: "MS"
        val logoIconName = brandConfig?.logoIcon ?: "CROWN"
        val iconsMap = mapOf(
            "CROWN" to Icons.Default.Star,
            "BAG" to Icons.Default.ShoppingCart,
            "HEART" to Icons.Default.Favorite,
            "BUILD" to Icons.Default.Build,
            "PERSON" to Icons.Default.Person
        )
        val selectedIcon = iconsMap[logoIconName] ?: Icons.Default.Star

        Box(
            modifier = Modifier
                .size(54.dp)
                .background(Primary.copy(alpha = 0.15f), CircleShape)
                .border(1.5.dp, Primary, CircleShape)
                .glow(Primary),
            contentAlignment = Alignment.Center
        ) {
            val customLogoImg = brandConfig?.logoImage
            if (!customLogoImg.isNullOrBlank()) {
                AsyncImage(
                    model = customLogoImg,
                    contentDescription = "Logo da Marca",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = selectedIcon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = logoText.uppercase().take(3),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface,
                        lineHeight = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val tabs = listOf(
            Triple(AppTab.DASHBOARD, Icons.Default.Home, "Painel de Negócios"),
            Triple(AppTab.TRANSACTIONS, Icons.Default.List, "Fluxo de Caixa"),
            Triple(AppTab.ORDERS, Icons.Default.ShoppingCart, "Agendamento de Pedidos"),
            Triple(AppTab.CALCS, Icons.Default.Star, "Custo de Peças")
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
