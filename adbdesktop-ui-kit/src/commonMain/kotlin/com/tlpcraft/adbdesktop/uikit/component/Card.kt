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
 * A bordered surface grouping related content. Becomes clickable and hoverable when [onClick] is set.
 * `modifier` is for layout only.
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
    val borderColor = if (selected) AppTheme.colors.accent else AppTheme.colors.border
    val background = if (hovered && onClick != null) AppTheme.colors.surfaceHover else AppTheme.colors.surface

    Column(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(if (selected) 2.dp else 1.dp, borderColor, shape)
            .then(if (onClick != null) Modifier.interactive(interactionSource, enabled = true, role = Role.Button, onClick = onClick) else Modifier)
            .padding(AppTheme.spacing.md),
        content = content,
    )
}

@UiKitPreview
@Composable
private fun CardPreview() = PreviewContext {
    Column {
        Card { Text("Static card") }
        Card(selected = true, onClick = {}) { Text("Selected card") }
    }
}
