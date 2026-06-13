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

fun normalizeCategory(rawCategory: String): String {
    val trimmed = rawCategory.trim()
    val lower = trimmed.lowercase()
    if (lower == "funcionários" || lower == "funcionário" || lower == "funcionarios" || lower == "funcionario" ||
        lower == "mão de obra" || lower == "mão-de-obra" || lower == "mao de obra" || lower == "mão de obra (funcionários)" ||
        lower == "funcionários / mão de obra" || lower == "funcionários e mão de obra" || lower == "mão de obra/funcionários" ||
        lower == "mão de obra/funcionarios" || lower == "funcionários & mão de obra"
    ) {
        return "Mão de Obra"
    }
    
    if (lower == "pano" || lower == "panos" || lower == "tecido" || lower == "tecidos" || lower == "matéria-prima" || lower == "matéria prima" || lower == "materia-prima" || lower == "materia prima") {
        return "Pano"
    }
    if (lower == "viés" || lower == "vies") {
        return "Viés"
    }
    if (lower == "linha" || lower == "linhas" || lower == "linhas & costura" || lower == "linhas e costura") {
        return "Linha"
    }
    if (lower == "forro" || lower == "forros") {
        return "Forro"
    }
    if (lower == "etiqueta de composição" || lower == "etiqueta composicao") {
        return "Etiqueta de composição"
    }
    if (lower == "etiqueta lateral" || lower == "etiqueta") {
        return "Etiqueta lateral"
    }
    if (lower == "manutenção" || lower == "manutencao") {
        return "Manutenção"
    }
    
    if (trimmed.isEmpty()) return "Variados"
    // Capitalize first letter of any other categories to look neat and normalized
    return trimmed.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
}
