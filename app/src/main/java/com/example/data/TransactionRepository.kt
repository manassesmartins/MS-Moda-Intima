package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val orderDao: OrderDao,
    private val pieceCalculationDao: PieceCalculationDao,
    val userDao: UserDao
) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val allCalculations: Flow<List<PieceCalculationEntity>> = pieceCalculationDao.getAllCalculations()

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun insertCalculation(entity: PieceCalculationEntity): Long {
        return pieceCalculationDao.insertCalculation(entity)
    }

    suspend fun deleteCalculationById(id: Long) {
        pieceCalculationDao.deleteCalculationById(id)
    }

    suspend fun clearCalculations() {
        pieceCalculationDao.deleteAll()
    }

    suspend fun insert(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun clearAll() {
        transactionDao.deleteAll()
        categoryDao.deleteAll()
        orderDao.deleteAll()
        pieceCalculationDao.deleteAll()
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun deleteCategoryById(id: Long) {
        categoryDao.deleteCategoryById(id)
    }

    suspend fun insertOrder(order: OrderEntity): Long {
        return orderDao.insertOrder(order)
    }

    suspend fun deleteOrderById(id: Long) {
        orderDao.deleteOrderById(id)
    }


    suspend fun seedMockDataIfEmpty() = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            // 1. Seed Categories if empty
            val categories = categoryDao.getAllCategories().first()
            if (categories.isEmpty()) {
                val defaultCategories = listOf(
                    CategoryEntity(name = "Vendas", type = "INFLOW"),
                    CategoryEntity(name = "Encomendas", type = "INFLOW"),
                    CategoryEntity(name = "Outros", type = "INFLOW"),
                    CategoryEntity(name = "Pano", type = "OUTFLOW"),
                    CategoryEntity(name = "Viés", type = "OUTFLOW"),
                    CategoryEntity(name = "Linha", type = "OUTFLOW"),
                    CategoryEntity(name = "Etiqueta de composição", type = "OUTFLOW"),
                    CategoryEntity(name = "Etiqueta lateral", type = "OUTFLOW"),
                    CategoryEntity(name = "Forro", type = "OUTFLOW"),
                    CategoryEntity(name = "Manutenção", type = "OUTFLOW"),
                    CategoryEntity(name = "Funcionários", type = "OUTFLOW"),
                    CategoryEntity(name = "Variados", type = "OUTFLOW")
                )
                for (c in defaultCategories) {
                    categoryDao.insertCategory(c)
                }
            }

            // 2. Seed Orders if empty (represents inflows / "ENTRADAS DO MÊS")
            val orders = orderDao.getAllOrders().first()
            if (orders.isEmpty()) {
                val defaultOrders = listOf(
                    // 1ª Semana
                    OrderEntity(clientName = "Sandro", pantyType = "Microfibra Larga", pantySize = "U", quantity = 2000, pantyValue = 1.30, totalValue = 2600.00, week = "1ª Semana"),
                    OrderEntity(clientName = "Sandro", pantyType = "Listrada", pantySize = "G", quantity = 1000, pantyValue = 1.30, totalValue = 1300.00, week = "1ª Semana"),
                    OrderEntity(clientName = "Evanderson", pantyType = "Listrada", pantySize = "P", quantity = 200, pantyValue = 1.10, totalValue = 220.00, week = "1ª Semana"),
                    OrderEntity(clientName = "Evanderson", pantyType = "Listrada", pantySize = "M", quantity = 400, pantyValue = 1.20, totalValue = 480.00, week = "1ª Semana"),
                    OrderEntity(clientName = "Evanderson", pantyType = "Listrada", pantySize = "G", quantity = 400, pantyValue = 1.30, totalValue = 520.00, week = "1ª Semana"),
                    // 2ª Semana
                    OrderEntity(clientName = "Dedé", pantyType = "Juvenil", pantySize = "U", quantity = 2250, pantyValue = 0.90, totalValue = 2025.00, week = "2ª Semana"),
                    OrderEntity(clientName = "Sandro", pantyType = "Listrada", pantySize = "P", quantity = 1000, pantyValue = 1.10, totalValue = 1100.00, week = "2ª Semana"),
                    // 3ª Semana
                    OrderEntity(clientName = "Evanderson", pantyType = "Microfibra Larga", pantySize = "U", quantity = 1100, pantyValue = 1.30, totalValue = 1430.00, week = "3ª Semana"),
                    OrderEntity(clientName = "Evanderson", pantyType = "Microfibra Fina", pantySize = "U", quantity = 1400, pantyValue = 1.30, totalValue = 1820.00, week = "3ª Semana"),
                    OrderEntity(clientName = "Enie", pantyType = "Microfibra Larga", pantySize = "U", quantity = 100, pantyValue = 1.30, totalValue = 130.00, week = "3ª Semana"),
                    OrderEntity(clientName = "Enie", pantyType = "Microfibra Fina", pantySize = "U", quantity = 500, pantyValue = 1.30, totalValue = 650.00, week = "3ª Semana"),
                    // 4ª Semana
                    OrderEntity(clientName = "Dedé", pantyType = "Juvenil", pantySize = "U", quantity = 2500, pantyValue = 0.90, totalValue = 2250.00, week = "4ª Semana"),
                    OrderEntity(clientName = "Sandro", pantyType = "Listrada", pantySize = "G", quantity = 1000, pantyValue = 1.30, totalValue = 1300.00, week = "4ª Semana")
                )
                for (o in defaultOrders) {
                    orderDao.insertOrder(o)
                }
            }

            // 3. Seed Transactions if empty (specifically to show "SAIDAS DO MÊS" and Employee Payments)
            val transactions = transactionDao.getAllTransactions().first()
            if (transactions.isEmpty()) {
                val now = System.currentTimeMillis()
                val defaultTransactions = listOf(
                    // Week 1 Exits
                    TransactionEntity(description = "Pano Lote 1", amount = 200.00, type = "OUTFLOW", category = "Pano", dateString = "1ª Semana", week = "1ª Semana"),
                    TransactionEntity(description = "Pano Lote 2", amount = 523.00, type = "OUTFLOW", category = "Pano", dateString = "1ª Semana", week = "1ª Semana"),
                    TransactionEntity(description = "Pano Lote 3", amount = 454.59, type = "OUTFLOW", category = "Pano", dateString = "1ª Semana", week = "1ª Semana"),
                    TransactionEntity(description = "Viés Elástico", amount = 154.00, type = "OUTFLOW", category = "Viés", dateString = "1ª Semana", week = "1ª Semana"),
                    TransactionEntity(description = "Viés Algodão", amount = 333.60, type = "OUTFLOW", category = "Viés", dateString = "1ª Semana", week = "1ª Semana"),
                    TransactionEntity(description = "Viés Poliéster", amount = 56.00, type = "OUTFLOW", category = "Viés", dateString = "1ª Semana", week = "1ª Semana"),
                    TransactionEntity(description = "Etiquetas Pontas", amount = 50.00, type = "OUTFLOW", category = "Etiqueta lateral", dateString = "1ª Semana", week = "1ª Semana"),
                    TransactionEntity(description = "Fios para Costura", amount = 50.00, type = "OUTFLOW", category = "Linha", dateString = "1ª Semana", week = "1ª Semana"),
                    TransactionEntity(description = "Diversos Linhas", amount = 54.00, type = "OUTFLOW", category = "Variados", dateString = "1ª Semana", week = "1ª Semana"),
                    // Week 1 Employee Payments
                    TransactionEntity(description = "Brenda (Salário)", amount = 350.00, type = "OUTFLOW", category = "Funcionários", dateString = "1ª Semana", week = "1ª Semana", extraText = "Perdeu 1 dia: doença"),
                    TransactionEntity(description = "Ezequias (Salário)", amount = 100.00, type = "OUTFLOW", category = "Funcionários", dateString = "1ª Semana", week = "1ª Semana"),
                    TransactionEntity(description = "Leonardo (Salário)", amount = 125.00, type = "OUTFLOW", category = "Funcionários", dateString = "1ª Semana", week = "1ª Semana"),
                    TransactionEntity(description = "Radija (Salário)", amount = 50.00, type = "OUTFLOW", category = "Funcionários", dateString = "1ª Semana", week = "1ª Semana"),
                    TransactionEntity(description = "Vera (Salário)", amount = 100.00, type = "OUTFLOW", category = "Funcionários", dateString = "1ª Semana", week = "1ª Semana"),
                    TransactionEntity(description = "Wuénia (Salário)", amount = 350.00, type = "OUTFLOW", category = "Funcionários", dateString = "1ª Semana", week = "1ª Semana"),

                    // Week 2 Exits
                    TransactionEntity(description = "Fios de Pano", amount = 326.00, type = "OUTFLOW", category = "Pano", dateString = "2ª Semana", week = "2ª Semana"),
                    TransactionEntity(description = "Fios de Pano 2", amount = 175.00, type = "OUTFLOW", category = "Pano", dateString = "2ª Semana", week = "2ª Semana"),
                    TransactionEntity(description = "Fios de Pano 3", amount = 360.00, type = "OUTFLOW", category = "Pano", dateString = "2ª Semana", week = "2ª Semana"),
                    TransactionEntity(description = "Viés Extra", amount = 700.00, type = "OUTFLOW", category = "Viés", dateString = "2ª Semana", week = "2ª Semana"),
                    TransactionEntity(description = "Viés Suporte", amount = 124.00, type = "OUTFLOW", category = "Viés", dateString = "2ª Semana", week = "2ª Semana"),
                    TransactionEntity(description = "Viés Interno", amount = 106.00, type = "OUTFLOW", category = "Viés", dateString = "2ª Semana", week = "2ª Semana"),
                    TransactionEntity(description = "Etiqueta Composição", amount = 60.00, type = "OUTFLOW", category = "Etiqueta de composição", dateString = "2ª Semana", week = "2ª Semana"),
                    TransactionEntity(description = "Linha Fina", amount = 36.00, type = "OUTFLOW", category = "Linha", dateString = "2ª Semana", week = "2ª Semana"),
                    TransactionEntity(description = "Diversos Botões", amount = 15.00, type = "OUTFLOW", category = "Variados", dateString = "2ª Semana", week = "2ª Semana"),
                    // Week 2 Employee Payments
                    TransactionEntity(description = "Brenda (Salário)", amount = 250.00, type = "OUTFLOW", category = "Funcionários", dateString = "2ª Semana", week = "2ª Semana"),
                    TransactionEntity(description = "Ezequias (Salário)", amount = 100.00, type = "OUTFLOW", category = "Funcionários", dateString = "2ª Semana", week = "2ª Semana"),
                    TransactionEntity(description = "Leonardo (Salário)", amount = 75.00, type = "OUTFLOW", category = "Funcionários", dateString = "2ª Semana", week = "2ª Semana"),
                    TransactionEntity(description = "Vera (Salário)", amount = 100.00, type = "OUTFLOW", category = "Funcionários", dateString = "2ª Semana", week = "2ª Semana"),
                    TransactionEntity(description = "Wuénia (Salário)", amount = 350.00, type = "OUTFLOW", category = "Funcionários", dateString = "2ª Semana", week = "2ª Semana"),

                    // Week 3 Exits
                    TransactionEntity(description = "Compra Pano Algodão", amount = 1150.00, type = "OUTFLOW", category = "Pano", dateString = "3ª Semana", week = "3ª Semana"),
                    TransactionEntity(description = "Retalhos Pano", amount = 79.00, type = "OUTFLOW", category = "Pano", dateString = "3ª Semana", week = "3ª Semana"),
                    TransactionEntity(description = "Viés Geral", amount = 359.00, type = "OUTFLOW", category = "Viés", dateString = "3ª Semana", week = "3ª Semana"),
                    TransactionEntity(description = "Etiqueta Comp Lote", amount = 50.00, type = "OUTFLOW", category = "Etiqueta de composição", dateString = "3ª Semana", week = "3ª Semana"),
                    // Week 3 Employee Payments
                    TransactionEntity(description = "Brenda (Salário)", amount = 350.00, type = "OUTFLOW", category = "Funcionários", dateString = "3ª Semana", week = "3ª Semana"),
                    TransactionEntity(description = "Ezequias (Salário)", amount = 100.00, type = "OUTFLOW", category = "Funcionários", dateString = "3ª Semana", week = "3ª Semana"),
                    TransactionEntity(description = "Vera (Salário)", amount = 100.00, type = "OUTFLOW", category = "Funcionários", dateString = "3ª Semana", week = "3ª Semana"),
                    TransactionEntity(description = "Wuénia (Salário)", amount = 350.00, type = "OUTFLOW", category = "Funcionários", dateString = "3ª Semana", week = "3ª Semana"),

                    // Week 4 Exits
                    TransactionEntity(description = "Pano Rolo Grande", amount = 411.00, type = "OUTFLOW", category = "Pano", dateString = "4ª Semana", week = "4ª Semana"),
                    TransactionEntity(description = "Pano Suave", amount = 172.00, type = "OUTFLOW", category = "Pano", dateString = "4ª Semana", week = "4ª Semana"),
                    TransactionEntity(description = "Viés Colorido", amount = 775.00, type = "OUTFLOW", category = "Viés", dateString = "4ª Semana", week = "4ª Semana"),
                    TransactionEntity(description = "Viés Liso G", amount = 250.00, type = "OUTFLOW", category = "Viés", dateString = "4ª Semana", week = "4ª Semana"),
                    TransactionEntity(description = "Etiqueta Composição G", amount = 60.00, type = "OUTFLOW", category = "Etiqueta de composição", dateString = "4ª Semana", week = "4ª Semana"),
                    TransactionEntity(description = "Etiqueta Lateral Lote 4", amount = 50.00, type = "OUTFLOW", category = "Etiqueta lateral", dateString = "4ª Semana", week = "4ª Semana"),

                    // Dynamic extra: seed some generic inflows so traditional UI works too
                    TransactionEntity(description = "Receita Geral MS Moda Íntima", amount = 500.00, type = "INFLOW", category = "Vendas", dateString = "Ontem", week = "1ª Semana")
                )
                for (t in defaultTransactions) {
                    transactionDao.insertTransaction(t)
                }
            }

            // 4. Seed Piece Calculations if empty
            val calcs = pieceCalculationDao.getAllCalculations().first()
            if (calcs.isEmpty()) {
                val defaultCalcs = listOf(
                    PieceCalculationEntity(pano = "Listrada", kg = null, valorKg = null, quantidade = 1191),
                    PieceCalculationEntity(pano = "Summerplex", kg = 28.40, valorKg = 29.00, quantidade = 2133),
                    PieceCalculationEntity(pano = "Listrada", kg = null, valorKg = 31.00, quantidade = 1108),
                    PieceCalculationEntity(pano = "Summerplex", kg = 41.20, valorKg = 27.90, quantidade = 2880),
                    PieceCalculationEntity(pano = "listrada", kg = 13.60, valorKg = 31.00, quantidade = null),
                    PieceCalculationEntity(pano = "", kg = null, valorKg = null, quantidade = null)
                )
                for (calc in defaultCalcs) {
                    pieceCalculationDao.insertCalculation(calc)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TransactionRepository", "Error during seedMockDataIfEmpty", e)
        }
    }
}
