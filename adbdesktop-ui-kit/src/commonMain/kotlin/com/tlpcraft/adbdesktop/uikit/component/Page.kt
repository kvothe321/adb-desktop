package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * A screen: a [title] with [actions], and [content] below it reaching the window edges. Content that needs a margin adds
 * its own. `modifier` is for layout only.
 */
@Composable
fun Page(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        TopBar(title = title, actions = actions)
        Column(Modifier.weight(1f).fillMaxWidth(), content = content)
    }
}

/**
 * An edge-to-edge surface for lists, logs and terminals: square, flat and set off by a hairline above it.
 * `modifier` is for layout only.
 */
@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.background(AppTheme.colors.surface)) {
        Divider()
        content()
    }
}

@UiKitPreview
@Composable
private fun PagePreview() = PreviewContext {
    Page(title = "Files", actions = { Button(text = "Upload", onClick = {}, variant = ButtonVariant.Primary) }) {
        Panel(Modifier.fillMaxSize()) {
            ListRow(headline = "Download", supporting = "/sdcard/Download")
        }
    }
}
