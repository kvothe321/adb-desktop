package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * A glyph from [IconName]. Pass a [contentDescription] unless the icon is purely decorative.
 * `modifier` is for layout only.
 */
@Composable
fun Icon(
    icon: IconName,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: IconSize = IconSize.Medium,
    tone: Tone = Tone.Neutral,
) {
    IconGlyph(
        icon = icon,
        contentDescription = contentDescription,
        tint = if (tone == Tone.Neutral) AppTheme.colors.textSecondary else tone.color,
        modifier = modifier,
        size = size,
    )
}

@Composable
internal fun IconGlyph(
    icon: IconName,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier,
    size: IconSize = IconSize.Medium,
) {
    MaterialIcon(imageVector = icon.vector, contentDescription = contentDescription, modifier = modifier.size(size.dp), tint = tint)
}

@UiKitPreview
@Composable
private fun IconPreview() = PreviewContext {
    Row {
        IconName.entries.forEach { Icon(it, contentDescription = it.name) }
    }
}
