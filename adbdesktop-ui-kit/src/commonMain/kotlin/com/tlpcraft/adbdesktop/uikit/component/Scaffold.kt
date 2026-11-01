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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import com.tlpcraft.adbdesktop.uikit.theme.Elevation
import com.tlpcraft.adbdesktop.uikit.theme.LocalWindowWidth
import com.tlpcraft.adbdesktop.uikit.theme.WindowWidth

/**
 * Top-level window layout: a [topBar] above the main [content], with messages from [messageHostState] floating above
 * it. A [side] panel, when given, floats on the right as a card the user can widen by dragging its edge. It also
 * decides [AppTheme.windowWidth] from the room the window gives. `modifier` is for layout only.
 */
@Composable
fun Scaffold(
    topBar: @Composable () -> Unit,
    messageHostState: MessageHostState,
    modifier: Modifier = Modifier,
    side: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize().background(AppTheme.colors.background)) {
        val windowWidth = WindowWidth.of(maxWidth)
        val sideMaxWidth = maxWidth * SIDE_MAX_SHARE
        CompositionLocalProvider(LocalWindowWidth provides windowWidth) {
            Column(Modifier.fillMaxSize()) {
                topBar()
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        Column(Modifier.fillMaxSize()) { content() }
                        SnackbarHost(messageHostState.snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(AppTheme.spacing.xl)) { data ->
                            Toast(data.visuals.message)
                        }
                    }
                    if (side != null) SidePanel(maxWidth = sideMaxWidth, windowWidth = windowWidth, content = side)
                }
            }
        }
    }
}

/** A dark pill in light mode and a light one in dark mode, as system notices look. */
@Composable
private fun Toast(message: String) {
    Box(
        Modifier
            .widthIn(max = 520.dp)
            .shadow(Elevation.overlay, AppTheme.shapes.pill)
            .background(AppTheme.colors.textPrimary, AppTheme.shapes.pill)
            .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.sm),
    ) {
        InverseText(message)
    }
}

@Composable
private fun SidePanel(maxWidth: Dp, windowWidth: WindowWidth, content: @Composable () -> Unit) {
    val preferred = when (windowWidth) {
        WindowWidth.Compact -> SIDE_COMPACT_WIDTH
        WindowWidth.Medium -> SIDE_MEDIUM_WIDTH
        WindowWidth.Expanded -> SIDE_EXPANDED_WIDTH
    }
    var dragged by remember { mutableStateOf<Dp?>(null) }
    val width = (dragged ?: preferred).coerceIn(SIDE_MIN_WIDTH, maxWidth.coerceAtLeast(SIDE_MIN_WIDTH))
    val density = LocalDensity.current
    val dragState = rememberDraggableState { delta ->
        dragged = (width - with(density) { delta.toDp() }).coerceIn(SIDE_MIN_WIDTH, maxWidth.coerceAtLeast(SIDE_MIN_WIDTH))
    }
    Row(Modifier.fillMaxHeight().padding(bottom = AppTheme.spacing.lg)) {
        Box(
            Modifier
                .width(SIDE_HANDLE_WIDTH)
                .fillMaxHeight()
                .pointerHoverIcon(HorizontalResizePointer)
                .draggable(dragState, Orientation.Horizontal),
        )
        Box(
            Modifier
                .padding(end = AppTheme.spacing.lg)
                .width(width)
                .fillMaxHeight()
                .surfaceCard(AppTheme.colors, AppTheme.shapes.large)
                .clip(AppTheme.shapes.large),
        ) {
            content()
        }
    }
}

private const val SIDE_MAX_SHARE = 0.45f
private val SIDE_COMPACT_WIDTH = 280.dp
private val SIDE_MEDIUM_WIDTH = 320.dp
private val SIDE_EXPANDED_WIDTH = 380.dp
private val SIDE_MIN_WIDTH = 240.dp
private val SIDE_HANDLE_WIDTH = 8.dp

@UiKitPreview
@Composable
private fun ScaffoldPreview() = PreviewContext {
    Scaffold(topBar = { Text("ADB Desktop", variant = TextVariant.Title) }, messageHostState = rememberMessageHostState()) {
        TopBar(title = "Apps")
    }
}

@UiKitPreview
@Composable
private fun ScaffoldSidePreview() = PreviewContext {
    Scaffold(topBar = { Text("ADB Desktop", variant = TextVariant.Title) }, messageHostState = rememberMessageHostState(), side = { TopBar(title = "Phone") }) {
        TopBar(title = "Files")
    }
}
