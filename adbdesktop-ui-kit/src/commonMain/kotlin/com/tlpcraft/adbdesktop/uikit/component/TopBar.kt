package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * The header of a content area: a large title on the left, [actions] on the right. When it gets narrow the title
 * shrinks and buttons with an icon show only the icon, with their text as a tooltip. `modifier` is for layout only.
 */
@Composable
fun TopBar(title: String, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val isNarrow = maxWidth < NARROW_WIDTH
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (isNarrow) NARROW_HEIGHT else HEIGHT)
                .padding(horizontal = AppTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        ) {
            Text(title, modifier = Modifier.weight(1f), variant = if (isNarrow) TextVariant.Title else TextVariant.Display, maxLines = 1)
            CompositionLocalProvider(LocalIconOnlyButtons provides isNarrow) { actions() }
        }
    }
}

private val NARROW_WIDTH = 640.dp
private val HEIGHT = 56.dp
private val NARROW_HEIGHT = 48.dp

@UiKitPreview
@Composable
private fun TopBarPreview() = PreviewContext {
    TopBar(title = "Devices") {
        IconButton(IconName.Refresh, contentDescription = "Refresh", onClick = {})
        Button(text = "Connect", onClick = {}, variant = ButtonVariant.Primary, icon = IconName.Wifi)
    }
}
