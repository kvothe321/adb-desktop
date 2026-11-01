package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/** A glyph on a soft rounded square, the way list items and cards show what they are. `modifier` is for layout only. */
@Composable
fun IconTile(icon: IconName, modifier: Modifier = Modifier, tone: Tone = Tone.Neutral) {
    val background = if (tone == Tone.Neutral) AppTheme.colors.surfaceRaised else tone.color.copy(alpha = TINT_ALPHA)
    Box(modifier.size(TILE_SIZE).background(background, AppTheme.shapes.medium), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, size = IconSize.Large, tone = tone)
    }
}

private val TILE_SIZE = 40.dp
private const val TINT_ALPHA = 0.12f

@UiKitPreview
@Composable
private fun IconTilePreview() = PreviewContext {
    Row {
        IconTile(IconName.Devices, tone = Tone.Accent)
        IconTile(IconName.Folder)
    }
}
