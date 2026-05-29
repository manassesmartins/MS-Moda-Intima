package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

val Primary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary

val OnPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onPrimary

val Secondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.secondary

val OnSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSecondary

val Tertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiary

val OnTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onTertiary

val ErrorColor: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.error

val OnError: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onError

val SurfaceDark: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val OnSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurface

val SurfaceContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val SurfaceContainerHigh: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val OnSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val SurfaceBright = Color(0xFF361525)
