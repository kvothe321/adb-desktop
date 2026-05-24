package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import com.tlpcraft.adbdesktop.uikit.theme.Elevation

/**
 * Top-level window layout: a [sidebar] beside the main [content], with messages from [messageHostState]
 * floating above it. `modifier` is for layout only.
 */
@Composable
fun Scaffold(
    sidebar: @Composable () -> Unit,
    messageHostState: MessageHostState,
    modifier: Modifier = Modifier,
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
    }
}

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
