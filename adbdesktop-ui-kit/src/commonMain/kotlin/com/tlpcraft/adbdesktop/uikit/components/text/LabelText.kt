package com.tlpcraft.adbdesktop.uikit.components.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme

/**
 * High‑emphasis label style for prominent interactive elements.
 *
 * Use for primary buttons, key navigation items, or emphasized UI labels
 * requiring strong visual weight. Maps to Material 3 `labelLarge`.
 *
 * @param text Label content.
 * @param color Optional override (defaults to `onBackground`).
 */
@Composable
fun LabelLargeText(text: String, color: Color? = null) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = color ?: colorScheme.onBackground
    )
}

/**
 * Default control label style.
 *
 * Use for most component labels (standard buttons, form field labels,
 * tab text, small section indicators). Balanced legibility vs. density.
 * Maps to Material 3 `labelMedium`.
 *
 * @param text Label content.
 * @param color Optional override (defaults to `onBackground`).
 */
@Composable
fun LabelMediumText(text: String, color: Color? = null) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color ?: colorScheme.onBackground
    )
}

/**
 * Low‑emphasis label for compact or auxiliary UI elements.
 *
 * Use for chips, overlines, helper/meta info, or tightly spaced
 * decorative labels. Avoid for primary actions. Maps to Material 3 `labelSmall`.
 *
 * @param text Label content.
 * @param color Optional override (defaults to `onBackground`).
 */
@Composable
fun LabelSmallText(text: String, modifier: Modifier = Modifier, color: Color? = null) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = color ?: colorScheme.onBackground
    )
}
