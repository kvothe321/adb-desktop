package com.tlpcraft.adbdesktop.uikit.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tlpcraft.adbdesktop.uikit.resources.InterBold
import com.tlpcraft.adbdesktop.uikit.resources.InterMedium
import com.tlpcraft.adbdesktop.uikit.resources.InterRegular
import com.tlpcraft.adbdesktop.uikit.resources.InterSemiBold
import com.tlpcraft.adbdesktop.uikit.resources.JetBrainsMonoRegular
import com.tlpcraft.adbdesktop.uikit.resources.Res
import org.jetbrains.compose.resources.Font

@Immutable
internal data class AppTypography(
    val display: TextStyle,
    val title: TextStyle,
    val heading: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val mono: TextStyle,
)

/** Inter throughout, with tighter tracking as sizes grow, the way large type is set on Apple platforms. */
@Composable
internal fun rememberAppTypography(): AppTypography {
    val inter = FontFamily(
        Font(Res.font.InterRegular, FontWeight.Normal),
        Font(Res.font.InterMedium, FontWeight.Medium),
        Font(Res.font.InterSemiBold, FontWeight.SemiBold),
        Font(Res.font.InterBold, FontWeight.Bold),
    )
    val monoFamily = FontFamily(Font(Res.font.JetBrainsMonoRegular))
    return AppTypography(
        display = TextStyle(fontFamily = inter, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp),
        title = TextStyle(fontFamily = inter, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp),
        heading = TextStyle(fontFamily = inter, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.15).sp),
        body = TextStyle(fontFamily = inter, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = (-0.1).sp),
        bodyStrong = TextStyle(fontFamily = inter, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.1).sp),
        label = TextStyle(fontFamily = inter, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
        caption = TextStyle(fontFamily = inter, fontSize = 12.sp, lineHeight = 16.sp),
        mono = TextStyle(fontFamily = monoFamily, fontSize = 12.sp, lineHeight = 17.sp),
    )
}

internal val LocalAppTypography = staticCompositionLocalOf<AppTypography> { error("AppTypography is provided by AppTheme") }
