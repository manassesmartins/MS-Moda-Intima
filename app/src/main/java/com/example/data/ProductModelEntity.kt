package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_models")
data class ProductModelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
