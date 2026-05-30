package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brand_config")
data class BrandConfigEntity(
    @PrimaryKey val id: String = "config_singleton",
    val brandName: String,
    val category: String,
    val niche: String,
    val colorScheme: String,
    val logoText: String,
    val logoIcon: String,
    val isConfigured: Boolean = false
)
