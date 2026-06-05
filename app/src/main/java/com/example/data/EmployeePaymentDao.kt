package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeePaymentDao {
    @Query("SELECT * FROM employee_payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<EmployeePaymentEntity>>

    @Query("SELECT * FROM employee_payments WHERE employeeId = :employeeId ORDER BY timestamp DESC")
    fun getPaymentsByEmployee(employeeId: Long): Flow<List<EmployeePaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: EmployeePaymentEntity): Long

    @Query("DELETE FROM employee_payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)

    @Query("DELETE FROM employee_payments")
    suspend fun deleteAll()
}
