package com.tlpcraft.adbdesktop.uikit.component

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Android only renders IDE previews of this app, so plain and simple beats fast here. */
internal actual fun imageBitmapOfBgra(width: Int, height: Int, pixels: ByteArray): ImageBitmap {
    val argb = IntArray(width * height).also { ByteBuffer.wrap(pixels).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(it) }
    return Bitmap.createBitmap(argb, width, height, Bitmap.Config.ARGB_8888).asImageBitmap()
}
