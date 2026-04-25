package com.tlpcraft.adbdesktop.uikit.components.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme

/**
 * Largest body text variant for long-form readable content blocks.
 *
 * Maps to Material 3 `bodyLarge`.
 *
 * @param text The paragraph text.
 * @param color Optional override color.
 * @param textAlign Optional text alignment (e.g., center, justify).
 */
@Composable
fun BodyLargeText(text: String, color: Color? = null, textAlign: TextAlign? = null) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = textAlign,
        color = color ?: colorScheme.onBackground
    )
}

/**
 * Default body text style for most readable content.
 *
 * Maps to Material 3 `bodyMedium`.
 *
 * @param text The paragraph text.
 * @param color Optional override color.
 * @param textAlign Optional text alignment (e.g., center, justify).
 * @param modifier Optional [Modifier] for additional styling.
 */
@Composable
fun BodyMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        textAlign = textAlign,
        color = color ?: colorScheme.onBackground,
        modifier = modifier
    )
}

/**
 * Compact body text for secondary or dense supporting information.
 *
 * Use for supplemental details, disclaimers, metadata, or subordinate text
 * in tight layouts where `bodyMedium` would feel too large.
 * Maps to Material 3 `bodySmall`.
 *
 * @param text Content to display.
 * @param color Optional override (defaults to `onBackground`).
 */
@Composable
fun BodySmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color ?: colorScheme.onBackground,
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow
    )
}
