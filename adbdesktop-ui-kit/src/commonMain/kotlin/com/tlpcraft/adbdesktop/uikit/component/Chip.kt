package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/** A toggleable filter option: a filled pill when selected, a soft one otherwise. `modifier` is for layout only. */
@Composable
fun Chip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.pill
    val background = when {
        selected -> colors.textPrimary
        hovered -> colors.surfaceHover
        else -> colors.surfaceRaised
    }

    Box(
        modifier = modifier
            .heightIn(min = ControlSize.Small.height)
            .clip(shape)
            .background(background)
            .interactive(interactionSource, enabled = true, role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = AppTheme.spacing.md),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) InverseText(text, TextVariant.Label) else Text(text, variant = TextVariant.Label, maxLines = 1)
    }
}

@UiKitPreview
@Composable
private fun ChipPreview() = PreviewContext {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        Chip("All", selected = true, onClick = {})
        Chip("User", selected = false, onClick = {})
    }
}
