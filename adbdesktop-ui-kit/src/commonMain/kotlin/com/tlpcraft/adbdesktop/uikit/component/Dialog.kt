package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog as PlatformDialog
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import com.tlpcraft.adbdesktop.uikit.theme.Elevation

/**
 * A modal window with a title, free content and a right-aligned action row. Escape and clicking outside
 * call [onDismissRequest]. `modifier` is for layout only.
 */
@Composable
fun Dialog(
    title: String,
    onDismissRequest: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    PlatformDialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .width(420.dp)
                .shadow(Elevation.overlay, AppTheme.shapes.large)
                .background(AppTheme.colors.surface, AppTheme.shapes.large)
                .border(1.dp, AppTheme.colors.border, AppTheme.shapes.large)
                .padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
        ) {
            Text(title, variant = TextVariant.Title)
            content()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm, Alignment.End),
                content = actions,
            )
        }
    }
}

@UiKitPreview
@Composable
private fun DialogContentPreview() = PreviewContext {
    Dialog(
        title = "Connect over Wi-Fi",
        onDismissRequest = {},
        actions = {
            Button(text = "Cancel", onClick = {}, variant = ButtonVariant.Ghost)
            Button(text = "Connect", onClick = {}, variant = ButtonVariant.Primary)
        },
    ) {
        TextField(value = "", onValueChange = {}, label = "Address", placeholder = "192.168.1.42:5555")
    }
}
