package com.example.ui

import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import com.example.data.*
import com.example.ui.theme.*

private data class ColorSchemeUiItem(
    val id: String,
    val name: String,
    val darkColor: Color,
    val lightColor: Color
)

@Composable
fun SettingsScreen(
    isCloudEnabled: Boolean,
    onCloudEnabledChange: (Boolean) -> Unit,
    syncState: String,
    onForceSync: () -> Unit,
    onClearAll: () -> Unit,
    userEmail: String?,
    onLogout: () -> Unit,
    viewModel: TransactionViewModel
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
    val updater = remember { viewModel.getUpdater(context) }
    var ownerInput by remember { mutableStateOf(updater.owner) }
    var repoInput by remember { mutableStateOf(updater.repo) }
    var branchInput by remember { mutableStateOf(updater.branch) }
    var apkPathInput by remember { mutableStateOf(updater.apkPath) }
    var versionJsonPathInput by remember { mutableStateOf(updater.versionJsonPath) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Main Screen Title matching tab bar
        item {
            Column {
                Text(
                    text = "Ajustes",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = "Gerencie as preferências e personalização do seu aplicativo",
                    fontSize = 13.sp,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Google Account Profile Card
        item {
            val userNameStr = viewModel.sessionManager.userName.ifBlank { "Usuário Ativo" }
            val avatarUrlStr = viewModel.sessionManager.userAvatar
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (avatarUrlStr.isNotBlank()) {
                        AsyncImage(
                            model = avatarUrlStr,
                            contentDescription = "Foto do perfil Google",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Primary, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Primary.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, Primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userNameStr,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = userEmail ?: "Sem e-mail conectado",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Primary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CONECTADO VIA GOOGLE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sair da Conta",
                            tint = ErrorColor
                        )
                    }
                }
            }
        }

        // PERSONALIZAÇÃO & ACESSIBILIDADE Card
        item {
            val appNameState by viewModel.appName.collectAsState()
            val colorSchemeName by viewModel.colorSchemeName.collectAsState()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val fontSizeScale by viewModel.fontSizeScale.collectAsState()
            var tempAppName by remember(appNameState) { mutableStateOf(appNameState) }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PERSONALIZAÇÃO & ACESSIBILIDADE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // App Name Customization Text Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Nome do Aplicativo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tempAppName,
                            onValueChange = { tempAppName = it },
                            placeholder = { Text("Ex: Gerenciamento de Confecção") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface
                            )
                        )
                        Button(
                            onClick = {
                                if (tempAppName.isNotBlank()) {
                                    viewModel.updateAppName(tempAppName)
                                    Toast.makeText(context, "Nome do app alterado!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = OnPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
                        ) {
                            Text("Salvar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                // Theme Mode Option (Light vs Dark Theme)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo de Visualização",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        Text(
                            text = if (isDarkMode) "Tema Escuro Elegante" else "Tema Claro Alto Contraste",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.updateDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnPrimary,
                            checkedTrackColor = Primary,
                            uncheckedThumbColor = OnSurfaceVariant,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                // Font Size Settings (Acessibilidade)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tamanho de Letra (Acessibilidade)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )
                    Text(
                        text = "Ajuste o tamanho do texto para facilitar a leitura caso tenha dificuldades visuais.",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val sizes = listOf(
                            Triple(0.85f, "Pequeno", "A-"),
                            Triple(1.0f, "Normal", "A"),
                            Triple(1.20f, "Grande", "A+"),
                            Triple(1.40f, "Extra G", "A++")
                        )
                        sizes.forEach { (scale, label, symbol) ->
                            val isSelected = Math.abs(fontSizeScale - scale) < 0.05f
                            Button(
                                onClick = { viewModel.updateFontSizeScale(scale) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Primary else Color.White.copy(alpha = 0.05f),
                                    contentColor = if (isSelected) OnPrimary else OnSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(symbol, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(label, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                // Layout Colors / Theme Presets Option
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paleta de Cores do Layout",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )

                    val colorSchemes = listOf(
                        ColorSchemeUiItem("PINK", "Orquídea", Color(0xFFF472B6), Color(0xFFDB2777)),
                        ColorSchemeUiItem("BLUE", "Safira", Color(0xFF60A5FA), Color(0xFF2563EB)),
                        ColorSchemeUiItem("GREEN", "Esmeralda", Color(0xFF34D399), Color(0xFF059669)),
                        ColorSchemeUiItem("ROSE", "Ouro Rosé", Color(0xFFF6A6B2), Color(0xFFD8577A)),
                        ColorSchemeUiItem("RED", "Rubi", Color(0xFFF87171), Color(0xFFDC2626))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        colorSchemes.forEach { item ->
                            val isSelected = colorSchemeName.uppercase() == item.id
                            val displayCol = if (isDarkMode) item.darkColor else item.lightColor
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Primary else Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.updateColorScheme(item.id) }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(displayCol, CircleShape)
                                )
                            }
                        }
                    }
                    val currentLabel = colorSchemes.firstOrNull { it.id == colorSchemeName.uppercase() }?.name ?: "Padrão"
                    Text(
                        text = "Tema selecionado: $currentLabel",
                        fontSize = 11.sp,
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // VERSION & INTEGRITY SECTION
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ATUALIZAÇÃO DO APLICATIVO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Verifique se há novas atualizações, melhorias de segurança ou patches de otimização disponíveis para o seu aplicativo.",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Versão Atual do App:",
                            fontSize = 11.sp,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = updater.getLocalVersion(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                Toast.makeText(context, "Buscando melhorias na nuvem...", Toast.LENGTH_SHORT).show()
                                updater.checkForUpdates(forceNotify = true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Buscar Atualização", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                var showAdvanced by remember { mutableStateOf(false) }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvanced = !showAdvanced }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Configuração do Servidor do App",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        text = if (showAdvanced) "OCULTAR" else "ALTERAR SERVIDOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant
                    )
                }

                if (showAdvanced) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "Ajuste os parâmetros abaixo para sincronizar as atualizações com seu próprio repositório público do GitHub:",
                            fontSize = 11.sp,
                            color = OnSurfaceVariant,
                            lineHeight = 15.sp
                        )

                        OutlinedTextField(
                            value = ownerInput,
                            onValueChange = { ownerInput = it },
                            label = { Text("Dono do Repositório (Usuário/Org)", fontSize = 11.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = repoInput,
                            onValueChange = { repoInput = it },
                            label = { Text("Nome do Repositório", fontSize = 11.sp) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = branchInput,
                                onValueChange = { branchInput = it },
                                label = { Text("Branch", fontSize = 11.sp) },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = apkPathInput,
                                onValueChange = { apkPathInput = it },
                                label = { Text("Caminho do APK", fontSize = 11.sp) },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.5f)
                            )
                        }

                        Button(
                            onClick = {
                                updater.owner = ownerInput
                                updater.repo = repoInput
                                updater.branch = branchInput
                                updater.apkPath = apkPathInput
                                Toast.makeText(context, "Parâmetros do servidor atualizados!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = OnPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Salvar Configuração", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
