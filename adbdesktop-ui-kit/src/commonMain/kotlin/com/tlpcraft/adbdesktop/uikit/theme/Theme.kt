package com.tlpcraft.adbdesktop.uikit.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun AppTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val materialColorScheme = if (darkTheme) {
        materialDarkColorScheme
    } else {
        materialLightColorScheme
    }

    CompositionLocalProvider(
        LocalAppDimensions provides AppDimensions(),
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content,
        )
    }
}

val colorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme

val dimensions: AppDimensions
    @Composable
    @ReadOnlyComposable
    get() = LocalAppDimensions.current
