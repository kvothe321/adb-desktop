package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * Fixed-width navigation column. [items] fills the top, [footer] sticks to the bottom.
 * `modifier` is for layout only.
 */
@Composable
fun Sidebar(
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    items: @Composable ColumnScope.() -> Unit,
) {
    Row(modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
                .background(AppTheme.colors.surface)
                .padding(AppTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
        ) {
            header()
            Spacer(Modifier.padding(top = AppTheme.spacing.sm))
            items()
            Spacer(Modifier.weight(1f))
            footer()
        }
        Divider(orientation = DividerOrientation.Vertical)
    }
}

/**
 * A navigation destination inside a [Sidebar]. [shortcut] is a hint such as `Ctrl+1`; the shortcut
 * itself is handled by the window. `modifier` is for layout only.
 */
@Composable
fun SidebarItem(
    label: String,
    icon: IconName,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shortcut: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val background = when {
        selected -> AppTheme.colors.accentSubtle
        hovered -> AppTheme.colors.surfaceHover
        else -> Color.Transparent
    }
    val tone = if (selected) Tone.Accent else Tone.Neutral

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ControlSize.Medium.height)
            .clip(AppTheme.shapes.medium)
            .background(background)
            .interactive(interactionSource, enabled = true, role = Role.Tab, onClick = onClick)
            .padding(horizontal = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        Icon(icon, contentDescription = null, tone = tone)
        Text(label, modifier = Modifier.weight(1f), variant = TextVariant.BodyStrong, tone = tone, maxLines = 1)
        if (shortcut != null) Text(shortcut, variant = TextVariant.Caption, emphasis = TextEmphasis.Disabled)
    }
}

@UiKitPreview
@Composable
private fun SidebarPreview() = PreviewContext {
    Sidebar(header = { Text("ADB Desktop", variant = TextVariant.Heading) }, footer = { Text("1.0.0", variant = TextVariant.Caption) }) {
        SidebarItem(label = "Devices", icon = IconName.Devices, selected = true, onClick = {}, shortcut = "Ctrl+1")
        SidebarItem(label = "Apps", icon = IconName.Package, selected = false, onClick = {}, shortcut = "Ctrl+2")
    }
}
