package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import android.net.Uri
import com.example.ui.TransactionViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BusinessSetupScreen(viewModel: TransactionViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Setup input states
    var brandName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Moda Íntima") }
    var niche by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("PINK") }
    var logoText by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("CROWN") }
    var logoImageBase64 by remember { mutableStateOf<String?>(null) }

    // Standard pre-defined options for categories
    val categoryOptions = listOf("Moda Íntima", "Vestuário / Têxtil", "Moda Praia / Fitness", "Artesanato & Crochê", "Calçados & Bolsas", "Outro")
    
    // Icon choices
    val iconsMap = mapOf(
        "CROWN" to Icons.Default.Star,
        "BAG" to Icons.Default.ShoppingCart,
        "HEART" to Icons.Default.Favorite,
        "BUILD" to Icons.Default.Build,
        "PERSON" to Icons.Default.Person
    )
    val iconLabels = mapOf(
        "CROWN" to "Premium (Estrela)",
        "BAG" to "Comércio (Sacola)",
        "HEART" to "Artesanal (Coração)",
        "BUILD" to "Confecção (Ferramentas)",
        "PERSON" to "Personalizado (Perfil)"
    )

    // Dynamic local colors depending on choice to render live preview beautifully
    val previewScheme = remember(selectedColor) {
        getDynamicColorScheme(selectedColor, isDark = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0C20),
                        Color(0xFF151030),
                        Color(0xFF0C091A)
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Onboarding Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "PASSO ÚNICO",
                    color = previewScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Personalize Seu Negócio",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Diga adeus ao genérico! Configure seu aplicativo com a identidade visual da sua marca.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // LIVE PREVIEW CARD (DYNAMICS COMPOSABLE)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = previewScheme.surface,
                    contentColor = previewScheme.onSurface
                ),
                border = BorderStroke(1.5.dp, previewScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PRÉ-VISUALIZAÇÃO DA SUA MARCA",
                        style = MaterialTheme.typography.labelSmall,
                        color = previewScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(previewScheme.background.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Dynamic Logo visual
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(previewScheme.primary.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, previewScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!logoImageBase64.isNullOrBlank()) {
                                AsyncImage(
                                    model = logoImageBase64,
                                    contentDescription = "Pré-visualização da logo",
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = iconsMap[selectedIconName] ?: Icons.Default.Star,
                                        contentDescription = null,
                                        tint = previewScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (brandName.isNotBlank()) brandName.take(3).uppercase() else "LOG",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = previewScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Text details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (brandName.isNotBlank()) brandName else "Minha Marca Co.",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = previewScheme.onSurface
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(previewScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = previewScheme.secondary
                                    )
                                }
                                if (niche.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .background(previewScheme.tertiary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = niche,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = previewScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // INPUT FORM CARD
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1736))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Field 1: Brand Name
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Nome da Sua Marca / Negócio", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        OutlinedTextField(
                            value = brandName,
                            onValueChange = { brandName = it },
                            placeholder = { Text("Ex: Confecção Real, Bella Confecções") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = previewScheme.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedContainerColor = Color.Black.copy(alpha = 0.25f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.12f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Field 2: Category Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Categoria de Produção", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categoryOptions.forEach { opt ->
                                val selected = category == opt
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) previewScheme.primary else Color.White.copy(alpha = 0.06f))
                                        .border(1.dp, if (selected) previewScheme.primary else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable { category = opt }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = opt,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.Black else Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Field 3: Niche
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Qual o Nicho ou Foco? (Opcional)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        OutlinedTextField(
                            value = niche,
                            onValueChange = { niche = it },
                            placeholder = { Text("Ex: Crochê Manual, Lingerie Fina, Fitness Atacado") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = previewScheme.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedContainerColor = Color.Black.copy(alpha = 0.25f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.12f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Field 4: Custom Logo Image Upload Picker
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Carregar Imagem de Logo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri: Uri? ->
                            uri?.let {
                                try {
                                    val inputStream: java.io.InputStream? = context.contentResolver.openInputStream(it)
                                    val bytes = inputStream?.readBytes()
                                    inputStream?.close()
                                    if (bytes != null) {
                                        val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                                        logoImageBase64 = "data:image/png;base64,$base64"
                                        Toast.makeText(context, "Logo carregada com sucesso!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erro ao carregar imagem: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        OutlinedCard(
                            onClick = { launcher.launch("image/*") },
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(previewScheme.primary.copy(alpha = 0.12f), CircleShape)
                                        .border(1.dp, previewScheme.primary.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!logoImageBase64.isNullOrBlank()) {
                                        AsyncImage(
                                            model = logoImageBase64,
                                            contentDescription = "Logo",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = previewScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (!logoImageBase64.isNullOrBlank()) "Imagem Pronta" else "Escolher Imagem Galeria",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (!logoImageBase64.isNullOrBlank()) "Logomarca carregada com sucesso localmente" else "Toque para abrir seus arquivos de imagem locais",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    // Field 5: APP DESIGN COLORS
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Escolha a Paleta do Aplicativo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val colorChoices = listOf(
                                "PINK" to Color(0xFFF472B6),
                                "BLUE" to Color(0xFF60A5FA),
                                "GREEN" to Color(0xFF34D399),
                                "ROSE" to Color(0xFFF6A6B2),
                                "RED" to Color(0xFFF87171)
                            )
                            colorChoices.forEach { (schemeName, uiColor) ->
                                val selected = selectedColor == schemeName
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(uiColor)
                                        .border(
                                            width = if (selected) 3.dp else 0.dp,
                                            color = if (selected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = schemeName },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ACTION BUTTON
            Button(
                onClick = {
                    if (brandName.isBlank()) {
                        Toast.makeText(context, "Insira o nome da sua marca!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveBrandConfig(
                            brandName = brandName,
                            category = category,
                            niche = niche,
                            colorScheme = selectedColor,
                            logoText = brandName.take(3),
                            logoIcon = "IMAGE",
                            logoImage = logoImageBase64
                        )
                        Toast.makeText(context, "Sua marca está pronta para uso!", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = previewScheme.primary,
                    contentColor = previewScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Text("Concluir e Iniciar Aplicativo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
