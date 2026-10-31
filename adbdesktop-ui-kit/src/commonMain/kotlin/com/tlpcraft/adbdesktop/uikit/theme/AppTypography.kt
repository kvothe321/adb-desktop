package com.tlpcraft.adbdesktop.uikit.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tlpcraft.adbdesktop.uikit.resources.JetBrainsMonoRegular
import com.tlpcraft.adbdesktop.uikit.resources.Res
import org.jetbrains.compose.resources.Font

@Immutable
internal data class AppTypography(
    val title: TextStyle,
    val heading: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val mono: TextStyle,
)

@Composable
internal fun rememberAppTypography(): AppTypography {
    val monoFamily = FontFamily(Font(Res.font.JetBrainsMonoRegular))
    return AppTypography(
        title = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
        heading = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
        body = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
        bodyStrong = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
        label = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
        caption = TextStyle(fontSize = 11.sp, lineHeight = 14.sp),
        mono = TextStyle(fontFamily = monoFamily, fontSize = 12.sp, lineHeight = 16.sp),
    )
}

internal val LocalAppTypography = staticCompositionLocalOf<AppTypography> { error("AppTypography is provided by AppTheme") }
