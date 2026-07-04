package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview

/**
 * Two panes side by side, separated by a handle the user can drag to resize [start].
 * `modifier` is for layout only.
 */
@Composable
fun SplitPane(
    start: @Composable () -> Unit,
    end: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initialStartWidth: Dp = 320.dp,
    minPaneWidth: Dp = 200.dp,
) {
    var startWidth by remember { mutableStateOf(initialStartWidth) }
    val density = LocalDensity.current

    BoxWithConstraints(modifier.fillMaxSize()) {
        val maxStartWidth = (maxWidth - minPaneWidth).coerceAtLeast(minPaneWidth)
        val dragState = rememberDraggableState { delta ->
            startWidth = (startWidth + with(density) { delta.toDp() }).coerceIn(minPaneWidth, maxStartWidth)
        }
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.width(startWidth.coerceAtMost(maxStartWidth)).fillMaxHeight()) { start() }
            Box(
                modifier = Modifier.width(HANDLE_WIDTH).fillMaxHeight().draggable(dragState, Orientation.Horizontal),
                contentAlignment = Alignment.Center,
            ) {
                Divider(orientation = DividerOrientation.Vertical)
            }
            Box(Modifier.weight(1f).fillMaxHeight()) { end() }
        }
    }
}

private val HANDLE_WIDTH = 6.dp

@UiKitPreview
@Composable
private fun SplitPanePreview() = PreviewContext {
    SplitPane(start = { Text("List") }, end = { Text("Details") })
}
