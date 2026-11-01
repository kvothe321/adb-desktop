package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * Two panes edge to edge, split by a hairline the user can drag to resize [start]. Where there is no room for both
 * side by side, [start] goes above [end]. `modifier` is for layout only.
 */
@Composable
fun SplitPane(
    start: @Composable () -> Unit,
    end: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initialStartWidth: Dp = 340.dp,
    minPaneWidth: Dp = 240.dp,
) {
    var startWidth by remember { mutableStateOf(initialStartWidth) }
    val startContent = remember(start) { movableContentOf(start) }
    val endContent = remember(end) { movableContentOf(end) }
    val density = LocalDensity.current
    val paneModifier = Modifier.background(AppTheme.colors.surface)

    BoxWithConstraints(modifier.fillMaxSize()) {
        Divider()
        if (maxWidth < minPaneWidth * 2 + GAP) {
            Column(Modifier.fillMaxSize()) {
                Box(paneModifier.fillMaxWidth().weight(1f)) { startContent() }
                Divider()
                Box(paneModifier.fillMaxWidth().weight(1f)) { endContent() }
            }
            return@BoxWithConstraints
        }
        val maxStartWidth = maxWidth - minPaneWidth - GAP
        val dragState = rememberDraggableState { delta ->
            startWidth = (startWidth + with(density) { delta.toDp() }).coerceIn(minPaneWidth, maxStartWidth)
        }
        Row(Modifier.fillMaxSize()) {
            Box(paneModifier.width(startWidth.coerceIn(minPaneWidth, maxStartWidth)).fillMaxHeight()) { startContent() }
            Box(
                paneModifier
                    .width(GAP)
                    .fillMaxHeight()
                    .pointerHoverIcon(HorizontalResizePointer)
                    .draggable(dragState, Orientation.Horizontal),
            ) {
                Divider(orientation = DividerOrientation.Vertical)
            }
            Box(paneModifier.weight(1f).fillMaxHeight()) { endContent() }
        }
    }
}

private val GAP = 5.dp

@UiKitPreview
@Composable
private fun SplitPanePreview() = PreviewContext {
    SplitPane(start = { Text("List") }, end = { Text("Details") })
}
