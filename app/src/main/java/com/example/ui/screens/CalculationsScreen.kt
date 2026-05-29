package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationsScreen(viewModel: TransactionViewModel) {
    val context = LocalContext.current
    val calculations by viewModel.allCalculations.collectAsStateWithLifecycle(emptyList())

    var activeSubTab by remember { mutableStateOf("PECAS") } // "PECAS" or "CALC"

    // Dialog state for editing calculation row
    var calculationToEdit by remember { mutableStateOf<com.example.data.PieceCalculationEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Custo de Peças",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Text(
            text = "Calcule o rendimento de peças por quilo de tecido",
            fontSize = 13.sp,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        // Tab switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("PECAS" to "Peças por KG", "CALC" to "Calculadora")
            tabs.forEach { (subId, label) ->
                val isSel = activeSubTab == subId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) Primary else Color.Transparent)
                        .clickable { activeSubTab = subId }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSel) OnPrimary else OnSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (activeSubTab == "PECAS") {
            // CÁLCULO DE PEÇAS POR KG SCREEN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Corte & Rendimentos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.clearCalculationsAndReseed() },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Resetar Tabela", tint = OnSurfaceVariant)
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = OnPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nova Linha", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnPrimary)
                    }
                }
            }

            // Explicativo da Tela de Rendimentos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainer, RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Gerencie o rendimento de cortes e custos reais por lote/tecido. Os dados são salvos localmente e integrados.",
                        color = OnSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (calculations.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Nenhum cálculo",
                                tint = OnSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Nenhum cálculo cadastrado",
                                color = OnSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = { showAddDialog = true }) {
                                Text("Toque aqui para adicionar uma linha", color = Primary)
                            }
                        }
                    }
                } else {
                    items(calculations, key = { it.id }) { item ->
                        val pano = item.pano
                        val kg = item.kg
                        val valorKg = item.valorKg
                        val qty = item.quantidade

                        val hasKg = kg != null && kg > 0
                        val hasQty = qty != null && qty > 0

                        val quantKgString = if (hasKg && hasQty) {
                            String.format(Locale.US, "%.5f", qty!!.toDouble() / kg!!)
                        } else {
                            ""
                        }

                        val valorQuantString = if (hasQty) {
                            if (hasKg && valorKg != null) {
                                val cost = (kg * valorKg) / qty.toDouble()
                                String.format(Locale("pt", "BR"), "R$ %,.2f", cost)
                            } else if (pano.lowercase().contains("listrada") && valorKg != null) {
                                val cost = valorKg / 106.8
                                String.format(Locale("pt", "BR"), "R$ %,.2f", cost)
                            } else {
                                ""
                            }
                        } else {
                            ""
                        }

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { calculationToEdit = item }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Primary.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.List,
                                            contentDescription = null,
                                            tint = Primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = if (pano.isBlank()) "Pano sem nome" else pano,
                                            color = OnSurface,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "ID #${item.id} • Métricas de Produção",
                                            color = OnSurfaceVariant.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = Primary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Rendimento (QTD / KG)",
                                            color = OnSurfaceVariant,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (quantKgString.isEmpty()) "Div/0!" else "$quantKgString pç/kg",
                                            color = Tertiary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Custo Unitário (Valor/QTD)",
                                            color = OnSurfaceVariant,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (valorQuantString.isEmpty()) "Div/0!" else valorQuantString,
                                            color = Primary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DetailMetric(label = "PESO TOTAL", value = if (kg == null) "0,00 kg" else String.format(Locale("pt", "BR"), "%,.2f kg", kg))
                                DetailMetric(label = "VALOR DO KG", value = if (valorKg == null) "R$ 0,00" else String.format(Locale("pt", "BR"), "R$ %,.2f", valorKg))
                                DetailMetric(label = "PEÇAS CORTADAS", value = if (qty == null) "0 u." else "$qty u.")
                            }
                        }
                    }
                }
            }
        } else {
            // INTERACTIVE CALCULATOR SCREEN (ELECTRONIC LAB DESIGN)
            var displayVal by remember { mutableStateOf("0") }
            var activeOp by remember { mutableStateOf<Char?>(null) }
            var prevVal by remember { mutableStateOf<Double?>(null) }
            var opPressed by remember { mutableStateOf(false) }

            Text(
                text = "Calculadora Eletrônica",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E102E)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Display Window
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF11071F), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            if (prevVal != null && activeOp != null) {
                                Text(
                                    text = "${if (prevVal!! % 1 == 0.0) prevVal!!.toInt() else prevVal} $activeOp",
                                    fontSize = 13.sp,
                                    color = OnSurfaceVariant.copy(alpha = 0.5f),
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = displayVal,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE5A1FF),
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }

                    // Interactive Keys
                    val keys = listOf(
                        listOf("C", "⌫", "%", "/"),
                        listOf("7", "8", "9", "*"),
                        listOf("4", "5", "6", "-"),
                        listOf("1", "2", "3", "+"),
                        listOf("+/-", "0", ".", "")
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        keys.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { key ->
                                    val isOp = key in listOf("/", "*", "-", "+")
                                    val isClear = key == "C"
                                    val isDelete = key == "⌫"
                                    val isPercent = key == "%"
                                    val isSign = key == "+/-"
                                    val isEmpty = key == ""
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1.2f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                when {
                                                    isEmpty -> Color.Transparent
                                                     isClear || isDelete -> ErrorColor.copy(alpha = 0.15f)
                                                    isOp || isPercent || isSign -> Primary.copy(alpha = 0.15f)
                                                    else -> Color.White.copy(alpha = 0.05f)
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                when {
                                                    isEmpty -> Color.Transparent
                                                    isClear || isDelete -> ErrorColor.copy(alpha = 0.3f)
                                                    isOp || isPercent || isSign -> Primary.copy(alpha = 0.3f)
                                                    else -> Color.White.copy(alpha = 0.08f)
                                                },
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                if (isEmpty) return@clickable
                                                when {
                                                    key in "0".."9" || key == "." -> {
                                                        if (displayVal == "0" || opPressed || displayVal == "Erro") {
                                                            displayVal = if (key == ".") "0." else key
                                                            opPressed = false
                                                        } else {
                                                            if (key == "." && displayVal.contains(".")) {
                                                                // Ignore duplicate decimals
                                                            } else {
                                                                displayVal += key
                                                            }
                                                        }
                                                    }
                                                    isClear -> {
                                                        displayVal = "0"
                                                        prevVal = null
                                                        activeOp = null
                                                        opPressed = false
                                                    }
                                                    isDelete -> {
                                                        if (displayVal != "0" && displayVal.isNotEmpty() && displayVal != "Erro") {
                                                            displayVal = displayVal.dropLast(1)
                                                            if (displayVal.isEmpty() || displayVal == "-") {
                                                                displayVal = "0"
                                                            }
                                                        }
                                                    }
                                                    isPercent -> {
                                                        val currentVal = displayVal.toDoubleOrNull() ?: 0.0
                                                        if (prevVal != null && activeOp != null) {
                                                            if (activeOp == '+' || activeOp == '-') {
                                                                val percentageOfPrev = prevVal!! * (currentVal / 100.0)
                                                                displayVal = if (percentageOfPrev % 1 == 0.0) percentageOfPrev.toInt().toString() else String.format(Locale.US, "%.4f", percentageOfPrev)
                                                            } else {
                                                                val p = currentVal / 100.0
                                                                displayVal = if (p % 1 == 0.0) p.toInt().toString() else p.toString()
                                                            }
                                                        } else {
                                                            val p = currentVal / 100.0
                                                            displayVal = if (p % 1 == 0.0) p.toInt().toString() else p.toString()
                                                        }
                                                    }
                                                    isSign -> {
                                                        val currentVal = displayVal.toDoubleOrNull() ?: 0.0
                                                        val toggled = -currentVal
                                                        displayVal = if (displayVal != "0" && displayVal != "Erro") {
                                                            if (toggled % 1 == 0.0) toggled.toInt().toString() else toggled.toString()
                                                        } else {
                                                            "0"
                                                        }
                                                    }
                                                    isOp -> {
                                                        prevVal = displayVal.toDoubleOrNull()
                                                        activeOp = key.first()
                                                        opPressed = true
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!isEmpty) {
                                            Text(
                                                text = key,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    isClear || isDelete -> ErrorColor
                                                    isOp || isPercent || isSign -> Primary
                                                    else -> OnSurface
                                                },
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Wide bottom evaluate (=) key
                    Button(
                        onClick = {
                            if (prevVal != null && activeOp != null) {
                                val currentVal = displayVal.toDoubleOrNull() ?: 0.0
                                val result = when (activeOp) {
                                    '+' -> prevVal!! + currentVal
                                    '-' -> prevVal!! - currentVal
                                    '*' -> prevVal!! * currentVal
                                    '/' -> if (currentVal != 0.0) prevVal!! / currentVal else Double.NaN
                                    else -> currentVal
                                }
                                displayVal = if (result.isNaN()) {
                                    "Erro"
                                } else {
                                    if (result % 1 == 0.0) result.toInt().toString() else String.format(Locale.US, "%.4f", result)
                                }
                                prevVal = null
                                activeOp = null
                                opPressed = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(text = "=", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OnPrimary, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }

    // DIALOG: ADD NEW CALCULATION ROW
    if (showAddDialog) {
        var pano by remember { mutableStateOf("") }
        var kgText by remember { mutableStateOf("") }
        var valorKgText by remember { mutableStateOf("") }
        var qtyText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Nova Linha de Cálculo", color = Primary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = pano,
                        onValueChange = { pano = it },
                        label = { Text("Pano (Tipo/Descrição)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = kgText,
                        onValueChange = { kgText = it },
                        label = { Text("Quilos (KG)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = valorKgText,
                        onValueChange = { valorKgText = it },
                        label = { Text("Valor do KG (R$/KG)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Quantidade de Peças") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val kg = kgText.replace(',', '.').toDoubleOrNull()
                        val valorKg = valorKgText.replace(',', '.').toDoubleOrNull()
                        val qty = qtyText.toIntOrNull()
                        viewModel.addCalculation(pano, kg, valorKg, qty)
                        showAddDialog = false
                        Toast.makeText(context, "Linha adicionada!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Adicionar", color = OnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            }
        )
    }

    // DIALOG: EDIT CALCULATION ROW
    calculationToEdit?.let { entity ->
        var pano by remember { mutableStateOf(entity.pano) }
        var kgText by remember { mutableStateOf(entity.kg?.toString() ?: "") }
        var valorKgText by remember { mutableStateOf(entity.valorKg?.toString() ?: "") }
        var qtyText by remember { mutableStateOf(entity.quantidade?.toString() ?: "") }

        AlertDialog(
            onDismissRequest = { calculationToEdit = null },
            containerColor = SurfaceDark,
            title = { Text("Editar Linha de Cálculo", color = Primary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = pano,
                        onValueChange = { pano = it },
                        label = { Text("Pano (Tipo/Descrição)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = kgText,
                        onValueChange = { kgText = it },
                        label = { Text("Quilos (KG)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = valorKgText,
                        onValueChange = { valorKgText = it },
                        label = { Text("Valor do KG (R$/KG)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Quantidade de Peças") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.deleteCalculation(entity.id)
                            calculationToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                    ) {
                        Text("Excluir", color = Color.White)
                    }
                    Button(
                        onClick = {
                            val kg = kgText.replace(',', '.').toDoubleOrNull()
                            val valorKg = valorKgText.replace(',', '.').toDoubleOrNull()
                            val qty = qtyText.toIntOrNull()
                            viewModel.updateCalculation(entity.id, pano, kg, valorKg, qty)
                            calculationToEdit = null
                            Toast.makeText(context, "Alterações salvas!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Salvar", color = OnPrimary)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { calculationToEdit = null }) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun DetailMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = OnSurfaceVariant.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = OnSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
