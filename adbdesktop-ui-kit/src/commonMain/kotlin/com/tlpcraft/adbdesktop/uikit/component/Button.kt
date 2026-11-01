package com.tlpcraft.adbdesktop.uikit.component

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
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
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
import com.tlpcraft.adbdesktop.uikit.theme.AppColors
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * A labelled, pill-shaped action. While [loading] it shows a spinner and ignores clicks. In a narrow [TopBar] a
 * button with an [icon] shows only the icon and keeps [text] as its tooltip. `modifier` is for layout only.
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
    val shape = AppTheme.shapes.pill
    val isIconOnly = icon != null && LocalIconOnlyButtons.current
    val surface = Modifier
        .heightIn(min = size.height)
        .clip(shape)
        .background(colors.container)
        .interactive(interactionSource, enabled = enabled && !loading, role = Role.Button, onClick = onClick)

    if (isIconOnly) {
        Tooltip(text, modifier) {
            Box(surface.size(size.height), contentAlignment = Alignment.Center) {
                ButtonGlyph(icon = icon, loading = loading, color = colors.content)
            }
        }
        return
    }

    Row(
        modifier = modifier.then(surface).padding(horizontal = size.horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading || icon != null) ButtonGlyph(icon = icon, loading = loading, color = colors.content)
        BasicText(text = text, style = AppTheme.typography.label.copy(color = colors.content), maxLines = 1)
    }
}

@Composable
private fun ButtonGlyph(icon: IconName?, loading: Boolean, color: Color) {
    when {
        loading -> CircularProgressIndicator(modifier = Modifier.size(IconSize.Small.dp), color = color, strokeWidth = 2.dp)
        icon != null -> IconGlyph(icon, contentDescription = null, tint = color, size = IconSize.Medium)
    }
}

/** Set by a narrow [TopBar], so its buttons save room. */
internal val LocalIconOnlyButtons = staticCompositionLocalOf { false }

private class ButtonColors(val container: Color, val content: Color)

private fun AppColors.forButton(variant: ButtonVariant, hovered: Boolean, enabled: Boolean): ButtonColors {
    if (!enabled) return ButtonColors(container = surfaceRaised, content = textDisabled)
    return when (variant) {
        ButtonVariant.Primary -> ButtonColors(container = if (hovered) accent.copy(alpha = HOVER_ALPHA) else accent, content = onAccent)
        ButtonVariant.Secondary -> ButtonColors(container = if (hovered) surfaceHover else surfaceRaised, content = textPrimary)
        ButtonVariant.Ghost -> ButtonColors(container = if (hovered) surfaceHover else Color.Transparent, content = textPrimary)
        ButtonVariant.Danger -> ButtonColors(container = error.copy(alpha = if (hovered) DANGER_HOVER_ALPHA else DANGER_ALPHA), content = error)
    }
}

private const val HOVER_ALPHA = 0.88f
private const val DANGER_ALPHA = 0.1f
private const val DANGER_HOVER_ALPHA = 0.18f

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
