package com.example.data

import android.util.Log
import com.example.data.api.*
import kotlinx.coroutines.flow.first

object SupabaseSyncManager {
    private const val TAG = "SupabaseSyncManager"

    suspend fun pushLocalData(repository: TransactionRepository, sessionManager: SessionManager): Boolean {
        if (!SupabaseClient.isConfigured) {
            Log.d(TAG, "Push skipped: Supabase not configured.")
            return false
        }

        val userId = sessionManager.userId ?: return false
        val authToken = sessionManager.authToken ?: return false
        val authHeader = "Bearer $authToken"
        val apiKey = SupabaseClient.supabaseAnonKey
        val api = SupabaseClient.dataApi ?: return false

        try {
            // 1. Sync Categories
            val localCategories = repository.allCategories.first()
            if (localCategories.isNotEmpty()) {
                val remoteCats = localCategories.map {
                    RemoteCategory(
                        id = if (it.id == 0L) null else it.id,
                        name = it.name,
                        type = it.type,
                        user_id = userId
                    )
                }
                val response = api.upsertCategories(apiKey, authHeader, list = remoteCats)
                if (!response.isSuccessful) {
                    Log.e(TAG, "Error pushing categories: ${response.errorBody()?.string()}")
                    return false
                }
            }

            // 2. Sync Transactions
            val localTransactions = repository.allTransactions.first()
            if (localTransactions.isNotEmpty()) {
                val remoteTxs = localTransactions.map {
                    RemoteTransaction(
                        id = if (it.id == 0L) null else it.id,
                        description = it.description,
                        amount = it.amount,
                        type = it.type,
                        category = it.category,
                        dateString = it.dateString,
                        timestamp = it.timestamp,
                        extraText = it.extraText,
                        week = it.week,
                        user_id = userId
                    )
                }
                val response = api.upsertTransactions(apiKey, authHeader, list = remoteTxs)
                if (!response.isSuccessful) {
                    Log.e(TAG, "Error pushing transactions: ${response.errorBody()?.string()}")
                    return false
                }
            }

            // 3. Sync Orders
            val localOrders = repository.allOrders.first()
            if (localOrders.isNotEmpty()) {
                val remoteOrders = localOrders.map {
                    RemoteOrder(
                        id = if (it.id == 0L) null else it.id,
                        clientName = it.clientName,
                        pantyType = it.pantyType,
                        pantySize = it.pantySize,
                        quantity = it.quantity,
                        pantyValue = it.pantyValue,
                        totalValue = it.totalValue,
                        week = it.week,
                        user_id = userId
                    )
                }
                val response = api.upsertOrders(apiKey, authHeader, list = remoteOrders)
                if (!response.isSuccessful) {
                    Log.e(TAG, "Error pushing orders: ${response.errorBody()?.string()}")
                    return false
                }
            }

            // 4. Sync Calculations
            val localCalcs = repository.allCalculations.first()
            if (localCalcs.isNotEmpty()) {
                val remoteCalcs = localCalcs.map {
                    RemotePieceCalculation(
                        id = if (it.id == 0L) null else it.id,
                        pano = it.pano,
                        kg = it.kg,
                        valorKg = it.valorKg,
                        quantidade = it.quantidade,
                        user_id = userId
                    )
                }
                val response = api.upsertCalculations(apiKey, authHeader, list = remoteCalcs)
                if (!response.isSuccessful) {
                    Log.e(TAG, "Error pushing calculations: ${response.errorBody()?.string()}")
                    return false
                }
            }

            Log.d(TAG, "Push local data to Supabase finished successfully!")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Exception during pushing data: ${e.message}", e)
            return false
        }
    }

    suspend fun pullRemoteData(repository: TransactionRepository, sessionManager: SessionManager): Boolean {
        if (!SupabaseClient.isConfigured) {
            Log.d(TAG, "Pull skipped: Supabase not configured.")
            return false
        }

        val authToken = sessionManager.authToken ?: return false
        val authHeader = "Bearer $authToken"
        val apiKey = SupabaseClient.supabaseAnonKey
        val api = SupabaseClient.dataApi ?: return false

        try {
            // 1. Pull Categories
            val catRes = api.getCategories(apiKey, authHeader)
            val remoteCats = if (catRes.isSuccessful) catRes.body() else null

            // 2. Pull Transactions
            val txRes = api.getTransactions(apiKey, authHeader)
            val remoteTxs = if (txRes.isSuccessful) txRes.body() else null

            // 3. Pull Orders
            val orderRes = api.getOrders(apiKey, authHeader)
            val remoteOrders = if (orderRes.isSuccessful) orderRes.body() else null

            // 4. Pull Calculations
            val calcRes = api.getCalculations(apiKey, authHeader)
            val remoteCalcs = if (calcRes.isSuccessful) calcRes.body() else null

            if (remoteCats == null || remoteTxs == null || remoteOrders == null || remoteCalcs == null) {
                Log.e(TAG, "Failed pulling from Supabase endpoints. Response bodies were null or unsuccessful.")
                return false
            }

            // Clean local database to prevent stale duplicates and apply pulled records
            repository.clearAll()

            // Insert Categories
            for (c in remoteCats) {
                repository.insertCategory(
                    CategoryEntity(
                        id = c.id ?: 0L,
                        name = c.name,
                        type = c.type
                    )
                )
            }

            // Insert Transactions
            for (t in remoteTxs) {
                repository.insert(
                    TransactionEntity(
                        id = t.id ?: 0L,
                        description = t.description,
                        amount = t.amount,
                        type = t.type,
                        category = t.category,
                        dateString = t.dateString,
                        timestamp = t.timestamp,
                        extraText = t.extraText,
                        week = t.week
                    )
                )
            }

            // Insert Orders
            for (o in remoteOrders) {
                repository.insertOrder(
                    OrderEntity(
                        id = o.id ?: 0L,
                        clientName = o.clientName,
                        pantyType = o.pantyType,
                        pantySize = o.pantySize,
                        quantity = o.quantity,
                        pantyValue = o.pantyValue,
                        totalValue = o.totalValue,
                        week = o.week
                    )
                )
            }

            // Insert Calculations
            for (cl in remoteCalcs) {
                repository.insertCalculation(
                    PieceCalculationEntity(
                        id = cl.id ?: 0L,
                        pano = cl.pano,
                        kg = cl.kg,
                        valorKg = cl.valorKg,
                        quantidade = cl.quantidade
                    )
                )
            }

            Log.d(TAG, "Bi-directional pulled database matches Supabase completely.")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Exception during pulling data: ${e.message}", e)
            return false
        }
    }
}
