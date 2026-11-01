package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
 * A row for lists: a headline, optional supporting line and leading/trailing slots. Hover and selection show as a
 * rounded highlight inset from the edges. `modifier` is for layout only.
 */
@Composable
fun ListRow(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    monospaceSupporting: Boolean = false,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val background = when {
        selected -> AppTheme.colors.accentSubtle
        hovered && onClick != null -> AppTheme.colors.surfaceHover
        else -> Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.sm, vertical = 1.dp)
            .clip(AppTheme.shapes.medium)
            .background(background)
            .then(if (onClick != null) Modifier.interactive(interactionSource, enabled = true, role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f)) {
            Text(headline, variant = TextVariant.BodyStrong, maxLines = 1)
            if (supporting != null) {
                if (monospaceSupporting) {
                    MonospaceText(supporting, emphasis = TextEmphasis.Medium, maxLines = 1)
                } else {
                    Text(supporting, variant = TextVariant.Caption, emphasis = TextEmphasis.Medium, maxLines = 1)
                }
            }
        }
        trailing?.invoke()
    }
}

@UiKitPreview
@Composable
private fun ListRowPreview() = PreviewContext {
    Column {
        ListRow(headline = "Notes", supporting = "com.example.notes", monospaceSupporting = true, onClick = {})
        ListRow(
            headline = "Settings",
            supporting = "com.android.settings",
            selected = true,
            leading = { Icon(IconName.Package, contentDescription = null) },
            trailing = { Badge("System") },
        )
    }
}
