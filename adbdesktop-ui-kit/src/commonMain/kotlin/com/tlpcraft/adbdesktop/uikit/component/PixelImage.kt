package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview

/** Shows raw BGRA [pixels], top row first, scaled to fit. Meant for images that change often, like video frames. `modifier` is for layout only. */
@Composable
fun PixelImage(
    width: Int,
    height: Int,
    pixels: ByteArray,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val bitmap = remember(pixels) { imageBitmapOfBgra(width, height, pixels) }
    Image(bitmap, contentDescription, modifier)
}

internal expect fun imageBitmapOfBgra(width: Int, height: Int, pixels: ByteArray): ImageBitmap

@UiKitPreview
@Composable
private fun PixelImagePreview() = PreviewContext {
    PixelImage(width = 2, height = 1, pixels = byteArrayOf(-1, 0, 0, -1, 0, 0, -1, -1), contentDescription = null, modifier = Modifier.size(80.dp))
}
