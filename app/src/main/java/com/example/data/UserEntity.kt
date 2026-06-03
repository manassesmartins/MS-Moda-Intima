package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // Dynamic UUID generated locally
    val email: String,
    val passwordHash: String, // SHA-256 hashed password
    val createdAt: Long = System.currentTimeMillis()
)
