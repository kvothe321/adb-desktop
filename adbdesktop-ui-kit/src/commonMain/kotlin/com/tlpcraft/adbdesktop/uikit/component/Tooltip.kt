package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview

/** Shows [text] while the pointer hovers [content]. `modifier` is for layout only. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tooltip(text: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(text, variant = TextVariant.Caption) } },
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
