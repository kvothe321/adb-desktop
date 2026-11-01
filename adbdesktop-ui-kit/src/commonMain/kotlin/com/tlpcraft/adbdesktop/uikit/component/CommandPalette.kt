package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog as PlatformDialog
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import com.tlpcraft.adbdesktop.uikit.theme.Elevation

/**
 * A search box over every command: typing narrows the list, Up and Down move the highlight, Enter runs it. Escape or
 * a click outside calls [onDismissRequest]. `modifier` is for layout only.
 */
@Composable
fun CommandPalette(
    commands: List<Command>,
    onSelect: (Command) -> Unit,
    onDismissRequest: () -> Unit,
    placeholder: String,
    emptyText: String,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var highlighted by remember { mutableIntStateOf(0) }
    val matches = remember(commands, query) { commands.filter { it.matches(query) } }
    val listState = rememberLazyListState()
    LaunchedEffect(matches) { highlighted = 0 }
    LaunchedEffect(highlighted) { if (matches.isNotEmpty()) listState.animateScrollToItem(highlighted) }

    PlatformDialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .width(PALETTE_WIDTH)
                .shadow(Elevation.overlay, PALETTE_SHAPE)
                .background(AppTheme.colors.surface, PALETTE_SHAPE)
                .border(1.dp, AppTheme.colors.border, PALETTE_SHAPE)
                .padding(AppTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = placeholder,
                leadingIcon = IconName.Search,
                autoFocus = true,
                onSubmit = { matches.getOrNull(highlighted)?.let(onSelect) },
                onArrowUp = { highlighted = (highlighted - 1).coerceAtLeast(0) },
                onArrowDown = { highlighted = (highlighted + 1).coerceAtMost(matches.lastIndex.coerceAtLeast(0)) },
            )
            if (matches.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(AppTheme.spacing.xl), contentAlignment = Alignment.Center) {
                    Text(emptyText, emphasis = TextEmphasis.Medium)
                }
            } else {
                LazyColumn(Modifier.heightIn(max = LIST_MAX_HEIGHT), state = listState) {
                    itemsIndexed(matches, key = { _, command -> command.id }) { index, command ->
                        val isNewGroup = index == 0 || matches[index - 1].group != command.group
                        if (isNewGroup) GroupHeader(command.group)
                        CommandRow(command = command, isHighlighted = index == highlighted, onClick = { onSelect(command) })
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String) {
    Text(
        title,
        modifier = Modifier.padding(start = AppTheme.spacing.md, top = AppTheme.spacing.md, bottom = AppTheme.spacing.xs),
        variant = TextVariant.Caption,
        emphasis = TextEmphasis.Medium,
    )
}

@Composable
private fun CommandRow(command: Command, isHighlighted: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.medium)
            .background(if (isHighlighted) AppTheme.colors.accentSubtle else AppTheme.colors.surface)
            .interactive(interactionSource, enabled = true, role = Role.Button, onClick = onClick)
            .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        Icon(command.icon, contentDescription = null, tone = if (isHighlighted) Tone.Accent else Tone.Neutral)
        Text(command.title, modifier = Modifier.weight(1f), variant = TextVariant.BodyStrong, maxLines = 1)
        command.hint?.let { Text(it, variant = TextVariant.Caption, emphasis = TextEmphasis.Medium, maxLines = 1) }
    }
}

/** Every word of [query] must start a word of the title or group, so short prefixes find commands quickly. */
private fun Command.matches(query: String): Boolean {
    val words = "$title $group".lowercase().split(' ', '-')
    return query.lowercase().split(' ').filter { it.isNotBlank() }.all { part -> words.any { it.startsWith(part) } }
}

private val PALETTE_WIDTH = 560.dp
private val PALETTE_SHAPE = RoundedCornerShape(20.dp)
private val LIST_MAX_HEIGHT = 420.dp

@UiKitPreview
@Composable
private fun CommandPalettePreview() = PreviewContext {
    CommandPalette(
        commands = listOf(
            Command("apps", "Apps", IconName.Package, "Go to", hint = "Ctrl+2"),
            Command("files", "Files", IconName.Folder, "Go to", hint = "Ctrl+3"),
            Command("theme", "Switch to dark theme", IconName.Moon, "App"),
        ),
        onSelect = {},
        onDismissRequest = {},
        placeholder = "Search commands and devices",
        emptyText = "Nothing matches",
    )
}
