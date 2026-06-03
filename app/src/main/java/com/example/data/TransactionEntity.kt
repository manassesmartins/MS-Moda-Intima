package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amount: Double,
    val type: String, // "INFLOW" or "OUTFLOW"
    val category: String, // e.g., "Matéria-prima", "Mão de Obra", "Logística & Envios", "Marketing", "Insumos", "Venda"
    val dateString: String, // "12 OUT 2023", "Ontem", "Há 2h" etc.
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = true, // Simulation of cloud synchronization
    val extraText: String = "", // "Lote #204", "Fornecedor TexArt", "Cliente VIP Alpha" etc.
    val week: String = "1ª Semana" // "1ª Semana", "2ª Semana", "3ª Semana", "4ª Semana", "5ª Semana"
)
