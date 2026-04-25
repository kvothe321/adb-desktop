package com.tlpcraft.adbdesktop.uikit.theme

import adb_desktop.adbdesktop_ui_kit.generated.resources.JetBrainsMonoRegular
import adb_desktop.adbdesktop_ui_kit.generated.resources.Res
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font

val typography
    @Composable
    get() = Typography(
        displayLarge = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.displayLargeTextSize
        ),
        displayMedium = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.displayMediumTextSize
        ),
        displaySmall = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.displaySmallTextSize
        ),
        headlineLarge = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.headlineLargeTextSize
        ),
        headlineMedium = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.headlineMediumTextSize
        ),
        headlineSmall = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.headlineSmallTextSize
        ),
        titleLarge = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.titleLargeTextSize
        ),
        titleMedium = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.titleMediumTextSize
        ),
        titleSmall = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.titleSmallTextSize
        ),
        bodyLarge = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.bodyLargeTextSize
        ),
        bodyMedium = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.bodyMediumTextSize
        ),
        bodySmall = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.bodySmallTextSize
        ),
        labelLarge = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.labelLargeTextSize
        ),
        labelMedium = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.labelMediumTextSize
        ),
        labelSmall = TextStyle(
            fontFamily = jetbrainsMonoRegular,
            fontSize = dimensions.typography.labelSmallTextSize
        )
    )

val jetbrainsMonoRegular
    @Composable
    get() = FontFamily(Font(Res.font.JetBrainsMonoRegular))
