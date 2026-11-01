package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

private const val OVERLAY_ALPHA = 0.85f

/**
 * Accepts files dragged from the desktop onto [content], showing [hint] while they hover over it.
 * [onFilesDrop] receives absolute paths. `modifier` is for layout only.
 */
@Composable
fun FileDropArea(
    onFilesDrop: (List<String>) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var isHovering by remember { mutableStateOf(false) }
    val dropTarget = rememberFileDropTarget(onHoverChange = { isHovering = it }, onFilesDrop = onFilesDrop)

    Box(modifier.then(dropTarget)) {
        content()
        if (isHovering) DropOverlay(hint)
    }
}

@Composable
private fun DropOverlay(hint: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.accentSubtle.copy(alpha = OVERLAY_ALPHA))
            .border(2.dp, AppTheme.colors.accent),
        contentAlignment = Alignment.Center,
    ) {
        EmptyState(title = hint, icon = IconName.Upload, tone = Tone.Accent)
    }
}

/** A modifier that reports files dragged over the element. */
@Composable
internal expect fun rememberFileDropTarget(onHoverChange: (Boolean) -> Unit, onFilesDrop: (List<String>) -> Unit): Modifier

@UiKitPreview
@Composable
private fun DropOverlayPreview() = PreviewContext {
    Box(Modifier.fillMaxSize()) { DropOverlay("Drop to upload") }
}
