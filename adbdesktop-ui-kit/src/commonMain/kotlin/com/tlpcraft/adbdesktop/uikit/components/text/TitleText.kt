package com.tlpcraft.adbdesktop.uikit.components.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme

/**
 * High-emphasis title for prominent list items or cards.
 *
 * Maps to Material 3 `titleLarge`.
 *
 * @param text The title text.
 * @param color Optional override color.
 */
@Composable
fun TitleLargeText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        fontWeight = fontWeight,
        style = MaterialTheme.typography.titleLarge,
        color = color ?: colorScheme.onBackground,
        textAlign = textAlign,
    )
}

/**
 * Standard title size for list rows, dialogs, or medium-emphasis headers.
 *
 * Maps to Material 3 `titleMedium`.
 *
 * @param text The title text.
 * @param color Optional override color.
 * @param fontWeight Optional override font weight.
 * @param textAlign Optional override text alignment.
 */
@Composable
fun TitleMediumText(
    text: String,
    color: Color? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = color ?: colorScheme.onBackground,
        fontWeight = fontWeight,
        textAlign = textAlign,
    )
}

/**
 * Compact title style for dense UI elements (e.g. small cards, nested lists).
 *
 * Maps to Material 3 `titleSmall`.
 *
 * @param text The title text.
 * @param color Optional override color.
 * @param fontWeight Optional override font weight.
 * @param textAlign Optional override text alignment.
 */
@Composable
fun TitleSmallText(
    text: String,
    color: Color? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = color ?: colorScheme.onBackground,
        fontWeight = fontWeight,
        textAlign = textAlign,
    )
}
