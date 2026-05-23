package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val pantyType: String,
    val pantySize: String,
    val quantity: Int,
    val pantyValue: Double,
    val totalValue: Double, // quantity * pantyValue
    val week: String = "1ª Semana", // "1ª Semana", "2ª Semana", "3ª Semana", "4ª Semana", "5ª Semana"
    val timestamp: Long = System.currentTimeMillis()
)
