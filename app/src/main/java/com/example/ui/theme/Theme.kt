package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

fun getDynamicColorScheme(schemeName: String, isDark: Boolean): ColorScheme {
    return if (isDark) {
        when (schemeName.uppercase()) {
            "BLUE" -> darkColorScheme(
                primary = Color(0xFF60A5FA),
                onPrimary = Color(0xFF0F172A),
                secondary = Color(0xFF93C5FD),
                onSecondary = Color(0xFF1E293B),
                tertiary = Color(0xFFA5F3FC),
                onTertiary = Color(0xFF0F172A),
                error = Color(0xFFF87171),
                background = Color(0xFF0F172A),
                onBackground = Color(0xFFF8FAFC),
                surface = Color(0xFF1E293B),
                onSurface = Color(0xFFF8FAFC),
                surfaceVariant = Color(0xFF334155),
                onSurfaceVariant = Color(0xFFCBD5E1)
            )
            "GREEN" -> darkColorScheme(
                primary = Color(0xFF34D399),
                onPrimary = Color(0xFF022C22),
                secondary = Color(0xFF6EE7B7),
                onSecondary = Color(0xFF042F2E),
                tertiary = Color(0xFFA7F3D0),
                onTertiary = Color(0xFF022C22),
                error = Color(0xFFF87171),
                background = Color(0xFF022C22),
                onBackground = Color(0xFFECFDF5),
                surface = Color(0xFF064E3B),
                onSurface = Color(0xFFECFDF5),
                surfaceVariant = Color(0xFF065F46),
                onSurfaceVariant = Color(0xFFD1FAE5)
            )
            "ROSE" -> darkColorScheme(
                primary = Color(0xFFFBBF24),
                onPrimary = Color(0xFF1C1917),
                secondary = Color(0xFFFDE68A),
                onSecondary = Color(0xFF292524),
                tertiary = Color(0xFFFEF3C7),
                onTertiary = Color(0xFF1C1917),
                error = Color(0xFFF87171),
                background = Color(0xFF1C1917),
                onBackground = Color(0xFFFAFAF9),
                surface = Color(0xFF292524),
                onSurface = Color(0xFFFAFAF9),
                surfaceVariant = Color(0xFF44403C),
                onSurfaceVariant = Color(0xFFD6D3D1)
            )
            "RED" -> darkColorScheme(
                primary = Color(0xFFF87171),
                onPrimary = Color(0xFF451212),
                secondary = Color(0xFFFCA5A5),
                onSecondary = Color(0xFF1F1212),
                tertiary = Color(0xFFFEE2E2),
                onTertiary = Color(0xFF451212),
                error = Color(0xFFEF4444),
                background = Color(0xFF1F1212),
                onBackground = Color(0xFFFEF2F2),
                surface = Color(0xFF2C1E1E),
                onSurface = Color(0xFFFEF2F2),
                surfaceVariant = Color(0xFF452B2B),
                onSurfaceVariant = Color(0xFFFCA5A5)
            )
            else -> darkColorScheme( // PINK - Default Original Elegant Dark Aesthetic
                primary = Primary,
                onPrimary = OnPrimary,
                secondary = Secondary,
                onSecondary = OnSecondary,
                tertiary = Tertiary,
                onTertiary = OnTertiary,
                error = ErrorColor,
                onError = OnError,
                background = SurfaceDark,
                onBackground = OnSurface,
                surface = SurfaceDark,
                onSurface = OnSurface,
                surfaceVariant = SurfaceContainer,
                onSurfaceVariant = OnSurfaceVariant
            )
        }
    } else {
        when (schemeName.uppercase()) {
            "BLUE" -> lightColorScheme(
                primary = Color(0xFF2563EB),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFF3B82F6),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFF06B6D4),
                onTertiary = Color(0xFFFFFFFF),
                error = Color(0xFFDC2626),
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF0F172A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFE2E8F0),
                onSurfaceVariant = Color(0xFF475569)
            )
            "GREEN" -> lightColorScheme(
                primary = Color(0xFF059669),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFF10B981),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFF14B8A6),
                onTertiary = Color(0xFFFFFFFF),
                error = Color(0xFFDC2626),
                background = Color(0xFFF0FDF4),
                onBackground = Color(0xFF064E3B),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF064E3B),
                surfaceVariant = Color(0xFFD1FAE5),
                onSurfaceVariant = Color(0xFF065F46)
            )
            "ROSE" -> lightColorScheme(
                primary = Color(0xFFD97706),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFFF59E0B),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFFEAB308),
                onTertiary = Color(0xFFFFFFFF),
                error = Color(0xFFDC2626),
                background = Color(0xFFFAFAF9),
                onBackground = Color(0xFF1C1917),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF1C1917),
                surfaceVariant = Color(0xFFF3F4F6),
                onSurfaceVariant = Color(0xFF44403C)
            )
            "RED" -> lightColorScheme(
                primary = Color(0xFFDC2626),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFFEF4444),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFFF43F5E),
                onTertiary = Color(0xFFFFFFFF),
                error = Color(0xFFB91C1C),
                background = Color(0xFFFEF2F2),
                onBackground = Color(0xFF451212),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF451212),
                surfaceVariant = Color(0xFFFEE2E2),
                onSurfaceVariant = Color(0xFF7F1D1D)
            )
            else -> lightColorScheme( // PINK - Default Original Light Aesthetic
                primary = Color(0xFFDB2777),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFFEC4899),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFFF43F5E),
                onTertiary = Color(0xFFFFFFFF),
                error = Color(0xFFE11D48),
                background = Color(0xFFFFF5F7),
                onBackground = Color(0xFF3B071E),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF3B071E),
                surfaceVariant = Color(0xFFFFE4E6),
                onSurfaceVariant = Color(0xFF9F1239)
            )
        }
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    colorSchemeName: String = "PINK",
    fontSizeScale: Float = 1.0f,
    content: @Composable () -> Unit,
) {
    val colorScheme = remember(darkTheme, colorSchemeName) {
        getDynamicColorScheme(colorSchemeName, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        val currentDensity = LocalDensity.current
        val customDensity = remember(currentDensity.density, currentDensity.fontScale, fontSizeScale) {
            Density(
                density = currentDensity.density,
                fontScale = currentDensity.fontScale * fontSizeScale
            )
        }
        CompositionLocalProvider(LocalDensity provides customDensity) {
            content()
        }
    }
}
