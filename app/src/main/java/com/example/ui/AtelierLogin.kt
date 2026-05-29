package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.SupabaseClient
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MsModaIntimaLoginScreen(viewModel: TransactionViewModel) {
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

    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authSuccessMessage by viewModel.authSuccessMessage.collectAsStateWithLifecycle()
    val appName by viewModel.appName.collectAsStateWithLifecycle()

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SurfaceDark,
                        androidx.compose.ui.graphics.lerp(SurfaceDark, Primary, 0.07f),
                        SurfaceDark
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Decorative floating blurry glows
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(240.dp)
                .offset(x = (-40).dp, y = (-40).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(SurfaceContainerHigh, CircleShape)
                        .border(1.5.dp, Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Cadeado de Segurança",
                        tint = Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = appName.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "MS Moda Íntima",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Administração Segura de Finanças",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Main Auth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TAB Toggles for LOGIN / REGISTER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TabToggleItem(
                            title = "Entrar",
                            isSelected = isLoginMode,
                            onClick = {
                                isLoginMode = true
                                viewModel.clearAuthMessages()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        TabToggleItem(
                            title = "Cadastrar",
                            isSelected = !isLoginMode,
                            onClick = {
                                isLoginMode = false
                                viewModel.clearAuthMessages()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = OnSurfaceVariant
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = SurfaceContainerHigh,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Senha") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = OnSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            TextButton(
                                onClick = { showPassword = !showPassword },
                                colors = ButtonDefaults.textButtonColors(contentColor = Primary)
                            ) {
                                Text(
                                    text = if (showPassword) "OCULTAR" else "VER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = SurfaceContainerHigh,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    // Error Notification Box
                    AnimatedVisibility(
                        visible = authError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        authError?.let { err ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ErrorColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .border(1.dp, ErrorColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = err,
                                    fontSize = 12.sp,
                                    color = ErrorColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Success Notification Box
                    AnimatedVisibility(
                        visible = authSuccessMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        authSuccessMessage?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Tertiary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Tertiary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = msg,
                                    fontSize = 12.sp,
                                    color = Tertiary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Action Button with Ripple and Touch Target
                    Button(
                        onClick = {
                            if (isLoginMode) {
                                viewModel.loginUser(email, password)
                            } else {
                                viewModel.signUpUser(email, password)
                            }
                        },
                        enabled = !authLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = OnPrimary,
                            disabledContainerColor = SurfaceContainerHigh,
                            disabledContentColor = OnSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit_button")
                    ) {
                        if (authLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = OnPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (isLoginMode) "Acessar Banco de Dados" else "Registrar Minha Conta",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Connection Status & Educational block
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isSupabaseActive = SupabaseClient.isConfigured

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isSupabaseActive) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isSupabaseActive) Tertiary else OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isSupabaseActive) "Sincronização em Nuvem Ativa" else "Modo Off-line Seguro (Sem dependências)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSupabaseActive) Tertiary else OnSurfaceVariant
                        )
                    }

                    Text(
                        text = if (isSupabaseActive) {
                            "As contas criadas serão autenticadas via servidor seguro e as sessões serão mantidas de forma persistente."
                        } else {
                            "Seus dados estão protegidos off-line no Room SQL local. Para habilitar o modo de integração em nuvem, configure as chaves de conexão segura do servidor."
                        },
                        fontSize = 10.sp,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )

                    if (!isSupabaseActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Dica: Você pode acessar localmente usando admin@atelier.com e admin123!",
                                    fontSize = 10.sp,
                                    color = Primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabToggleItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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

    Surface(
        onClick = onClick,
        color = if (isSelected) SurfaceContainerHigh else Color.Transparent,
        contentColor = if (isSelected) Primary else OnSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxHeight()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
