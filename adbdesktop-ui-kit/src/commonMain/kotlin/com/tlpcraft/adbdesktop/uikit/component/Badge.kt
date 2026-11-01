package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

private const val BADGE_BACKGROUND_ALPHA = 0.14f

/** A short status label with a colored dot. `modifier` is for layout only. */
@Composable
fun Badge(text: String, modifier: Modifier = Modifier, tone: Tone = Tone.Neutral) {
    Row(
        modifier = modifier
            .background(tone.color.copy(alpha = BADGE_BACKGROUND_ALPHA), AppTheme.shapes.pill)
            .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.xxs + 1.dp),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(6.dp).background(tone.color, AppTheme.shapes.pill))
        Text(text, variant = TextVariant.Label, tone = tone, maxLines = 1)
    }
}

@UiKitPreview
@Composable
private fun BadgePreview() = PreviewContext {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        Tone.entries.forEach { Badge(it.name, tone = it) }
    }
}
