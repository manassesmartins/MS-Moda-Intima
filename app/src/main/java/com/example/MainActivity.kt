package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.TransactionRepository
import com.example.ui.MsModaIntimaApp
import com.example.ui.TransactionViewModel
import com.example.ui.TransactionViewModelFactory
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Single point of initialization for offline database and repository structures
        val database = AppDatabase.getDatabase(this)
        val repository = TransactionRepository(
            database.transactionDao,
            database.categoryDao,
            database.orderDao,
            database.pieceCalculationDao,
            database.userDao,
            database.brandConfigDao
        )
        val sessionManager = com.example.data.SessionManager(this)
        val factory = TransactionViewModelFactory(repository, sessionManager, applicationContext)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        // Monitor connections and automatically trigger sync when internet becomes available
        try {
            val connectivityManager = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val networkRequest = android.net.NetworkRequest.Builder()
                .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(networkRequest, object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    if (viewModel.sessionManager.isLoggedIn) {
                        viewModel.triggerSyncSimulation()
                    }
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error registering network status callback: ${e.message}")
        }

        enableEdgeToEdge()
        setContent {
            val colorSchemeName by viewModel.colorSchemeName.collectAsState()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val fontSizeScale by viewModel.fontSizeScale.collectAsState()

            MyApplicationTheme(
                darkTheme = isDarkMode,
                colorSchemeName = colorSchemeName,
                fontSizeScale = fontSizeScale
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MsModaIntimaApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.checkForUpdatesSilently(this)
        }
    }
}
