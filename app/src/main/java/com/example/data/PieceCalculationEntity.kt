package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "piece_calculations")
data class PieceCalculationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pano: String = "",
    val kg: Double? = null,
    val valorKg: Double? = null,
    val quantidade: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)
