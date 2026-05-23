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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Single point of initialization for offline database and repository structures
        val database = AppDatabase.getDatabase(this)
        val repository = TransactionRepository(
            database.transactionDao,
            database.categoryDao,
            database.orderDao,
            database.pieceCalculationDao,
            database.userDao
        )
        val sessionManager = com.example.data.SessionManager(this)
        val factory = TransactionViewModelFactory(repository, sessionManager)
        val viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MsModaIntimaApp(viewModel = viewModel)
                }
            }
        }
    }
}
