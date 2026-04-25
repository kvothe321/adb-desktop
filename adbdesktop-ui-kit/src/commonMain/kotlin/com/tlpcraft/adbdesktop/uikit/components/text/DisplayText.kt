package com.tlpcraft.adbdesktop.uikit.components.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme

/**
 * High‑emphasis, largest hero text for prominent marketing or brand moments.
 *
 * Use sparingly (e.g. onboarding hero, large splash headline).
 * Maps to Material 3 `displayLarge`.
 *
 * @param text The text content.
 * @param color Optional override for the text color (defaults to `onBackground`).
 */
@Composable
fun DisplayLargeText(text: String, color: Color? = null) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayLarge,
        color = color ?: colorScheme.onBackground
    )
}

/**
 * Large promotional / emphatic text slightly smaller than `DisplayLargeText`.
 *
 * Good for secondary hero areas, large informational callouts.
 * Maps to Material 3 `displayMedium`.
 *
 * @param text The text content.
 * @param color Optional override color.
 */
@Composable
fun DisplayMediumText(text: String, color: Color? = null) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayMedium,
        color = color ?: colorScheme.onBackground
    )
}

/**
 * Smallest display style for standout text that should remain visually dominant
 * but less overwhelming than other display variants.
 *
 * Maps to Material 3 `displaySmall`.
 *
 * @param text The text content.
 * @param color Optional override color.
 */
@Composable
fun DisplaySmallText(text: String, color: Color? = null) {
    Text(
        text = text,
        style = MaterialTheme.typography.displaySmall,
        color = color ?: colorScheme.onBackground
    )
}
