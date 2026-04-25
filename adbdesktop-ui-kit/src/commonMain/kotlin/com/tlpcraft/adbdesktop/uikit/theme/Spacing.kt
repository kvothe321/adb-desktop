package com.tlpcraft.adbdesktop.uikit.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AppSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp
)

data class AppRadius(
    val sm: Dp = 4.dp,
    val md: Dp = 8.dp,
    val lg: Dp = 16.dp,
    val full: Dp = 999.dp
)

data class AppStroke(
    val thin: Dp = 1.dp,
    val md: Dp = 2.dp,
    val thick: Dp = 4.dp
)

data class AppDimensions(
    val spacing: AppSpacing = AppSpacing(),
    val radius: AppRadius = AppRadius(),
    val stroke: AppStroke = AppStroke(),
    val typography: TypographyTokens = TypographyTokens()
)

data class TypographyTokens(
    val displayLargeTextSize: TextUnit = 56.sp,
    val displayMediumTextSize: TextUnit = 38.sp,
    val displaySmallTextSize: TextUnit = 32.sp,
    val headlineLargeTextSize: TextUnit = 28.sp,
    val headlineMediumTextSize: TextUnit = 24.sp,
    val headlineSmallTextSize: TextUnit = 20.sp,
    val titleLargeTextSize: TextUnit = 20.sp,
    val titleMediumTextSize: TextUnit = 17.sp,
    val titleSmallTextSize: TextUnit = 15.sp,
    val bodyLargeTextSize: TextUnit = 17.sp,
    val bodyMediumTextSize: TextUnit = 15.sp,
    val bodySmallTextSize: TextUnit = 13.sp,
    val labelLargeTextSize: TextUnit = 16.sp,
    val labelMediumTextSize: TextUnit = 14.sp,
    val labelSmallTextSize: TextUnit = 12.sp
)

val LocalAppDimensions = staticCompositionLocalOf { AppDimensions() }
