package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import com.tlpcraft.adbdesktop.uikit.theme.Elevation

/**
 * Top-level window layout: a [sidebar] beside the main [content], with messages from [messageHostState]
 * floating above it. A [dock], when given, sits on the right edge and the user can drag its border to resize it.
 * `modifier` is for layout only.
 */
@Composable
fun Scaffold(
    sidebar: @Composable () -> Unit,
    messageHostState: MessageHostState,
    modifier: Modifier = Modifier,
    dock: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Row(modifier.fillMaxSize().background(AppTheme.colors.background)) {
        sidebar()
        Box(Modifier.weight(1f).fillMaxSize()) {
            Column(Modifier.fillMaxSize()) { content() }
            SnackbarHost(messageHostState.snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(AppTheme.spacing.lg)) { data ->
                Box(
                    Modifier
                        .widthIn(max = 520.dp)
                        .shadow(Elevation.overlay, AppTheme.shapes.medium)
                        .background(AppTheme.colors.surfaceRaised, AppTheme.shapes.medium)
                        .border(1.dp, AppTheme.colors.border, AppTheme.shapes.medium)
                        .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
                ) {
                    Text(data.visuals.message)
                }
            }
        }
        if (dock != null) Dock(dock)
    }
}

@Composable
private fun Dock(content: @Composable () -> Unit) {
    var width by remember { mutableStateOf(DOCK_INITIAL_WIDTH) }
    val density = LocalDensity.current
    val dragState = rememberDraggableState { delta ->
        width = (width - with(density) { delta.toDp() }).coerceIn(DOCK_MIN_WIDTH, DOCK_MAX_WIDTH)
    }
    Row(Modifier.fillMaxHeight()) {
        Box(
            modifier = Modifier.width(DOCK_HANDLE_WIDTH).fillMaxHeight().draggable(dragState, Orientation.Horizontal),
            contentAlignment = Alignment.Center,
        ) {
            Divider(orientation = DividerOrientation.Vertical)
        }
        Box(Modifier.width(width).fillMaxHeight().background(AppTheme.colors.surface)) { content() }
    }
}

private val DOCK_INITIAL_WIDTH = 380.dp
private val DOCK_MIN_WIDTH = 260.dp
private val DOCK_MAX_WIDTH = 720.dp
private val DOCK_HANDLE_WIDTH = 6.dp

@UiKitPreview
@Composable
private fun ScaffoldPreview() = PreviewContext {
    Scaffold(
        sidebar = { Sidebar(header = { Text("ADB Desktop") }, footer = {}) {} },
        messageHostState = rememberMessageHostState(),
    ) {
        TopBar(title = "Devices")
    }
}

@UiKitPreview
@Composable
private fun ScaffoldDockPreview() = PreviewContext {
    Scaffold(
        sidebar = { Sidebar(header = { Text("ADB Desktop") }, footer = {}) {} },
        messageHostState = rememberMessageHostState(),
        dock = { TopBar(title = "Mirror") },
    ) {
        TopBar(title = "Files")
    }
}
