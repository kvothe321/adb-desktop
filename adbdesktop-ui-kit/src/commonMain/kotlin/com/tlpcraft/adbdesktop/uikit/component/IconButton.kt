package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * An icon-only action. [contentDescription] is also shown as a tooltip, so it is required. A [selected] button is
 * highlighted, for actions that toggle a mode on. `modifier` is for layout only.
 */
@Composable
fun IconButton(
    icon: IconName,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: ControlSize = ControlSize.Medium,
    enabled: Boolean = true,
    selected: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    Tooltip(contentDescription, modifier) {
        Box(
            modifier = Modifier
                .size(size.height)
                .clip(AppTheme.shapes.pill)
                .background(
                    when {
                        selected -> AppTheme.colors.accentSubtle
                        hovered && enabled -> AppTheme.colors.surfaceHover
                        else -> Color.Transparent
                    },
                )
                .interactive(interactionSource, enabled = enabled, role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            val tint = when {
                !enabled -> AppTheme.colors.textDisabled
                selected -> AppTheme.colors.accent
                else -> AppTheme.colors.textSecondary
            }
            IconGlyph(icon, contentDescription = contentDescription, tint = tint)
        }
    }
}

@UiKitPreview
@Composable
private fun IconButtonPreview() = PreviewContext {
    Row {
        IconButton(IconName.Refresh, contentDescription = "Refresh", onClick = {})
        IconButton(IconName.Close, contentDescription = "Close", onClick = {}, size = ControlSize.Small)
        IconButton(IconName.Delete, contentDescription = "Delete", onClick = {}, enabled = false)
        IconButton(IconName.Mirror, contentDescription = "Phone", onClick = {}, selected = true)
    }
}
