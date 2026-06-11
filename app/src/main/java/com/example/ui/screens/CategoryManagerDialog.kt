package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun CategoryManagerDialog(
    viewModel: TransactionViewModel,
    onDismiss: () -> Unit
) {
    val Primary = MaterialTheme.colorScheme.primary
    val OnPrimary = MaterialTheme.colorScheme.onPrimary
    val Secondary = MaterialTheme.colorScheme.secondary
    val OnSecondary = MaterialTheme.colorScheme.onSecondary
    val Tertiary = MaterialTheme.colorScheme.tertiary
    val OnTertiary = MaterialTheme.colorScheme.onTertiary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val SurfaceContainer = MaterialTheme.colorScheme.surfaceVariant
    val SurfaceContainerHigh = MaterialTheme.colorScheme.surfaceVariant
    val SurfaceDark = MaterialTheme.colorScheme.background
    val ErrorColor = MaterialTheme.colorScheme.error

    val context = LocalContext.current
    val categories by viewModel.allCategories.collectAsStateWithLifecycle(emptyList())

    var categoryNameInput by remember { mutableStateOf("") }
    var editingCategoryId by remember { mutableStateOf<Long?>(null) }

    // Only display/manage OUTFLOW categories since transactions are only outflows
    val outflowCategories = remember(categories) { categories.filter { it.type == "OUTFLOW" } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Gerenciador de Categorias (Custos)", fontWeight = FontWeight.Bold, color = Primary)
        },
        containerColor = SurfaceContainerHigh,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                // Add / Edit form
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    val isEditing = editingCategoryId != null
                    Text(
                        text = if (isEditing) "EDITAR CATEGORIA" else "ADICIONAR CATEGORIA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    OutlinedTextField(
                        value = categoryNameInput,
                        onValueChange = { categoryNameInput = it },
                        placeholder = { Text("Ex: Forros de Malha", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isEditing) {
                            Button(
                                onClick = {
                                    editingCategoryId = null
                                    categoryNameInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar", color = OnSurface, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                if (categoryNameInput.isNotBlank()) {
                                    val catName = categoryNameInput.trim()
                                    val currentId = editingCategoryId
                                    if (currentId != null) {
                                        viewModel.updateCategory(currentId, catName, "OUTFLOW")
                                        Toast.makeText(context, "Categoria atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                                        editingCategoryId = null
                                    } else {
                                        viewModel.addCategory(catName, "OUTFLOW")
                                        Toast.makeText(context, "Categoria adicionada com sucesso!", Toast.LENGTH_SHORT).show()
                                    }
                                    categoryNameInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.weight(if (isEditing) 1.5f else 1f),
                            enabled = categoryNameInput.isNotBlank()
                        ) {
                            Text(
                                text = if (isEditing) "Salvar" else "Adicionar",
                                fontWeight = FontWeight.Bold,
                                color = OnPrimary
                            )
                        }
                    }
                }

                // List of existing added categories
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CATEGORIAS DE CUSTOS CADASTRADAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                    if (outflowCategories.isEmpty()) {
                        Text("Nenhuma categoria customizada ainda.", fontSize = 12.sp, color = OnSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(outflowCategories, key = { it.id }) { cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = ErrorColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(text = cat.name, fontSize = 14.sp, color = OnSurface)
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Edit Button
                                        IconButton(
                                            onClick = {
                                                editingCategoryId = cat.id
                                                categoryNameInput = cat.name
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar Categoria",
                                                tint = Tertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Delete Button
                                        IconButton(
                                            onClick = {
                                                if (editingCategoryId == cat.id) {
                                                    editingCategoryId = null
                                                    categoryNameInput = ""
                                                }
                                                viewModel.deleteCategory(cat.id)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Excluir Categoria",
                                                tint = ErrorColor.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = Primary)
            }
        }
    )
}

@Composable
fun CategoryBar(name: String, percentage: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, fontSize = 14.sp, color = OnSurface)
            Text(text = String.format(Locale.US, "%.0f%%", percentage), fontSize = 14.sp, color = Primary, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}
