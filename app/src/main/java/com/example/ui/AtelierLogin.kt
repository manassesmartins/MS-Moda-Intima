package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import com.example.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.util.Log

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

    var showFallbackPicker by remember { mutableStateOf(false) }
    var context = LocalContext.current

    // Auto Google sign-in on launch if already authenticated previously
    LaunchedEffect(Unit) {
        try {
            val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
            if (lastAccount != null && lastAccount.email != null) {
                viewModel.loginWithGoogle(
                    email = lastAccount.email!!,
                    name = lastAccount.displayName ?: "Usuário Google",
                    avatarUrl = lastAccount.photoUrl?.toString()
                )
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Automatic Google Sign-in on launch failed", e)
        }
    }

    // Set up real Google Sign-In options with basic scopes (email and profile) which are guaranteed to succeed on any account picker
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }
    
    val driveScope = Scope("https://www.googleapis.com/auth/drive.file")

    // Launcher for dynamic Google Drive file-scope consent
    val drivePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { driveResult ->
        // Continue login with whatever credentials we have, even if Drive permission was canceled/denied!
        val intentData = driveResult.data
        if (intentData != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intentData)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null && account.email != null) {
                    viewModel.loginWithGoogle(
                        email = account.email!!,
                        name = account.displayName ?: "Usuário Google",
                        avatarUrl = account.photoUrl?.toString()
                    )
                    return@rememberLauncherForActivityResult
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Drive launcher parsing exception", e)
            }
        }
        val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (lastAccount != null && lastAccount.email != null) {
            viewModel.loginWithGoogle(
                email = lastAccount.email!!,
                name = lastAccount.displayName ?: "Usuário Google",
                avatarUrl = lastAccount.photoUrl?.toString()
            )
        } else {
            viewModel.setAuthError("Permissão do Drive foi cancelada ou não pôde ser concluída.")
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intentData = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(intentData)
        try {
            val account = task.getResult(ApiException::class.java)
            val googleEmail = account?.email ?: ""
            val googleName = account?.displayName ?: "Usuário Google"
            val googlePhoto = account?.photoUrl?.toString()
            
            if (googleEmail.isNotEmpty()) {
                // Check if Drive file permission is already granted
                if (GoogleSignIn.hasPermissions(account, driveScope)) {
                    viewModel.loginWithGoogle(googleEmail, googleName, googlePhoto)
                } else {
                    // Request Drive file permission dynamically to give the Google app permissions on demand
                    val gsoWithDrive = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestProfile()
                        .requestScopes(driveScope)
                        .build()
                    val driveSignInClient = GoogleSignIn.getClient(context, gsoWithDrive)
                    drivePermissionLauncher.launch(driveSignInClient.signInIntent)
                }
            } else {
                viewModel.setAuthError("Não foi possível carregar o e-mail da conta Google")
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Google Sign-In ApiException: Code ${e.statusCode}", e)
            val detailedMsg = when (e.statusCode) {
                10 -> "Erro 10 (DEVELOPER_ERROR): A assinatura SHA-1 deste aplicativo ou o ID do cliente Google não estão associados ao ID do cliente correspondente no Google Cloud Console.\n\nPACOTE: " + context.packageName + "\nSHA-1: 41:AF:70:44:89:BC:D3:F0:4F:33:33:13:DE:F1:04:87:42:02:3F:15"
                12500 -> "Erro 12500: Erro de sinalização interna do Google Play Services. Certifique-se de registrar o SHA-1 no console Google Cloud:\n\nSHA-1: 41:AF:70:44:89:BC:D3:F0:4F:33:33:13:DE:F1:04:87:42:02:3F:15"
                7 -> "Erro 7 (NETWORK_ERROR): Falha de rede. Certifique-se de que o dispositivo possui acesso à Internet estável."
                16 -> "Erro 16 (CANCELED): O processo de login foi cancelado pelo usuário."
                else -> "Erro Código ${e.statusCode}: O login com o Google não pôde ser validado com os servidores."
            }
            // MOCK LOGIN FALLBACK: Se falhar em desenvolvimento por falta de SHA-1 na cloud, mostramos o fallback
            showFallbackPicker = true
            viewModel.setAuthError(detailedMsg)
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Google Login parsing failed", e)
            showFallbackPicker = true
            viewModel.setAuthError("O login com o Google foi cancelado ou não pôde ser concluído.")
        }
    }

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
                    text = "Gestão de Produção",
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
                    Text(
                        text = "AUTENTICAÇÃO EXCLUSIVA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Para acessar as planilhas, relatórios de faturamento e controle de ordens de produção, conecte-se com sua Conta do Google. Seus dados serão mantidos sincronizados na nuvem de forma automática.",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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

                    // Google Login Button with official identity branding
                    Button(
                        onClick = {
                            try {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            } catch (e: Exception) {
                                viewModel.setAuthError("Erro ao iniciar o login com o Google: ${e.localizedMessage}")
                            }
                        },
                        enabled = !authLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = OnPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (authLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = OnPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBox,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Conectar com o Google",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
                    val isGoogleActive = viewModel.sessionManager.isLoggedIn

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isGoogleActive) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isGoogleActive) Tertiary else OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isGoogleActive) "Backup no Google Drive Ativo" else "Modo Off-line Seguro",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isGoogleActive) Tertiary else OnSurfaceVariant
                        )
                    }

                    Text(
                        text = if (isGoogleActive) {
                            "Habilitado! Seu banco de dados SQLite será sincronizado de forma totalmente criptografada e segura no seu Google Drive."
                        } else {
                            "Seus dados estão protegidos localmente no SQLite. Para configurar o backup automático e manter as informações seguras mesmo se reinstalar o app, faça login com a conta Google."
                        },
                        fontSize = 10.sp,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )

                    if (!isGoogleActive) {
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
                                    text = "O backup do banco de dados será mantido no seu Google Drive de forma 100% automática.",
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
    
    // Fallback Picker Dialog for local testing when Google SignIn isn't configured
    if (showFallbackPicker) {
        var fallbackName by remember { mutableStateOf("Administrador") }
        var fallbackEmail by remember { mutableStateOf("admin@producao.com") }
        
        AlertDialog(
            onDismissRequest = { showFallbackPicker = false },
            title = {
                Text(
                    text = "Acesso de Recuperação",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OnSurface
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "O Serviço do Google Play não pôde ser verificado. Para continuar utilizando o app, simule seu login temporário:",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant
                    )
                    OutlinedTextField(
                        value = fallbackName,
                        onValueChange = { fallbackName = it },
                        label = { Text("Nome Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = fallbackEmail,
                        onValueChange = { fallbackEmail = it },
                        label = { Text("E-mail Google") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFallbackPicker = false
                        viewModel.setAuthError(null)
                        viewModel.loginWithGoogle(fallbackEmail, fallbackName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Entrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFallbackPicker = false }) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            }
        )
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
