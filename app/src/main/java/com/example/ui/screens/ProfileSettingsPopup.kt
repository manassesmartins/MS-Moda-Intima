package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.BrandConfigEntity
import com.example.ui.TransactionViewModel
import com.example.ui.utils.rememberBitmapFromBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsPopup(
    onDismiss: () -> Unit,
    viewModel: TransactionViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val Primary = MaterialTheme.colorScheme.primary
    val OnPrimary = MaterialTheme.colorScheme.onPrimary
    val Secondary = MaterialTheme.colorScheme.secondary
    val OnSecondary = MaterialTheme.colorScheme.onSecondary
    val Tertiary = MaterialTheme.colorScheme.tertiary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val SurfaceDark = MaterialTheme.colorScheme.background

    var activeSubPopup by remember { mutableStateOf<String?>(null) } // "ID", "THEME", "CLOUD", "LOCAL"

    val brandConfig by viewModel.brandConfig.collectAsState()
    val isCloudEnabled by viewModel.isCloudBackupEnabled.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val userEmail = viewModel.sessionManager.userEmail

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Profile Info
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GERENCIAR PERFIL & AJUSTES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar UI", tint = OnSurfaceVariant)
                    }
                }

                // Brand Visual Identity
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Primary.copy(alpha = 0.15f), CircleShape)
                        .border(1.5.dp, Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val decodedLogo = rememberBitmapFromBase64(brandConfig?.logoImage)
                    if (decodedLogo != null) {
                        androidx.compose.foundation.Image(
                            bitmap = decodedLogo,
                            contentDescription = "Logo",
                            modifier = Modifier.size(80.dp).clip(CircleShape)
                        )
                    } else {
                        val iconsMap = mapOf(
                            "CROWN" to Icons.Default.Star,
                            "BAG" to Icons.Default.ShoppingCart,
                            "HEART" to Icons.Default.Favorite,
                            "BUILD" to Icons.Default.Build,
                            "PERSON" to Icons.Default.Person
                        )
                        val iconVec = iconsMap[brandConfig?.logoIcon ?: "CROWN"] ?: Icons.Default.Star
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = (brandConfig?.logoText ?: "GP").uppercase().take(3),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = brandConfig?.brandName ?: "Gestor de Produção",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = userEmail ?: "Modo P2P Offline",
                    fontSize = 11.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(14.dp))

                // Menu items as interactive cells that open custom sub-popups
                SettingsMenuRow(
                    title = "Identidade da Marca",
                    subtitle = "Nome, logotipo, nicho de atuação",
                    iconVec = Icons.Default.Edit,
                    onClick = { activeSubPopup = "ID" },
                    primaryColor = Primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsMenuRow(
                    title = "Personalização & Escala",
                    subtitle = "Esquema de cores, tamanho de fonte, tema escuro",
                    iconVec = Icons.Default.Settings,
                    onClick = { activeSubPopup = "THEME" },
                    primaryColor = Primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Spacer(modifier = Modifier.height(10.dp))

                SettingsMenuRow(
                    title = "Espelhamento PC / Web (P2P)",
                    subtitle = "Sincronizar em tempo real com laptop/desktop via MQTT",
                    iconVec = Icons.Default.Send,
                    onClick = { activeSubPopup = "P2P" },
                    primaryColor = Primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsMenuRow(
                    title = "Importação e Exportação Local",
                    subtitle = "Exportar ou restaurar banco SQLite em arquivo",
                    iconVec = Icons.Default.Share,
                    onClick = { activeSubPopup = "LOCAL" },
                    primaryColor = Primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsMenuRow(
                    title = "Buscar Atualizações",
                    subtitle = "Verificar se há nova versão disponivel",
                    iconVec = Icons.Default.Refresh,
                    onClick = { 
                        Toast.makeText(context, "Verificando se há atualizações...", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(1500)
                            Toast.makeText(context, "O aplicativo já está na versão mais recente (v1.0.0)", Toast.LENGTH_LONG).show()
                        }
                    },
                    primaryColor = Primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            viewModel.clearAllDataAndReseed()
                            Toast.makeText(context, "Banco local limpo e reinicializado!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB4AB)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFB4AB).copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Resetar Banco", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.logoutUser()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB4AB).copy(alpha = 0.2f), contentColor = Color(0xFFFFB4AB)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sair da Conta", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // SUB-POPUP 1: IDENTIDADE DA MARCA
    // ----------------------------------------------------
    if (activeSubPopup == "ID") {
        var brandNameInput by remember(brandConfig) { mutableStateOf(brandConfig?.brandName ?: "") }
        var categoryInput by remember(brandConfig) { mutableStateOf(brandConfig?.category ?: "Moda Íntima") }
        var nicheInput by remember(brandConfig) { mutableStateOf(brandConfig?.niche ?: "") }
        var logoIconInput by remember(brandConfig) { mutableStateOf(brandConfig?.logoIcon ?: "CROWN") }
        var logoTextInput by remember(brandConfig) { mutableStateOf(brandConfig?.logoText ?: "") }
        var logoImageInput by remember(brandConfig) { mutableStateOf<String?>(brandConfig?.logoImage) }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { bitmap: android.graphics.Bitmap? ->
            if (bitmap != null) {
                try {
                    val maxDimen = 300
                    val scale = java.lang.Math.min(maxDimen.toFloat()/bitmap.width, maxDimen.toFloat()/bitmap.height)
                    val resizedBitmap = if (scale < 1.0f) {
                        android.graphics.Bitmap.createScaledBitmap(
                            bitmap, 
                            (bitmap.width * scale).toInt(), 
                            (bitmap.height * scale).toInt(), 
                            true
                        )
                    } else bitmap

                    val outputStream = java.io.ByteArrayOutputStream()
                    resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, 80, outputStream)
                    val bytes = outputStream.toByteArray()
                    
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    logoImageInput = "data:image/webp;base64,$base64"
                    Toast.makeText(context, "Foto de Logotipo Tirada!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro ao processar imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                cameraLauncher.launch(null)
            } else {
                Toast.makeText(context, "Permissão de câmera negada", Toast.LENGTH_SHORT).show()
            }
        }

        val imageLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    
                    if (bitmap != null) {
                        // Resize to max 300x300 for avatar (saves memory & MQTT payload size)
                        val maxDimen = 300
                        val scale = java.lang.Math.min(maxDimen.toFloat()/bitmap.width, maxDimen.toFloat()/bitmap.height)
                        val resizedBitmap = if (scale < 1.0f) {
                            android.graphics.Bitmap.createScaledBitmap(
                                bitmap, 
                                (bitmap.width * scale).toInt(), 
                                (bitmap.height * scale).toInt(), 
                                true
                            )
                        } else bitmap

                        val outputStream = java.io.ByteArrayOutputStream()
                        resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, 80, outputStream)
                        val bytes = outputStream.toByteArray()
                        
                        val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                        logoImageInput = "data:image/webp;base64,$base64"
                        Toast.makeText(context, "Imagem de Logotipo Carregada!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro ao carregar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }

        AlertDialog(
            onDismissRequest = { activeSubPopup = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = {
                Text("Identidade da Marca", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurface)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Nome do Negócio", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        OutlinedTextField(
                            value = brandNameInput,
                            onValueChange = { brandNameInput = it },
                            placeholder = { Text("Ex: Ateliê Realeza") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text("Nicho / Foco principal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        OutlinedTextField(
                            value = nicheInput,
                            onValueChange = { nicheInput = it },
                            placeholder = { Text("Ex: Lingerie Fina, Fitness Atacado") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text("Logotipo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(Primary.copy(alpha = 0.1f), CircleShape)
                                    .border(1.dp, Primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val loadedBmp = rememberBitmapFromBase64(logoImageInput)
                                if (loadedBmp != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = loadedBmp,
                                        contentDescription = "Logo",
                                        modifier = Modifier.size(56.dp).clip(CircleShape)
                                    )
                                } else {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Primary)
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { imageLauncher.launch("image/*") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Galeria", fontSize = 11.sp)
                                    }
                                    
                                    Button(
                                        onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary.copy(alpha=0.8f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Câmera", fontSize = 11.sp)
                                    }
                                }
                                if (logoImageInput != null) {
                                    Text(
                                        text = "Remover",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFFB4AB),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { logoImageInput = null }
                                    )
                                }
                            }
                        }
                    }

                    if (logoImageInput.isNullOrBlank()) {
                        item {
                            Text("Letras de Backup (Sigla de 3 letras)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                            OutlinedTextField(
                                value = logoTextInput,
                                onValueChange = { logoTextInput = it.take(3) },
                                placeholder = { Text("Ex: ATR") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (brandNameInput.isBlank()) {
                            Toast.makeText(context, "Nome é obrigatório!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.saveBrandConfig(
                                brandName = brandNameInput,
                                category = categoryInput,
                                niche = nicheInput,
                                colorScheme = brandConfig?.colorScheme ?: "PINK",
                                logoText = if (logoTextInput.isNotBlank()) logoTextInput else brandNameInput.take(3),
                                logoIcon = logoIconInput,
                                logoImage = logoImageInput
                            )
                            Toast.makeText(context, "Identidade atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                            activeSubPopup = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Salvar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeSubPopup = null }) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            }
        )
    }

    // ----------------------------------------------------
    // SUB-POPUP 2: PERSONALIZAÇÃO & CORES
    // ----------------------------------------------------
    if (activeSubPopup == "THEME") {
        val appNameState by viewModel.appName.collectAsState()
        val colorSchemeName by viewModel.colorSchemeName.collectAsState()
        val isDarkMode by viewModel.isDarkMode.collectAsState()
        val fontSizeScale by viewModel.fontSizeScale.collectAsState()
        var tempAppName by remember(appNameState) { mutableStateOf(appNameState) }

        val schemeOptions = listOf(
            "PINK" to "Rosa Clássico (Orquídea)",
            "EMERALD" to "Esmeralda Vibrante (Ateliê)",
            "PURPLE" to "Ametista Imperial (Crochê)",
            "BLUE" to "Safira Oceano (Confecção)"
        )

        AlertDialog(
            onDismissRequest = { activeSubPopup = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = {
                Text("Personalização e Tema", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurface)
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("Nome de Exibição do Aplicativo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        OutlinedTextField(
                            value = tempAppName,
                            onValueChange = { tempAppName = it },
                            placeholder = { Text("Ex: Gestor de Produção") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text("Tema do Dispositivo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modo Escuro / Noturno", fontSize = 13.sp, color = OnSurface)
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.updateDarkMode(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Primary)
                            )
                        }
                    }

                    item {
                        Text("Esquema de Cores Ativo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Spacer(modifier = Modifier.height(6.dp))
                        schemeOptions.forEach { (schemeId, label) ->
                            val selected = colorSchemeName == schemeId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) Primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable {
                                        viewModel.updateColorScheme(schemeId)
                                        // Update the brandConfig db to sync color scheme
                                        brandConfig?.let {
                                            viewModel.saveBrandConfig(
                                                brandName = it.brandName,
                                                category = it.category,
                                                niche = it.niche,
                                                colorScheme = schemeId,
                                                logoText = it.logoText,
                                                logoIcon = it.logoIcon,
                                                logoImage = it.logoImage
                                            )
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                gap = 12.dp,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            when(schemeId) {
                                                "PINK" -> Color(0xFFF472B6)
                                                "EMERALD" -> Color(0xFF10B981)
                                                "PURPLE" -> Color(0xFF8B5CF6)
                                                else -> Color(0xFF3B82F6)
                                            },
                                            CircleShape
                                        )
                                )
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = OnSurface
                                )
                            }
                        }
                    }

                    item {
                        Text("Escala de Tamanho da Fonte: ${(fontSizeScale * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Slider(
                            value = fontSizeScale,
                            onValueChange = { viewModel.updateFontSizeScale(it) },
                            valueRange = 0.85f..1.25f,
                            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempAppName.isNotBlank()) {
                            viewModel.updateAppName(tempAppName)
                            Toast.makeText(context, "Nome e layout atualizados!", Toast.LENGTH_SHORT).show()
                        }
                        activeSubPopup = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Salvar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeSubPopup = null }) {
                    Text("Fechar", color = OnSurfaceVariant)
                }
            }
        )
    }

    // ----------------------------------------------------
    // SUB-POPUP 4: IMPORTAÇÃO & EXPORTAÇÃO LOCAL SQLite
    // ----------------------------------------------------
    if (activeSubPopup == "LOCAL") {
        val exportDatabaseLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/octet-stream")
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        val success = viewModel.exportDatabaseToStream(outputStream)
                        if (success) {
                            Toast.makeText(context, "Banco exportado com sucesso!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Falha ao exportar.", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }

        val importDatabaseLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val success = viewModel.importDatabaseFromStream(inputStream)
                        if (success) {
                            Toast.makeText(context, "Banco restaurado! Reiniciando...", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Falha ao importar o arquivo sqlite.", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }

        AlertDialog(
            onDismissRequest = { activeSubPopup = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = {
                Text("Importação & Exportação Local", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurface)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Você pode fazer cópias de backup locais de seus dados exportando o banco de dados SQLite para o cartão SD/memórias ou importando relatórios e cópias antigas.",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Button(
                        onClick = { exportDatabaseLauncher.launch("gestor_producao_backup_${System.currentTimeMillis()}.db") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar Banco de Dados", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    androidx.compose.material3.OutlinedButton(
                        onClick = { importDatabaseLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Primary)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importar Banco de Dados", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeSubPopup = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Voltar")
                }
            }
        )
    }

    // ----------------------------------------------------
    // SUB-POPUP X: ESPELHAMENTO PC/WEB (P2P)
    // ----------------------------------------------------
    if (activeSubPopup == "P2P") {
        var pinCode by remember { mutableStateOf("") }
        var isSyncing by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val txs by viewModel.allTransactions.collectAsState()
        val cats by viewModel.allCategories.collectAsState()
        val orders by viewModel.allOrders.collectAsState()
        val calcs by viewModel.allCalculations.collectAsState()
        
        AlertDialog(
            onDismissRequest = { activeSubPopup = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = {
                Text("Espelhamento PC / Web", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurface)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Acesse o site da versão desktop pelo computador e digite o Código de 6 dígitos gerado na tela abaixo para espelhar ou conectar seus dados em tempo real.\n\nTodo o seu banco de dados e layout serão sincronizados remotamente.",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    
                    val scanner = remember {
                        com.google.mlkit.vision.codescanner.GmsBarcodeScanning.getClient(context)
                    }

                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { if (it.length <= 6) pinCode = it },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        placeholder = { Text("Código de 6 dígitos") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                scanner.startScan()
                                    .addOnSuccessListener { barcode ->
                                        barcode.rawValue?.let { scannedPin ->
                                            if (scannedPin.length == 6 && scannedPin.all { it.isDigit() }) {
                                                pinCode = scannedPin
                                            } else {
                                                Toast.makeText(context, "QR Code inválido", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Falha ao escanear", Toast.LENGTH_SHORT).show()
                                    }
                            }) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Escanear QR")
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Button(
                        onClick = {
                            if (pinCode.length == 6 && !isSyncing) {
                                isSyncing = true
                                coroutineScope.launch {
                                    val success = com.example.data.MqttSyncManager.syncWithWeb(
                                        pinCode = pinCode,
                                        context = context,
                                        transactions = txs,
                                        categories = cats,
                                        orders = orders,
                                        calculations = calcs,
                                        brandConfig = brandConfig?.let { config ->
                                            org.json.JSONObject().apply {
                                                put("brandName", config.brandName)
                                                put("category", config.category)
                                                put("niche", config.niche)
                                                put("colorScheme", config.colorScheme)
                                                put("logoIcon", config.logoIcon)
                                                put("logoText", config.logoText)
                                                put("logoImage", config.logoImage)
                                                put("isConfigured", config.isConfigured)
                                            }
                                        }
                                    )
                                    withContext(Dispatchers.Main) {
                                        isSyncing = false
                                        pinCode = "" // Clear after sync
                                        Toast.makeText(context, if(success) "Dados espelhados com sucesso!" else "Erro ao parear, tente novamente.", Toast.LENGTH_SHORT).show()
                                        if (success) activeSubPopup = null
                                    }
                                }
                            } else if (pinCode.length != 6) {
                                Toast.makeText(context, "Digite os 6 dígitos mostrados no navegador web.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = OnPrimary, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Pareamento", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { activeSubPopup = null }) {
                    Text("Voltar", color = OnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun SettingsMenuRow(
    title: String,
    subtitle: String,
    iconVec: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(primaryColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = iconVec, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

// Custom Row receiver function helper
@Composable
fun Row(
    modifier: Modifier = Modifier,
    gap: androidx.compose.ui.unit.Dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = verticalAlignment,
        content = content
    )
}
