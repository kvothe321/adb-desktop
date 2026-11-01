package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.theme.AppColors
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * The look of a card: the surface color, lifted by a soft shadow in light mode. Dark surfaces stand out from the
 * near-black window on their own, so they get only a faint hairline.
 */
internal fun Modifier.surfaceCard(colors: AppColors, shape: Shape): Modifier = this
    .then(if (colors.isDark) Modifier else Modifier.shadow(CARD_SHADOW, shape, ambientColor = SHADOW_COLOR, spotColor = SHADOW_COLOR))
    .background(colors.surface, shape)
    .then(if (colors.isDark) Modifier.border(1.dp, colors.border, shape) else Modifier)

/** Text on a [AppColors.textPrimary] background, such as a toast or a selected chip. */
@Composable
internal fun InverseText(text: String, variant: TextVariant = TextVariant.BodyStrong) {
    BasicText(text, style = variant.textStyle().copy(color = AppTheme.colors.background), maxLines = 1, overflow = TextOverflow.Ellipsis)
}

private val CARD_SHADOW = 3.dp
private const val SHADOW_ALPHA = 0.2f
private val SHADOW_COLOR = Color.Black.copy(alpha = SHADOW_ALPHA)
