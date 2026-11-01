package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo

private const val BYTES_PER_PIXEL = 4

internal actual fun imageBitmapOfBgra(width: Int, height: Int, pixels: ByteArray): ImageBitmap = Image.makeRaster(
    imageInfo = ImageInfo(width, height, ColorType.BGRA_8888, ColorAlphaType.OPAQUE),
    bytes = pixels,
    rowBytes = width * BYTES_PER_PIXEL,
).toComposeImageBitmap()
