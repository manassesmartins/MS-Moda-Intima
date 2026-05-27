package com.example.ui

import androidx.lifecycle.ViewModel
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TransactionEntity
import com.example.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppTab {
    DASHBOARD,
    TRANSACTIONS,
    ORDERS,
    CALCS,
    SETTINGS
}

data class FinancialSummary(
    val currentBalance: Double,
    val totalInflow: Double,
    val totalOutflow: Double,
    val profitPercentageVsLastMonth: Double,
    val categoryBreakdown: Map<String, Double>
)

sealed class PendingDelete {
    data class Transaction(val entity: com.example.data.TransactionEntity) : PendingDelete()
    data class Category(val entity: com.example.data.CategoryEntity) : PendingDelete()
    data class Order(val entity: com.example.data.OrderEntity) : PendingDelete()
    data class Calculation(val entity: com.example.data.PieceCalculationEntity) : PendingDelete()
}

class TransactionViewModel(
    private val repository: TransactionRepository,
    val sessionManager: com.example.data.SessionManager
) : ViewModel() {

    // Deletion Confirmation and Undo State
    private val _showDeleteConfirmation = MutableStateFlow<PendingDelete?>(null)
    val showDeleteConfirmation: StateFlow<PendingDelete?> = _showDeleteConfirmation.asStateFlow()

    private var lastDeletedTransaction: com.example.data.TransactionEntity? = null
    private var lastDeletedCategory: com.example.data.CategoryEntity? = null
    private var lastDeletedOrder: com.example.data.OrderEntity? = null
    private var lastDeletedCalculation: com.example.data.PieceCalculationEntity? = null
    private var lastDeleteType: String? = null // "TX", "CAT", "ORDER", "CALC"

    private val _showUndoSnackbar = kotlinx.coroutines.flow.MutableSharedFlow<String>(extraBufferCapacity = 1)
    val showUndoSnackbar = _showUndoSnackbar.asSharedFlow()

    fun cancelDeleteRequest() {
        _showDeleteConfirmation.value = null
    }

    fun confirmDelete() {
        val pending = _showDeleteConfirmation.value ?: return
        _showDeleteConfirmation.value = null
        viewModelScope.launch {
            when (pending) {
                is PendingDelete.Transaction -> {
                    lastDeletedTransaction = pending.entity
                    lastDeleteType = "TX"
                    repository.deleteById(pending.entity.id)
                    triggerSyncSimulation()
                    _showUndoSnackbar.tryEmit("Lançamento removido")
                }
                is PendingDelete.Category -> {
                    lastDeletedCategory = pending.entity
                    lastDeleteType = "CAT"
                    repository.deleteCategoryById(pending.entity.id)
                    triggerSyncSimulation()
                    _showUndoSnackbar.tryEmit("Categoria removida")
                }
                is PendingDelete.Order -> {
                    lastDeletedOrder = pending.entity
                    lastDeleteType = "ORDER"
                    repository.deleteOrderById(pending.entity.id)
                    triggerSyncSimulation()
                    _showUndoSnackbar.tryEmit("Pedido removido")
                }
                is PendingDelete.Calculation -> {
                    lastDeletedCalculation = pending.entity
                    lastDeleteType = "CALC"
                    repository.deleteCalculationById(pending.entity.id)
                    triggerSyncSimulation()
                    _showUndoSnackbar.tryEmit("Cálculo de peça removido")
                }
            }
        }
    }

    fun undoLastDelete() {
        viewModelScope.launch {
            when (lastDeleteType) {
                "TX" -> {
                    lastDeletedTransaction?.let { tx ->
                        repository.insert(tx)
                        triggerSyncSimulation()
                        lastDeletedTransaction = null
                        lastDeleteType = null
                    }
                }
                "CAT" -> {
                    lastDeletedCategory?.let { cat ->
                        repository.insertCategory(cat)
                        triggerSyncSimulation()
                        lastDeletedCategory = null
                        lastDeleteType = null
                    }
                }
                "ORDER" -> {
                    lastDeletedOrder?.let { order ->
                        repository.insertOrder(order)
                        triggerSyncSimulation()
                        lastDeletedOrder = null
                        lastDeleteType = null
                    }
                }
                "CALC" -> {
                    lastDeletedCalculation?.let { calc ->
                        repository.insertCalculation(calc)
                        triggerSyncSimulation()
                        lastDeletedCalculation = null
                        lastDeleteType = null
                    }
                }
            }
        }
    }

    // Dynamic In-App GitHub Updater State Management
    private var _updater: GitHubUpdater? = null
    private val _updaterStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updaterStatus: StateFlow<UpdateStatus> = _updaterStatus.asStateFlow()

    fun getUpdater(context: Context): GitHubUpdater {
        return _updater ?: GitHubUpdater(context.applicationContext).also {
            _updater = it
            viewModelScope.launch {
                it.status.collect { status ->
                    _updaterStatus.value = status
                }
            }
        }
    }

    fun checkForUpdatesSilently(context: Context) {
        viewModelScope.launch {
            getUpdater(context).checkForUpdates(forceNotify = false)
        }
    }



    // Authentication States and Flow Controllers
    private val _isUserLoggedIn = MutableStateFlow(sessionManager.isLoggedIn)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    private fun hashPassword(password: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(password.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password
        }
    }

    fun clearAuthMessages() {
        _authError.value = null
        _authSuccessMessage.value = null
    }

    fun logoutUser() {
        sessionManager.clearSession()
        _isUserLoggedIn.value = false
        clearAuthMessages()
    }

    fun signUpUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authError.value = "E-mail e senha não podem estar em branco."
            return
        }
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            _authSuccessMessage.value = null
            try {
                if (com.example.data.api.SupabaseClient.isConfigured) {
                    val api = com.example.data.api.SupabaseClient.api
                    if (api != null) {
                        val response = api.signUp(
                            apiKey = com.example.data.api.SupabaseClient.supabaseAnonKey,
                            request = com.example.data.api.SupabaseSignUpRequest(email, password)
                        )
                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            val userId = body.user?.id ?: java.util.UUID.randomUUID().toString()
                            
                            // Save profile into local Room database for cache
                            repository.insertUser(
                                com.example.data.UserEntity(
                                    id = userId,
                                    email = email,
                                    passwordHash = hashPassword(password)
                                )
                            )
                            sessionManager.saveSession(
                                userId = userId,
                                email = email,
                                authToken = body.access_token,
                                usingSupabase = true
                            )
                            // Populate base data and push initially to start user view beautifully
                            repository.seedMockDataIfEmpty()
                            com.example.data.SupabaseSyncManager.pushLocalData(repository, sessionManager)
                            _authSuccessMessage.value = "Conta cadastrada com sucesso e sincronizada com a nuvem!"
                            _isUserLoggedIn.value = true
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: ""
                            _authError.value = "Falha na conexão: $errorMsg"
                        }
                    } else {
                        _authError.value = "Erro ao conectar com o servidor em nuvem."
                    }
                } else {
                    // Safe Offline fallback setup
                    val existing = repository.getUserByEmail(email)
                    if (existing != null) {
                        _authError.value = "Este e-mail já está cadastrado localmente!"
                    } else {
                        val localId = java.util.UUID.randomUUID().toString()
                        repository.insertUser(
                            com.example.data.UserEntity(
                                id = localId,
                                email = email,
                                passwordHash = hashPassword(password)
                            )
                        )
                        sessionManager.saveSession(
                            userId = localId,
                            email = email,
                            authToken = null,
                            usingSupabase = false
                        )
                        _authSuccessMessage.value = "Cadastro local concluído com sucesso!"
                        _isUserLoggedIn.value = true
                    }
                }
            } catch (e: Exception) {
                _authError.value = "Erro de conexão: ${e.localizedMessage}"
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authError.value = "E-mail e senha são obrigatórios."
            return
        }
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            _authSuccessMessage.value = null
            try {
                if (com.example.data.api.SupabaseClient.isConfigured) {
                    val api = com.example.data.api.SupabaseClient.api
                    if (api != null) {
                        val response = api.login(
                            apiKey = com.example.data.api.SupabaseClient.supabaseAnonKey,
                            request = com.example.data.api.SupabaseLoginRequest(email, password)
                        )
                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            val userId = body.user?.id ?: java.util.UUID.randomUUID().toString()
                            
                            // Synchronize details locally for caching
                            repository.insertUser(
                                com.example.data.UserEntity(
                                    id = userId,
                                    email = email,
                                    passwordHash = hashPassword(password)
                                )
                            )
                            sessionManager.saveSession(
                                userId = userId,
                                email = email,
                                authToken = body.access_token,
                                usingSupabase = true
                            )
                            // Pull user remote data to prevent any data loss across devices
                            isPulling = true
                            try {
                                com.example.data.SupabaseSyncManager.pullRemoteData(repository, sessionManager)
                            } finally {
                                isPulling = false
                            }
                            _authSuccessMessage.value = "Autenticação realizada! Dados sincronizados."
                            _isUserLoggedIn.value = true
                        } else {
                            // Check local fallback in case network failed or invalid, we prioritize local user safety
                            val localUser = repository.getUserByEmail(email)
                            if (localUser != null && localUser.passwordHash == hashPassword(password)) {
                                sessionManager.saveSession(
                                    userId = localUser.id,
                                    email = email,
                                    authToken = null,
                                    usingSupabase = false
                                )
                                _authSuccessMessage.value = "Autenticação local com sucesso (Cache off-line)!"
                                _isUserLoggedIn.value = true
                            } else {
                                val errorMsg = response.errorBody()?.string() ?: "Credenciais incorretas."
                                _authError.value = "Erro de login: $errorMsg"
                            }
                        }
                    } else {
                        _authError.value = "Erro de conexão com o servidor."
                    }
                } else {
                    // Safe Offline Authentication
                    val localUser = repository.getUserByEmail(email)
                    if (localUser != null) {
                        if (localUser.passwordHash == hashPassword(password)) {
                            sessionManager.saveSession(
                                userId = localUser.id,
                                email = email,
                                authToken = null,
                                usingSupabase = false
                            )
                            _authSuccessMessage.value = "Login local concluído com sucesso."
                            _isUserLoggedIn.value = true
                        } else {
                            _authError.value = "Senha incorreta!"
                        }
                    } else {
                        // Create a default master account for convenience if they enter admin/admin and DB empty
                        if (email == "admin@atelier.com" && password == "admin123") {
                            val adminId = "admin-local-uuid"
                            repository.insertUser(
                                com.example.data.UserEntity(
                                    id = adminId,
                                    email = email,
                                    passwordHash = hashPassword(password)
                                )
                            )
                            sessionManager.saveSession(
                                userId = adminId,
                                email = email,
                                authToken = null,
                                usingSupabase = false
                            )
                            _authSuccessMessage.value = "Conta administradora local inicializada!"
                            _isUserLoggedIn.value = true
                        } else {
                            _authError.value = "Usuário não encontrado. Se é o seu primeiro acesso, clique em Cadastrar!"
                        }
                    }
                }
            } catch (e: Exception) {
                // Connection exception - fallback to local login
                val localUser = repository.getUserByEmail(email)
                if (localUser != null && localUser.passwordHash == hashPassword(password)) {
                    sessionManager.saveSession(
                        userId = localUser.id,
                        email = email,
                        authToken = null,
                        usingSupabase = false
                    )
                    _authSuccessMessage.value = "Autenticado off-line de forma segura!"
                    _isUserLoggedIn.value = true
                } else {
                    _authError.value = "Erro de rede: ${e.localizedMessage}. Sem cache offline coincidente."
                }
            } finally {
                _authLoading.value = false
            }
        }
    }

    // Navigation and UX states
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _isAddingTransaction = MutableStateFlow(false)
    val isAddingTransaction: StateFlow<Boolean> = _isAddingTransaction.asStateFlow()

    // Transaction filter tab ("Tudo", "Entradas", "Saídas")
    private val _transactionFilter = MutableStateFlow("SAIDAS") // "TUDO", "ENTRADAS", "SAIDAS"
    val transactionFilter: StateFlow<String> = _transactionFilter.asStateFlow()

    // Cloud Synchronicity Status
    private val _isCloudBackupEnabled = MutableStateFlow(true)
    val isCloudBackupEnabled: StateFlow<Boolean> = _isCloudBackupEnabled.asStateFlow()

    private val _syncState = MutableStateFlow("SYNCED") // "SYNCED", "SAVED_OFFLINE", "SYNCING"
    val syncState: StateFlow<String> = _syncState.asStateFlow()

    // Expose transaction list
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Expose Categories and Orders
    val allCategories: StateFlow<List<com.example.data.CategoryEntity>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allOrders: StateFlow<List<com.example.data.OrderEntity>> = repository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCalculations: StateFlow<List<com.example.data.PieceCalculationEntity>> = repository.allCalculations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    // Filtered transaction list
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _transactionFilter
    ) { list, _ ->
        list.filter { it.type == "OUTFLOW" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial summaries computed reactively
    val summary: StateFlow<FinancialSummary> = kotlinx.coroutines.flow.combine(allTransactions, allOrders) { transactions, orders ->
        val totalIn = orders.sumOf { it.totalValue }
        val totalOut = transactions.filter { it.type == "OUTFLOW" }.sumOf { it.amount }
        val balance = totalIn - totalOut

        // Calculate dynamic category percentages based on OUTFLOW
        val outflowTransactions = transactions.filter { it.type == "OUTFLOW" }
        val totalOutflowSum = outflowTransactions.sumOf { it.amount }

        val breakdown = mutableMapOf<String, Double>()
        if (totalOutflowSum > 0) {
            val grouped = outflowTransactions.groupBy { it.category }
            for ((cat, items) in grouped) {
                val catSum = items.sumOf { it.amount }
                breakdown[cat] = (catSum / totalOutflowSum) * 100.0
            }
        } else {
            // Fallback details matching visual mockup percentages exactly
            breakdown["Matéria-prima"] = 45.0
            breakdown["Mão de Obra"] = 30.0
            breakdown["Logística & Envios"] = 15.0
            breakdown["Marketing"] = 10.0
        }

        FinancialSummary(
            currentBalance = balance,
            totalInflow = totalIn,
            totalOutflow = totalOut,
            profitPercentageVsLastMonth = 12.4, // Baseline
            categoryBreakdown = breakdown
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialSummary(0.0, 0.0, 0.0, 12.4, emptyMap())
    )

    // Automatic DB Sincronization Control
    private var isPulling = false
    private var syncJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            isPulling = true
            try {
                repository.seedMockDataIfEmpty()
            } catch (e: Exception) {
                android.util.Log.e("TransactionViewModel", "Error seeding data", e)
            } finally {
                isPulling = false
            }
        }

        // Automatic Cloud Sync observer on Database changes
        viewModelScope.launch {
            combine(
                repository.allTransactions,
                repository.allCategories,
                repository.allOrders,
                repository.allCalculations
            ) { _, _, _, _ -> true }.collect {
                if (!isPulling && _isCloudBackupEnabled.value) {
                    syncJob?.cancel()
                    syncJob = launch {
                        kotlinx.coroutines.delay(1000)
                        triggerSyncSimulation()
                    }
                }
            }
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setAddingTransaction(adding: Boolean) {
        _isAddingTransaction.value = adding
    }

    fun setTransactionFilter(filter: String) {
        _transactionFilter.value = filter
    }

    fun setCloudBackupEnabled(enabled: Boolean) {
        _isCloudBackupEnabled.value = enabled
        if (enabled) {
            triggerSyncSimulation()
        } else {
            _syncState.value = "MUTED"
        }
    }

    fun triggerSyncSimulation() {
        if (!_isCloudBackupEnabled.value) return
        viewModelScope.launch {
            _syncState.value = "SYNCING"
            val success = if (com.example.data.api.SupabaseClient.isConfigured && sessionManager.isLoggedIn) {
                com.example.data.SupabaseSyncManager.pushLocalData(repository, sessionManager)
            } else {
                kotlinx.coroutines.delay(1200)
                true
            }
            if (success) {
                _syncState.value = "SYNCED"
            } else {
                _syncState.value = "ERROR_SYNC"
            }
        }
    }

    fun addTransaction(
        description: String,
        amount: Double,
        category: String,
        type: String, // "INFLOW" or "OUTFLOW"
        dateString: String = "",
        week: String = "1ª Semana"
    ) {
        viewModelScope.launch {
            val finalDateString = if (dateString.isEmpty()) {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR"))
                sdf.format(Date()).uppercase(Locale.getDefault())
            } else {
                dateString
            }

            val transaction = TransactionEntity(
                id = 0,
                description = description,
                amount = amount,
                type = type,
                category = category,
                dateString = finalDateString,
                timestamp = System.currentTimeMillis(),
                synced = _isCloudBackupEnabled.value,
                extraText = if (type == "INFLOW") "Venda Direta" else "Despesa",
                week = week
            )

            repository.insert(transaction)
            triggerSyncSimulation()
            _isAddingTransaction.value = false
        }
    }

    fun editTransaction(
        id: Long,
        description: String,
        amount: Double,
        category: String,
        type: String, // "INFLOW" or "OUTFLOW"
        dateString: String = "",
        week: String = "1ª Semana"
    ) {
        viewModelScope.launch {
            val finalDateString = if (dateString.isEmpty()) {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR"))
                sdf.format(Date()).uppercase(Locale.getDefault())
            } else {
                dateString
            }

            val transaction = TransactionEntity(
                id = id,
                description = description,
                amount = amount,
                type = type,
                category = category,
                dateString = finalDateString,
                timestamp = System.currentTimeMillis(),
                synced = _isCloudBackupEnabled.value,
                extraText = if (type == "INFLOW") "Venda Direta" else "Despesa",
                week = week
            )

            repository.insert(transaction)
            triggerSyncSimulation()
        }
    }

    fun addCategory(name: String, type: String) {
        viewModelScope.launch {
            repository.insertCategory(com.example.data.CategoryEntity(name = name, type = type))
            triggerSyncSimulation()
        }
    }

    fun updateCategory(id: Long, name: String, type: String) {
        viewModelScope.launch {
            repository.insertCategory(com.example.data.CategoryEntity(id = id, name = name, type = type))
            triggerSyncSimulation()
        }
    }

    fun deleteCategory(id: Long) {
        val cat = allCategories.value.find { it.id == id }
        if (cat != null) {
            _showDeleteConfirmation.value = PendingDelete.Category(cat)
        }
    }

    fun addOrder(clientName: String, pantyType: String, pantySize: String, quantity: Int, pantyValue: Double, week: String) {
        viewModelScope.launch {
            val totalValue = quantity * pantyValue
            val order = com.example.data.OrderEntity(
                clientName = clientName,
                pantyType = pantyType,
                pantySize = pantySize,
                quantity = quantity,
                pantyValue = pantyValue,
                totalValue = totalValue,
                week = week,
                timestamp = System.currentTimeMillis()
            )
            repository.insertOrder(order)
            triggerSyncSimulation()
        }
    }

    fun editOrder(id: Long, clientName: String, pantyType: String, pantySize: String, quantity: Int, pantyValue: Double, week: String) {
        viewModelScope.launch {
            val totalValue = quantity * pantyValue
            val order = com.example.data.OrderEntity(
                id = id,
                clientName = clientName,
                pantyType = pantyType,
                pantySize = pantySize,
                quantity = quantity,
                pantyValue = pantyValue,
                totalValue = totalValue,
                week = week,
                timestamp = System.currentTimeMillis()
            )
            repository.insertOrder(order)
            triggerSyncSimulation()
        }
    }

    fun deleteOrder(id: Long) {
        val order = allOrders.value.find { it.id == id }
        if (order != null) {
            _showDeleteConfirmation.value = PendingDelete.Order(order)
        }
    }

    fun deleteTransaction(id: Long) {
        val tx = allTransactions.value.find { it.id == id }
        if (tx != null) {
            _showDeleteConfirmation.value = PendingDelete.Transaction(tx)
        }
    }

    fun clearAllDataAndReseed() {
        viewModelScope.launch {
            isPulling = true
            try {
                _isCloudBackupEnabled.value = true
                repository.clearAll()
                repository.seedMockDataIfEmpty()
                _syncState.value = "SYNCED"
            } finally {
                isPulling = false
            }
        }
    }

    // Helper Extension to map a flow of lists of entities to computed properties
    private fun StateFlow<List<TransactionEntity>>.mapToSummary(): kotlinx.coroutines.flow.Flow<FinancialSummary> {
        return this.map { transactions ->
            val totalIn = transactions.filter { it.type == "INFLOW" }.sumOf { it.amount }
            val totalOut = transactions.filter { it.type == "OUTFLOW" }.sumOf { it.amount }
            val balance = totalIn - totalOut

            // Calculate dynamic category percentages based on OUTFLOW
            val outflowTransactions = transactions.filter { it.type == "OUTFLOW" }
            val totalOutflowSum = outflowTransactions.sumOf { it.amount }

            val breakdown = mutableMapOf<String, Double>()
            if (totalOutflowSum > 0) {
                val grouped = outflowTransactions.groupBy { it.category }
                for ((cat, items) in grouped) {
                    val catSum = items.sumOf { it.amount }
                    breakdown[cat] = (catSum / totalOutflowSum) * 100.0
                }
            } else {
                // Fallback details matching visual mockup percentages exactly
                breakdown["Matéria-prima"] = 45.0
                breakdown["Mão de Obra"] = 30.0
                breakdown["Logística & Envios"] = 15.0
                breakdown["Marketing"] = 10.0
            }

            FinancialSummary(
                currentBalance = balance,
                totalInflow = totalIn,
                totalOutflow = totalOut,
                profitPercentageVsLastMonth = 12.4, // Baseline
                categoryBreakdown = breakdown
            )
        }
    }

    // Piece calculation CRUD operations
    fun addCalculation(pano: String, kg: Double?, valorKg: Double?, quantidade: Int?) {
        viewModelScope.launch {
            repository.insertCalculation(
                com.example.data.PieceCalculationEntity(
                    pano = pano,
                    kg = kg,
                    valorKg = valorKg,
                    quantidade = quantidade,
                    timestamp = System.currentTimeMillis()
                )
            )
            triggerSyncSimulation()
        }
    }

    fun updateCalculation(id: Long, pano: String, kg: Double?, valorKg: Double?, quantidade: Int?) {
        viewModelScope.launch {
            repository.insertCalculation(
                com.example.data.PieceCalculationEntity(
                    id = id,
                    pano = pano,
                    kg = kg,
                    valorKg = valorKg,
                    quantidade = quantidade,
                    timestamp = System.currentTimeMillis() // Shows date of last alteration!
                )
            )
            triggerSyncSimulation()
        }
    }

    fun deleteCalculation(id: Long) {
        val calc = allCalculations.value.find { it.id == id }
        if (calc != null) {
            _showDeleteConfirmation.value = PendingDelete.Calculation(calc)
        }
    }

    fun clearCalculationsAndReseed() {
        viewModelScope.launch {
            repository.clearCalculations()
            triggerSyncSimulation()
        }
    }
}

class TransactionViewModelFactory(
    private val repository: TransactionRepository,
    private val sessionManager: com.example.data.SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
