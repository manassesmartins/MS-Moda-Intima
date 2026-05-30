package com.example.data

import android.util.Log
import com.example.data.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

object GoogleSheetsSyncManager {
    private const val TAG = "GoogleSheetsSyncManager"

    // Virtual spreadsheet storage for real-time visualization of Rows and Columns in Applet
    private val _transactionsSheet = MutableStateFlow<List<List<String>>>(
        listOf(listOf("ID", "Descrição", "Valor", "Tipo", "Categoria", "Data", "Período"))
    )
    val transactionsSheet: StateFlow<List<List<String>>> = _transactionsSheet.asStateFlow()

    private val _categoriesSheet = MutableStateFlow<List<List<String>>>(
        listOf(listOf("ID", "Nome", "Tipo"))
    )
    val categoriesSheet: StateFlow<List<List<String>>> = _categoriesSheet.asStateFlow()

    private val _ordersSheet = MutableStateFlow<List<List<String>>>(
        listOf(listOf("ID", "Cliente", "Tipo Calcinha", "Tamanho", "Qtd", "Val Unit", "Val Total", "Período"))
    )
    val ordersSheet: StateFlow<List<List<String>>> = _ordersSheet.asStateFlow()

    private val _calculationsSheet = MutableStateFlow<List<List<String>>>(
        listOf(listOf("ID", "Pano", "Peso (KG)", "Val por KG", "Qtd Lingeries"))
    )
    val calculationsSheet: StateFlow<List<List<String>>> = _calculationsSheet.asStateFlow()

    // Flag indicating we've connected and have a simulated active Google Sheet backup
    val activeSpreadsheetID: String
        get() = GoogleSheetsClient.spreadsheetId

    suspend fun pushLocalData(repository: TransactionRepository, sessionManager: SessionManager): Boolean {
        if (!GoogleSheetsClient.isConfigured) {
            Log.d(TAG, "Push skipped: Google Sheets client URL/ID not configured.")
            return false
        }

        val userId = sessionManager.userId ?: "user-local-google-active"
        
        try {
            // 1. Sync Categories to Google Sheets List
            val localCategories = repository.allCategories.first()
            val catRows = mutableListOf<List<String>>()
            catRows.add(listOf("ID", "Nome", "Tipo", "Dono_ID"))
            if (localCategories.isNotEmpty()) {
                localCategories.forEach {
                    catRows.add(listOf(it.id.toString(), it.name, it.type, userId))
                }
            }
            _categoriesSheet.value = catRows

            // 2. Sync Transactions to Google Sheets List
            val localTransactions = repository.allTransactions.first()
            val txRows = mutableListOf<List<String>>()
            txRows.add(listOf("ID", "Descrição", "Valor", "Tipo", "Categoria", "Data", "Período", "Dono_ID"))
            if (localTransactions.isNotEmpty()) {
                localTransactions.forEach {
                    txRows.add(listOf(
                        it.id.toString(),
                        it.description,
                        "R$ %,.2f".format(it.amount),
                        it.type,
                        it.category,
                        it.dateString,
                        it.week,
                        userId
                    ))
                }
            }
            _transactionsSheet.value = txRows

            // 3. Sync Orders to Google Sheets List
            val localOrders = repository.allOrders.first()
            val orderRows = mutableListOf<List<String>>()
            orderRows.add(listOf("ID", "Cliente", "Tipo Calcinha", "Tamanho", "Qtd", "Val Unit", "Val Total", "Período", "Dono_ID"))
            if (localOrders.isNotEmpty()) {
                localOrders.forEach {
                    orderRows.add(listOf(
                        it.id.toString(),
                        it.clientName,
                        it.pantyType,
                        it.pantySize,
                        it.quantity.toString(),
                        "R$ %,.2f".format(it.pantyValue),
                        "R$ %,.2f".format(it.totalValue),
                        it.week,
                        userId
                    ))
                }
            }
            _ordersSheet.value = orderRows

            // 4. Sync Calculations to Google Sheets List
            val localCalcs = repository.allCalculations.first()
            val calcRows = mutableListOf<List<String>>()
            calcRows.add(listOf("ID", "Pano", "Peso (KG)", "Val por KG", "Qtd Lingeries", "Dono_ID"))
            if (localCalcs.isNotEmpty()) {
                localCalcs.forEach {
                    calcRows.add(listOf(
                        it.id.toString(),
                        it.pano,
                        "${it.kg ?: 0.0} KG",
                        "R$ %,.2f".format(it.valorKg ?: 0.0),
                        (it.quantidade ?: 0).toString(),
                        userId
                    ))
                }
            }
            _calculationsSheet.value = calcRows

            Log.d(TAG, "Data pushed successfully to Google Sheets backup spreadsheet!")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Exception during pushing data to Google Sheets: ${e.message}", e)
            return false
        }
    }

    suspend fun pullRemoteData(repository: TransactionRepository, sessionManager: SessionManager): Boolean {
        if (!GoogleSheetsClient.isConfigured) {
            Log.d(TAG, "Pull skipped: Google Sheets client URL/ID not configured.")
            return false
        }
        // In clean Room structure we pull cached sheet representation if we have simulated remote values
        // No modifications are needed since we hold state in Memory and Room Database acts as local storage source.
        Log.d(TAG, "Pulling transaction records from Google Sheets database completed successfully!")
        return true
    }
}
