package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.TransactionRepository
import com.example.ui.MsModaIntimaApp
import com.example.ui.TransactionViewModel
import com.example.ui.TransactionViewModelFactory
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testAppLaunchAndRender() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // Create an in-memory database for testing
    val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
      
    val repository = TransactionRepository(
      database.transactionDao,
      database.categoryDao,
      database.orderDao,
      database.pieceCalculationDao,
      database.userDao,
      database.brandConfigDao
    )
    
    val sessionManager = com.example.data.SessionManager(context)
    val viewModel = TransactionViewModel(repository, sessionManager)
    
    composeTestRule.setContent {
      MsModaIntimaApp(viewModel = viewModel)
    }
    
    // Allow compostion and pending coroutines to complete
    composeTestRule.waitForIdle()
    
    assertNotNull(viewModel)
    database.close()
  }
}

