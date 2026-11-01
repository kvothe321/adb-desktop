package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * A device's screen as a plain rectangle, so nothing it shows is cut off. It keeps [aspectRatio], width over height,
 * and fits the space it is given. `modifier` is for layout only.
 */
@Composable
fun DeviceScreen(aspectRatio: Float, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.aspectRatio(aspectRatio, matchHeightConstraintsFirst = true).background(AppTheme.colors.deviceScreen)) {
        content()
    }
}

private const val PREVIEW_ASPECT_RATIO = 0.46f

@UiKitPreview
@Composable
private fun DeviceScreenPreview() = PreviewContext {
    DeviceScreen(aspectRatio = PREVIEW_ASPECT_RATIO, modifier = Modifier.size(width = 180.dp, height = 380.dp)) {}
}
