package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.CircularProgressIndicator
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
import com.tlpcraft.adbdesktop.uikit.theme.AppColors
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * A labelled action. While [loading] it shows a spinner and ignores clicks.
 * `modifier` is for layout only.
 */
@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Secondary,
    size: ControlSize = ControlSize.Medium,
    icon: IconName? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val colors = AppTheme.colors.forButton(variant, hovered, enabled)
    val shape = AppTheme.shapes.medium

    Row(
        modifier = modifier
            .heightIn(min = size.height)
            .clip(shape)
            .background(colors.container)
            .border(1.dp, colors.border, shape)
            .interactive(interactionSource, enabled = enabled && !loading, role = Role.Button, onClick = onClick)
            .padding(horizontal = size.horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(IconSize.Small.dp), color = colors.content, strokeWidth = 2.dp)
        } else if (icon != null) {
            IconGlyph(icon, contentDescription = null, tint = colors.content, size = IconSize.Small)
        }
        BasicText(text = text, style = AppTheme.typography.label.copy(color = colors.content), maxLines = 1)
    }
}

private class ButtonColors(val container: Color, val content: Color, val border: Color)

private fun AppColors.forButton(variant: ButtonVariant, hovered: Boolean, enabled: Boolean): ButtonColors {
    if (!enabled) return ButtonColors(container = surfaceRaised, content = textDisabled, border = border)
    return when (variant) {
        ButtonVariant.Primary -> ButtonColors(
            container = if (hovered) accent.copy(alpha = HOVER_ALPHA) else accent,
            content = onAccent,
            border = Color.Transparent,
        )

        ButtonVariant.Secondary -> ButtonColors(container = if (hovered) surfaceHover else surface, content = textPrimary, border = border)

        ButtonVariant.Ghost -> ButtonColors(container = if (hovered) surfaceHover else Color.Transparent, content = textPrimary, border = Color.Transparent)

        ButtonVariant.Danger -> ButtonColors(
            container = if (hovered) error.copy(alpha = SUBTLE_ALPHA) else Color.Transparent,
            content = error,
            border = error.copy(alpha = BORDER_ALPHA),
        )
    }
}

private const val HOVER_ALPHA = 0.88f
private const val SUBTLE_ALPHA = 0.12f
private const val BORDER_ALPHA = 0.5f

@UiKitPreview
@Composable
private fun ButtonPreview() = PreviewContext {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
        ButtonVariant.entries.forEach { Button(text = it.name, onClick = {}, variant = it, icon = IconName.Add) }
        Button(text = "Small", onClick = {}, size = ControlSize.Small)
        Button(text = "Loading", onClick = {}, loading = true)
        Button(text = "Disabled", onClick = {}, enabled = false)
    }
}
