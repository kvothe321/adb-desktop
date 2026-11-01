package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * A rounded surface grouping related content, lifted off the window. Becomes clickable and hoverable when [onClick]
 * is set, and a [selected] card is outlined in the accent color. `modifier` is for layout only.
 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = AppTheme.shapes.large
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .surfaceCard(colors, shape)
            .then(if (selected) Modifier.border(2.dp, colors.accent, shape) else Modifier)
            .clip(shape)
            .then(if (hovered && onClick != null) Modifier.background(colors.surfaceHover.copy(alpha = HOVER_TINT)) else Modifier)
            .then(if (onClick != null) Modifier.interactive(interactionSource, enabled = true, role = Role.Button, onClick = onClick) else Modifier)
            .padding(AppTheme.spacing.lg),
        content = content,
    )
}

private const val HOVER_TINT = 0.5f

@UiKitPreview
@Composable
private fun CardPreview() = PreviewContext {
    Column {
        Card { Text("Static card") }
        Card(selected = true, onClick = {}) { Text("Selected card") }
    }
}
