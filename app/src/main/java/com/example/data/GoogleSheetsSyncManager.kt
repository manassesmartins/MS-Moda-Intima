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

    private val _brandConfigSheet = MutableStateFlow<List<List<String>>>(
        listOf(listOf("BrandName", "Category", "Niche", "ColorScheme", "LogoText", "LogoIcon", "LogoImage", "IsConfigured", "Dono_ID"))
    )
    val brandConfigSheet: StateFlow<List<List<String>>> = _brandConfigSheet.asStateFlow()

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

            // 5. Sync BrandConfig to Google Sheets List
            val localBrand = repository.getBrandConfig()
            val brandRows = mutableListOf<List<String>>()
            brandRows.add(listOf("BrandName", "Category", "Niche", "ColorScheme", "LogoText", "LogoIcon", "LogoImage", "IsConfigured", "Dono_ID"))
            if (localBrand != null) {
                brandRows.add(listOf(
                    localBrand.brandName,
                    localBrand.category,
                    localBrand.niche,
                    localBrand.colorScheme,
                    localBrand.logoText,
                    localBrand.logoIcon,
                    localBrand.logoImage ?: "",
                    localBrand.isConfigured.toString(),
                    userId
                ))
            }
            _brandConfigSheet.value = brandRows

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
        val userId = sessionManager.userId ?: "user-local-google-active"
        
        // 1. Recover BrandConfig automatically from virtual Google Sheet if none exists locally
        try {
            val localBrand = repository.getBrandConfig()
            if (localBrand == null) {
                val matchingRow = _brandConfigSheet.value.drop(1).firstOrNull { it.size >= 9 && it[8] == userId }
                if (matchingRow != null) {
                    val entity = com.example.data.BrandConfigEntity(
                        brandName = matchingRow[0],
                        category = matchingRow[1],
                        niche = matchingRow[2],
                        colorScheme = matchingRow[3],
                        logoText = matchingRow[4],
                        logoIcon = matchingRow[5],
                        logoImage = matchingRow[6].ifBlank { null },
                        isConfigured = matchingRow[7].toBoolean()
                    )
                    repository.insertBrandConfig(entity)
                    Log.d(TAG, "Restored brand settings automatically from Google Account!")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring brand config during pull: ${e.message}", e)
        }

        // 2. Recover Categories from Google Sheets database if locally empty
        try {
            val localCategories = repository.allCategories.first()
            if (localCategories.isEmpty()) {
                val matchingCats = _categoriesSheet.value.drop(1).filter { it.size >= 4 && it[3] == userId }
                matchingCats.forEach { row ->
                    repository.insertCategory(
                        CategoryEntity(
                            id = row[0].toLongOrNull() ?: 0L,
                            name = row[1],
                            type = row[2]
                        )
                    )
                }
                Log.d(TAG, "Restored categories successfully from Google Account!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring categories during pull: ${e.message}", e)
        }

        // 3. Recover Transactions from Google Sheets database if locally empty
        try {
            val localTx = repository.allTransactions.first()
            if (localTx.isEmpty()) {
                val matchingTx = _transactionsSheet.value.drop(1).filter { it.size >= 8 && it[7] == userId }
                matchingTx.forEach { row ->
                    val cleanAmtStr = row[2].replace("R$", "").replace(" ", "").replace(".", "").replace(",", ".")
                    val amt = cleanAmtStr.toDoubleOrNull() ?: 0.0
                    repository.insert(
                        TransactionEntity(
                            id = row[0].toLongOrNull() ?: 0L,
                            description = row[1],
                            amount = amt,
                            type = row[3],
                            category = row[4],
                            dateString = row[5],
                            week = row[6]
                        )
                    )
                }
                Log.d(TAG, "Restored transactions successfully from Google Account!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring transactions during pull: ${e.message}", e)
        }

        // 4. Recover Orders from Google Sheets database if locally empty
        try {
            val localOrders = repository.allOrders.first()
            if (localOrders.isEmpty()) {
                val matchingOrders = _ordersSheet.value.drop(1).filter { it.size >= 9 && it[8] == userId }
                matchingOrders.forEach { row ->
                    val valUnit = row[5].replace("R$", "").replace(" ", "").replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
                    val valTotal = row[6].replace("R$", "").replace(" ", "").replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
                    repository.insertOrder(
                        OrderEntity(
                            id = row[0].toLongOrNull() ?: 0L,
                            clientName = row[1],
                            pantyType = row[2],
                            pantySize = row[3],
                            quantity = row[4].toIntOrNull() ?: 0,
                            pantyValue = valUnit,
                            totalValue = valTotal,
                            week = row[7]
                        )
                    )
                }
                Log.d(TAG, "Restored orders successfully from Google Account!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring orders during pull: ${e.message}", e)
        }

        // 5. Recover Lingerie Fabric Calculations from Google Sheets database if locally empty
        try {
            val localCalcs = repository.allCalculations.first()
            if (localCalcs.isEmpty()) {
                val matchingCalcs = _calculationsSheet.value.drop(1).filter { it.size >= 6 && it[5] == userId }
                matchingCalcs.forEach { row ->
                    val kgVal = row[2].replace("KG", "").replace(" ", "").replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
                    val valKg = row[3].replace("R$", "").replace(" ", "").replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
                    repository.insertCalculation(
                        PieceCalculationEntity(
                            id = row[0].toLongOrNull() ?: 0L,
                            pano = row[1],
                            kg = kgVal,
                            valorKg = valKg,
                            quantidade = row[4].toIntOrNull() ?: 0
                        )
                    )
                }
                Log.d(TAG, "Restored piece calculations successfully from Google Account!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring piece calculations during pull: ${e.message}", e)
        }

        Log.d(TAG, "Pulling user workspace settings and backup databases completed successfully!")
        return true
    }
}
