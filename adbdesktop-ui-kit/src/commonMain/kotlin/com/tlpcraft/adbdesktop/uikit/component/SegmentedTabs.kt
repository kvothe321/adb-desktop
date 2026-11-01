package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * A segmented control: a soft track with the selected segment raised on it. [selectedIndex] may point at no segment.
 * With [isIconOnly] each segment shows its icon and keeps the label as a tooltip. `modifier` is for layout only.
 */
@Composable
fun SegmentedTabs(
    items: List<TabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isIconOnly: Boolean = false,
) {
    Row(
        modifier = modifier
            .clip(AppTheme.shapes.pill)
            .background(AppTheme.colors.surfaceRaised)
            .padding(TRACK_PADDING),
        horizontalArrangement = Arrangement.spacedBy(TRACK_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            Segment(item = item, isSelected = index == selectedIndex, isIconOnly = isIconOnly, onClick = { onSelect(index) })
        }
    }
}

@Composable
private fun Segment(
    item: TabItem,
    isSelected: Boolean,
    isIconOnly: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = AppTheme.shapes.pill
    val emphasis = if (isSelected || hovered) TextEmphasis.High else TextEmphasis.Medium
    val segment = Modifier
        .heightIn(min = SEGMENT_HEIGHT)
        .then(if (isSelected) Modifier.surfaceCard(AppTheme.colors, shape) else Modifier)
        .clip(shape)
        .interactive(interactionSource, enabled = true, role = Role.Tab, onClick = onClick)

    Tooltip(listOfNotNull(item.label.takeIf { isIconOnly }, item.hint).joinToString("  ").ifEmpty { item.label }) {
        Row(
            modifier = segment.padding(horizontal = if (isIconOnly) AppTheme.spacing.sm else AppTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(item.icon, contentDescription = item.label.takeIf { isIconOnly }, tone = if (isSelected) Tone.Accent else Tone.Neutral)
            if (!isIconOnly) Text(item.label, variant = TextVariant.Label, emphasis = emphasis, maxLines = 1)
        }
    }
}

private val TRACK_PADDING = 3.dp
private val SEGMENT_HEIGHT = 32.dp

@UiKitPreview
@Composable
private fun SegmentedTabsPreview() = PreviewContext {
    SegmentedTabs(
        items = listOf(TabItem("Overview", IconName.Devices), TabItem("Apps", IconName.Package), TabItem("Files", IconName.Folder)),
        selectedIndex = 1,
        onSelect = {},
    )
}
