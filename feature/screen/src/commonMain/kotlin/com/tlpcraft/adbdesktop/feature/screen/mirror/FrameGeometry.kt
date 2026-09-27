package com.tlpcraft.adbdesktop.feature.screen.mirror

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.tlpcraft.adbdesktop.core.domain.model.MirrorPoint
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Where [position] in a box of [boxSize] lands on a frame drawn fitted and centred in it. Outside the frame it is `null`, or
 * the nearest edge when [isClamped], so a drag that leaves the picture still ends on the device.
 */
internal fun framePointAt(
    position: Offset,
    boxSize: IntSize,
    frameWidth: Int,
    frameHeight: Int,
    isClamped: Boolean,
): MirrorPoint? {
    if (boxSize == IntSize.Zero || frameWidth == 0 || frameHeight == 0) return null
    val scale = min(boxSize.width.toFloat() / frameWidth, boxSize.height.toFloat() / frameHeight)
    val x = ((position.x - (boxSize.width - frameWidth * scale) / 2) / scale).roundToInt()
    val y = ((position.y - (boxSize.height - frameHeight * scale) / 2) / scale).roundToInt()
    val isInside = x in 0 until frameWidth && y in 0 until frameHeight
    return when {
        isInside -> MirrorPoint(x, y, frameWidth, frameHeight)
        isClamped -> MirrorPoint(x.coerceIn(0, frameWidth - 1), y.coerceIn(0, frameHeight - 1), frameWidth, frameHeight)
        else -> null
    }
}
