package com.tlpcraft.adbdesktop.uikit.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val materialLightColorScheme = lightColorScheme(
    primary = Color(0xFF5254CC),
    primaryContainer = Color(0xFF6366F1),
    onPrimaryContainer = Color(0xFFF3F5F6),
    surface = Color(0xFFE4E4EF),
    onSurface = Color(0xFF242425),
    background = Color(0xFFE4E4EF),
)

val materialDarkColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1),
    primaryContainer = Color(0xFF6366F1),
    onPrimaryContainer = Color(0xFFF3F5F6),
    surface = Color(0xFF111118),
    onSurface = Color(0xFFEBEBEB),
    background = Color(0xFF0A0A0F),
)
