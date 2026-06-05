package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employee_payments")
data class EmployeePaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val amount: Double,
    val week: String,
    val paymentDate: String,
    val status: String = "Pendente",
    val timestamp: Long = System.currentTimeMillis(),
    val transactionId: Long? = null
)
