package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import com.tlpcraft.adbdesktop.uikit.theme.Elevation

/**
 * A phone around [content]: a dark bezel with rounded corners. The screen keeps [aspectRatio], width over height, and
 * the frame fits the space it is given. `modifier` is for layout only.
 */
@Composable
fun DeviceFrame(aspectRatio: Float, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .aspectRatio(aspectRatio, matchHeightConstraintsFirst = true)
            .shadow(Elevation.overlay, FRAME_SHAPE)
            .background(AppTheme.colors.deviceBezel, FRAME_SHAPE)
            .padding(BEZEL_WIDTH),
    ) {
        Box(Modifier.clip(SCREEN_SHAPE).background(AppTheme.colors.deviceScreen)) { content() }
    }
}

private val BEZEL_WIDTH = 7.dp
private val FRAME_SHAPE = RoundedCornerShape(34.dp)
private val SCREEN_SHAPE = RoundedCornerShape(27.dp)

private const val PREVIEW_ASPECT_RATIO = 0.46f

@UiKitPreview
@Composable
private fun DeviceFramePreview() = PreviewContext {
    DeviceFrame(aspectRatio = PREVIEW_ASPECT_RATIO, modifier = Modifier.size(width = 180.dp, height = 380.dp)) {}
}
