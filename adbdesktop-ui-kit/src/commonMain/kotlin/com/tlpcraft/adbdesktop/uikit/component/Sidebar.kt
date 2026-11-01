package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
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
import com.tlpcraft.adbdesktop.uikit.theme.WindowWidth

/**
 * Navigation column on the window background. [items] fills the top, [footer] sticks to the bottom. In a
 * [WindowWidth.Compact] window it folds into a rail of icons, and its items show their labels as tooltips.
 * `modifier` is for layout only.
 */
@Composable
fun Sidebar(
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    items: @Composable ColumnScope.() -> Unit,
) {
    val isRail = AppTheme.windowWidth == WindowWidth.Compact
    CompositionLocalProvider(LocalIsRail provides isRail) {
        Column(
            modifier = modifier
                .width(if (isRail) RAIL_WIDTH else SIDEBAR_WIDTH)
                .fillMaxHeight()
                .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.lg),
            horizontalAlignment = if (isRail) Alignment.CenterHorizontally else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
        ) {
            header()
            Spacer(Modifier.height(AppTheme.spacing.lg))
            items()
            Spacer(Modifier.weight(1f))
            footer()
        }
    }
}

/**
 * A navigation destination inside a [Sidebar]. [shortcut] is a hint such as `Ctrl+1`, shown on hover; the shortcut
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
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.medium
    val itemModifier = when {
        selected -> Modifier.surfaceCard(colors, shape)
        hovered -> Modifier.background(colors.surfaceHover, shape)
        else -> Modifier.background(Color.Transparent, shape)
    }
    val tone = if (selected) Tone.Accent else Tone.Neutral

    if (LocalIsRail.current) {
        Tooltip(listOfNotNull(label, shortcut).joinToString("  "), modifier) {
            Box(
                Modifier
                    .padding(vertical = AppTheme.spacing.xxs)
                    .then(itemModifier)
                    .clip(shape)
                    .interactive(interactionSource, enabled = true, role = Role.Tab, onClick = onClick)
                    .padding(RAIL_ITEM_PADDING),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = label, size = IconSize.Large, tone = tone)
            }
        }
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ITEM_HEIGHT)
            .then(itemModifier)
            .clip(shape)
            .interactive(interactionSource, enabled = true, role = Role.Tab, onClick = onClick)
            .padding(horizontal = AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        Icon(icon, contentDescription = null, tone = tone)
        Text(
            label,
            modifier = Modifier.weight(1f),
            variant = TextVariant.BodyStrong,
            emphasis = if (selected) TextEmphasis.High else TextEmphasis.Medium,
            maxLines = 1,
        )
        if (shortcut != null && hovered) Text(shortcut, variant = TextVariant.Caption, emphasis = TextEmphasis.Disabled)
    }
}

/** Whether the surrounding [Sidebar] is folded into a rail, so its header and footer can shrink too. */
@Composable
fun isSidebarRail(): Boolean = LocalIsRail.current

private val LocalIsRail = staticCompositionLocalOf { false }

private val SIDEBAR_WIDTH = 236.dp
private val RAIL_WIDTH = 76.dp
private val ITEM_HEIGHT = 38.dp
private val RAIL_ITEM_PADDING = 10.dp

@UiKitPreview
@Composable
private fun SidebarPreview() = PreviewContext {
    Sidebar(header = { Text("ADB Desktop", variant = TextVariant.Heading) }, footer = { Text("1.0.0", variant = TextVariant.Caption) }) {
        SidebarItem(label = "Devices", icon = IconName.Devices, selected = true, onClick = {}, shortcut = "Ctrl+1")
        SidebarItem(label = "Apps", icon = IconName.Package, selected = false, onClick = {}, shortcut = "Ctrl+2")
    }
}
