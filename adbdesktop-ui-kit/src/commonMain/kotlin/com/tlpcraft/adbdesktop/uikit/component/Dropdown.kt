package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * Picks one of [items]. [placeholder] is shown while nothing is selected, and also as the only
 * menu entry when [items] is empty. `modifier` is for layout only.
 */
@Composable
fun <T> Dropdown(
    items: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    itemLabel: (T) -> String,
    placeholder: String,
    modifier: Modifier = Modifier,
    itemDetail: ((T) -> String)? = null,
    icon: IconName? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = AppTheme.shapes.medium
    val background = if (hovered) AppTheme.colors.surfaceHover else AppTheme.colors.surfaceRaised

    Box(modifier) {
        DropdownTrigger(
            label = selected?.let(itemLabel) ?: placeholder,
            detail = selected?.let { itemDetail?.invoke(it) },
            isPlaceholder = selected == null,
            icon = icon,
            modifier = Modifier
                .heightIn(min = ControlSize.Medium.height)
                .widthIn(min = 200.dp)
                .clip(shape)
                .background(background)
                .interactive(interactionSource, enabled = true, role = Role.DropdownList, onClick = { expanded = true })
                .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = AppTheme.shapes.medium,
            containerColor = AppTheme.colors.surface,
            border = BorderStroke(1.dp, AppTheme.colors.border),
        ) {
            if (items.isEmpty()) {
                DropdownMenuItem(text = { Text(placeholder, emphasis = TextEmphasis.Medium) }, onClick = { expanded = false }, enabled = false)
            }
            items.forEach { item ->
                DropdownMenuItem(
                    text = { DropdownItemText(itemLabel(item), itemDetail?.invoke(item)) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    },
                    trailingIcon = if (item == selected) {
                        { Icon(IconName.Check, contentDescription = null, size = IconSize.Small, tone = Tone.Accent) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun DropdownTrigger(
    label: String,
    detail: String?,
    isPlaceholder: Boolean,
    icon: IconName?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, size = IconSize.Large, tone = if (isPlaceholder) Tone.Neutral else Tone.Accent)
        Column(Modifier.weight(1f)) {
            Text(label, variant = TextVariant.BodyStrong, emphasis = if (isPlaceholder) TextEmphasis.Medium else TextEmphasis.High, maxLines = 1)
            if (detail != null) Text(detail, variant = TextVariant.Caption, emphasis = TextEmphasis.Medium, maxLines = 1)
        }
        Icon(IconName.ChevronDown, contentDescription = null, size = IconSize.Small)
    }
}

@Composable
private fun DropdownItemText(label: String, detail: String?) {
    Column {
        Text(label, maxLines = 1)
        if (detail != null) MonospaceText(detail, emphasis = TextEmphasis.Medium, maxLines = 1)
    }
}

@UiKitPreview
@Composable
private fun DropdownPreview() = PreviewContext {
    Dropdown(
        items = listOf("Pixel 8", "Galaxy A52"),
        selected = "Pixel 8",
        onSelect = {},
        itemLabel = { it },
        placeholder = "Select a device",
    )
}
