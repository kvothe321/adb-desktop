package com.tlpcraft.adbdesktop.uikit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
internal data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceHover: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val accent: Color,
    val onAccent: Color,
    val accentSubtle: Color,
    val info: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val isDark: Boolean,
)

internal val LightColors = AppColors(
    background = Color(0xFFF3F3F6),
    surface = Color(0xFFFFFFFF),
    surfaceRaised = Color(0xFFE9E9F0),
    surfaceHover = Color(0xFFE2E2EC),
    border = Color(0xFFD5D5E0),
    textPrimary = Color(0xFF1C1C22),
    textSecondary = Color(0xFF5C5C6B),
    textDisabled = Color(0xFFA0A0AE),
    accent = Color(0xFF5254CC),
    onAccent = Color(0xFFFFFFFF),
    accentSubtle = Color(0xFFE3E3FA),
    info = Color(0xFF2F6FDB),
    success = Color(0xFF2E8B57),
    warning = Color(0xFFC77700),
    error = Color(0xFFC62E3A),
    isDark = false,
)

internal val DarkColors = AppColors(
    background = Color(0xFF0F1117),
    surface = Color(0xFF151822),
    surfaceRaised = Color(0xFF1C2030),
    surfaceHover = Color(0xFF232838),
    border = Color(0xFF2A2F40),
    textPrimary = Color(0xFFEBEBF0),
    textSecondary = Color(0xFF9A9CB0),
    textDisabled = Color(0xFF5A5D70),
    accent = Color(0xFF6366F1),
    onAccent = Color(0xFFFFFFFF),
    accentSubtle = Color(0xFF262A55),
    info = Color(0xFF5B9BFF),
    success = Color(0xFF4CC38A),
    warning = Color(0xFFF0A93B),
    error = Color(0xFFFF6B75),
    isDark = true,
)

internal val LocalAppColors = staticCompositionLocalOf { LightColors }
