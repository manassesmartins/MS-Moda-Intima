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
                    text = "Gerencie dados, preferências e sincronização em nuvem",
                    fontSize = 13.sp,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Account Profile & Supabase Sync status card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SESSÃO DE USUÁRIO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Tertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = userEmail ?: "Modo Offline (Sem Sessão)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (userEmail != null) "Sessão Ativa na Nuvem" else "Faça Login para salvar na Nuvem",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }

        // Real-Time Sync toggle & status card centered
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.8f)) {
                        Text(
                            text = "Sincronização em Nuvem",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        Text(
                            text = "Manter dados sincronizados em tempo real na nuvem de forma segura",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isCloudEnabled,
                        onCheckedChange = onCloudEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnPrimary,
                            checkedTrackColor = Tertiary,
                            uncheckedThumbColor = OnSurfaceVariant,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("sync_toggle")
                    )
                }
                if (isCloudEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estado do Backup: " + when (syncState) {
                                "SYNCED" -> "Sincronizado"
                                "SYNCING" -> "Sincando..."
                                "ERROR_SYNC" -> "Sem Conexão ou Não Configurado"
                                else -> "Salvo Offline"
                            },
                            fontSize = 13.sp,
                            color = OnSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onForceSync,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Tertiary.copy(alpha = 0.15f),
                                contentColor = Tertiary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Sincronizar Agora",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // Local State Persistence details
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ARMAZENAMENTO OFFLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Secondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "O MS Moda Íntima salva todos os dados em cache local seguro (Room DB) quando estiver sem sinal de internet. Assim que a conexão for restabelecida, a sincronização é completada em segundo plano.",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        lineHeight = 18.sp
                    )
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
                        ColorSchemeUiItem("ROSE", "Ouro Rosé", Color(0xFFFBBF24), Color(0xFFD97706)),
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

        // Reset Cache & seeding options
        item {
            Button(
                onClick = onClearAll,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF93000A).copy(alpha = 0.08f),
                    contentColor = Color(0xFFFFB4AB)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF93000A).copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_data_button")
            ) {
                Text(
                    text = "Resetar Informações Locais",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Integration Status details & secrets explanation inside Settings Screen
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INTEGRAÇÃO COM A NUVEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Tertiary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (com.example.data.api.SupabaseClient.isConfigured) {
                            "Status: CONECTADO ✅\nO aplicativo está totalmente integrado e pronto para sincronizar dados e contas na Nuvem."
                        } else {
                            "Status: MODO SEGURO OFF-LINE ⚠️\nOs dados de usuário estão protegidos localmente neste dispositivo (Room DB). Configure as chaves de conexão segura do servidor no AI Studio para ativar as contas seguras em nuvem."
                        },
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // VERSION & INTEGRITY SECTION
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SINCRONIZAÇÃO DE VERSÃO CLOUD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "O aplicativo integra-se com a nuvem para garantir a integridade total do banco de dados e sincronizar recursos. Verifique se há melhorias de segurança ou patches de otimização de consultas disponíveis para o sistema.",
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

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "CONFIGURAÇÕES DO REPOSITÓRIO GITHUB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Secondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = ownerInput,
                    onValueChange = { ownerInput = it },
                    label = { Text("Nome do Proprietário / Org") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = OnSurfaceVariant,
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = repoInput,
                    onValueChange = { repoInput = it },
                    label = { Text("Nome do Repositório") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = OnSurfaceVariant,
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = branchInput,
                    onValueChange = { branchInput = it },
                    label = { Text("Branch de Sincronização") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = OnSurfaceVariant,
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apkPathInput,
                    onValueChange = { apkPathInput = it },
                    label = { Text("Caminho do APK do App") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = OnSurfaceVariant,
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = versionJsonPathInput,
                    onValueChange = { versionJsonPathInput = it },
                    label = { Text("Caminho do JSON de Versão (Ex: version.json)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = OnSurfaceVariant,
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (ownerInput.isNotBlank() && repoInput.isNotBlank()) {
                            updater.owner = ownerInput
                            updater.repo = repoInput
                            updater.branch = branchInput
                            updater.apkPath = apkPathInput
                            updater.versionJsonPath = versionJsonPathInput
                            Toast.makeText(context, "Configurações atualizadas com sucesso!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Proprietário e Repositório não podem estar em branco.", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Secondary,
                        contentColor = OnPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salvar Ajustes do Repositório", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Logout Button item
        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.05f),
                    contentColor = Primary
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("logout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sair da Conta (Logout)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
