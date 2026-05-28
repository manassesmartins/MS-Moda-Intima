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

            // 2. Seed Transactions if empty
            val transactions = transactionDao.getAllTransactions().first()
            if (transactions.isEmpty()) {
                val defaultTransactions = listOf(
                    TransactionEntity(description = "Seda e Aviamentos Lote #29", amount = 145.80, type = "OUTFLOW", category = "Linha", dateString = "12 MAI 2026", week = "1ª Semana"),
                    TransactionEntity(description = "Algodão Pima 100% (Lote #40)", amount = 850.00, type = "OUTFLOW", category = "Pano", dateString = "14 MAI 2026", week = "1ª Semana"),
                    TransactionEntity(description = "Mão de Obra Oficina Costura Maria", amount = 1200.00, type = "OUTFLOW", category = "Funcionários", dateString = "18 MAI 2026", week = "2ª Semana"),
                    TransactionEntity(description = "Mil Etiquetas de Cetim Laterais", amount = 95.00, type = "OUTFLOW", category = "Etiqueta lateral", dateString = "20 MAI 2026", week = "2ª Semana"),
                    TransactionEntity(description = "Manutenção Preventiva Overlocks", amount = 220.00, type = "OUTFLOW", category = "Manutenção", dateString = "24 MAI 2026", week = "3ª Semana"),
                    TransactionEntity(description = "Forro de Calcinha Algodão Cru", amount = 310.00, type = "OUTFLOW", category = "Forro", dateString = "26 MAI 2026", week = "4ª Semana")
                )
                for (t in defaultTransactions) {
                    transactionDao.insertTransaction(t)
                }
            }

            // 3. Seed Orders if empty
            val orders = orderDao.getAllOrders().first()
            if (orders.isEmpty()) {
                val defaultOrders = listOf(
                    OrderEntity(clientName = "Lingerie Chic Atacado", pantyType = "Calcinha Renda Macia", pantySize = "M", quantity = 300, pantyValue = 3.50, totalValue = 1050.00, week = "1ª Semana"),
                    OrderEntity(clientName = "Boutique Bella Flor", pantyType = "Fio Dental Microfibra", pantySize = "G", quantity = 200, pantyValue = 4.00, totalValue = 800.00, week = "2ª Semana"),
                    OrderEntity(clientName = "Distribuidora Moda & Cia", pantyType = "Calcinha Algodão Confort", pantySize = "P", quantity = 500, pantyValue = 3.20, totalValue = 1600.00, week = "3ª Semana"),
                    OrderEntity(clientName = "Atacado Feminino VIP", pantyType = "Caleçon Sublime Renda", pantySize = "M", quantity = 150, pantyValue = 5.50, totalValue = 825.00, week = "4ª Semana")
                )
                for (o in defaultOrders) {
                    orderDao.insertOrder(o)
                }
            }

            // 4. Seed Piece Calculations if empty
            val calcs = pieceCalculationDao.getAllCalculations().first()
            if (calcs.isEmpty()) {
                val defaultCalcs = listOf(
                    PieceCalculationEntity(pano = "Algodão Estampado 100%", kg = 12.5, valorKg = 45.0, quantidade = 350),
                    PieceCalculationEntity(pano = "Microfibra Poliamida Premium", kg = 8.2, valorKg = 65.0, quantidade = 280),
                    PieceCalculationEntity(pano = "Renda Jacquard Extramacia", kg = 4.5, valorKg = 120.0, quantidade = 150)
                )
                for (e in defaultCalcs) {
                    pieceCalculationDao.insertCalculation(e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TransactionRepository", "Error during seedMockDataIfEmpty", e)
        }
    }
}
