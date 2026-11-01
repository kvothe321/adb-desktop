package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.border
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * Shows [text] while the pointer hovers [content], on a raised surface so it reads in both themes.
 * `modifier` is for layout only.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tooltip(text: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip(
                modifier = Modifier.border(1.dp, AppTheme.colors.border, AppTheme.shapes.medium),
                shape = AppTheme.shapes.medium,
                containerColor = AppTheme.colors.surfaceRaised,
                contentColor = AppTheme.colors.textPrimary,
            ) {
                Text(text, variant = TextVariant.Caption)
            }
        },
        state = rememberTooltipState(),
        modifier = modifier,
        content = content,
    )
}

@UiKitPreview
@Composable
private fun TooltipPreview() = PreviewContext {
    Tooltip("Refresh") { Text("Hover me") }
}
