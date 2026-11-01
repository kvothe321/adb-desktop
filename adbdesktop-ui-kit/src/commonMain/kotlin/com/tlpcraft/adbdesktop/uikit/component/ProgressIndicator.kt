package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * Shows activity, or a value between 0 and 1 when [progress] is set. [tone] colors the indicator, so a
 * linear bar doubles as a meter. `modifier` is for layout only; linear bars fill the width they are given.
 */
@Composable
fun ProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    style: ProgressStyle = ProgressStyle.Circular,
    tone: Tone = Tone.Accent,
) {
    val color = tone.color
    val track = AppTheme.colors.surfaceRaised
    when (style) {
        ProgressStyle.Circular -> {
            if (progress == null) {
                CircularProgressIndicator(modifier.size(24.dp), color = color, trackColor = track, strokeWidth = 2.dp)
            } else {
                CircularProgressIndicator({ progress.coerceIn(0f, 1f) }, modifier.size(24.dp), color = color, trackColor = track, strokeWidth = 2.dp)
            }
        }

        ProgressStyle.Linear -> {
            val barModifier = modifier.height(4.dp).clip(AppTheme.shapes.pill)
            if (progress == null) {
                LinearProgressIndicator(barModifier, color = color, trackColor = track, strokeCap = StrokeCap.Round)
            } else {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = barModifier,
                    color = color,
                    trackColor = track,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
            }
        }
    }
}

@UiKitPreview
@Composable
private fun ProgressIndicatorPreview() = PreviewContext {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
        ProgressIndicator()
        ProgressIndicator(Modifier.width(120.dp), progress = 0.7f, style = ProgressStyle.Linear, tone = Tone.Success)
        ProgressIndicator(Modifier.width(120.dp), progress = 0.15f, style = ProgressStyle.Linear, tone = Tone.Error)
    }
}
