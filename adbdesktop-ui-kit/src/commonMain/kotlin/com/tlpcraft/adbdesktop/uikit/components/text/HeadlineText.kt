package com.tlpcraft.adbdesktop.uikit.components.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme

/**
 * Primary large section or screen title.
 *
 * Suitable for top-level headings in content-heavy screens.
 * Maps to Material 3 `headlineLarge`.
 *
 * @param text The heading text.
 * @param color Optional override color.
 */
@Composable
fun HeadlineLargeText(text: String, color: Color? = null) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        color = color ?: colorScheme.onBackground
    )
}

/**
 * Mid-level headline used for major subsections below the top hierarchy.
 *
 * Maps to Material 3 `headlineMedium`.
 *
 * @param text The heading text.
 * @param modifier Optional modifier.
 * @param color Optional override color.
 * @param maxLines Optional maximum number of lines.
 * @param overflow Optional overflow behavior.
 */
@Composable
fun HeadlineMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = color ?: colorScheme.onBackground,
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 * Lowest level headline, often for grouping related content blocks.
 *
 * Maps to Material 3 `headlineSmall`.
 *
 * @param text The heading text.
 * @param modifier Optional modifier.
 * @param color Optional override color.
 * @param maxLines Optional maximum number of lines.
 * @param overflow Optional overflow behavior.
 */
@Composable
fun HeadlineSmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = color ?: colorScheme.onBackground,
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow
    )
}
