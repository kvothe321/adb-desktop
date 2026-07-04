package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/** The header of a content area: a title on the left, [actions] on the right. `modifier` is for layout only. */
@Composable
fun TopBar(title: String, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) {
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(AppTheme.colors.surface)
                .padding(horizontal = AppTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        ) {
            Text(title, modifier = Modifier.weight(1f), variant = TextVariant.Title, maxLines = 1)
            actions()
        }
        Divider()
    }
}

@UiKitPreview
@Composable
private fun TopBarPreview() = PreviewContext {
    TopBar(title = "Devices") {
        IconButton(IconName.Refresh, contentDescription = "Refresh", onClick = {})
        Button(text = "Connect", onClick = {}, variant = ButtonVariant.Primary, icon = IconName.Wifi)
    }
}
