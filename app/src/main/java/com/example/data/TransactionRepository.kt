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
        } catch (e: Exception) {
            android.util.Log.e("TransactionRepository", "Error during seedMockDataIfEmpty", e)
        }
    }
}
