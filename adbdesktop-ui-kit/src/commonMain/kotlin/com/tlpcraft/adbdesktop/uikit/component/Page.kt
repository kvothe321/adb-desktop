package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * A screen: a large [title] with [actions], and [content] below it, inset so it lines up with the title. Narrow
 * screens get tighter insets. `modifier` is for layout only.
 */
@Composable
fun Page(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val inset = pageInset(isNarrow = maxWidth < NARROW_PAGE_WIDTH)
        Column(Modifier.fillMaxSize()) {
            TopBar(title = title, actions = actions)
            Column(Modifier.weight(1f).fillMaxWidth().padding(start = inset, end = inset, bottom = inset), content = content)
        }
    }
}

/**
 * A card for lists, logs and other edge-to-edge content: rounded and lifted like [Card], without inner padding.
 * `modifier` is for layout only.
 */
@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = AppTheme.shapes.large
    Column(modifier.surfaceCard(AppTheme.colors, shape).clip(shape), content = content)
}

/** Shared with [TopBar], so a page's content and title start at the same edge. */
internal val NARROW_PAGE_WIDTH = 640.dp

@Composable
@ReadOnlyComposable
internal fun pageInset(isNarrow: Boolean): Dp = if (isNarrow) AppTheme.spacing.lg else AppTheme.spacing.xl

@UiKitPreview
@Composable
private fun PagePreview() = PreviewContext {
    Page(title = "Files", actions = { Button(text = "Upload", onClick = {}, variant = ButtonVariant.Primary) }) {
        Panel(Modifier.fillMaxSize()) {
            ListRow(headline = "Download", supporting = "/sdcard/Download")
        }
    }
}
