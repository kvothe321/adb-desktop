package com.tlpcraft.adbdesktop.core.data.source.adb.dto

/** A control message for the mirrored device. Codes, actions and meta states are Android's own `KeyEvent` and `MotionEvent` values. */
sealed interface MirrorControlDto {
    data class Key(val action: Int, val keyCode: Int, val metaState: Int) : MirrorControlDto

    data class Text(val text: String) : MirrorControlDto

    /** [x] and [y] are in a frame of [frameWidth] by [frameHeight]; the device drops positions in a size it no longer shows. */
    data class Touch(val action: Int, val x: Int, val y: Int, val frameWidth: Int, val frameHeight: Int) : MirrorControlDto

    data class Scroll(val x: Int, val y: Int, val frameWidth: Int, val frameHeight: Int, val horizontal: Float, val vertical: Float) : MirrorControlDto

    data class SetClipboard(val text: String, val paste: Boolean) : MirrorControlDto

    data class BackOrScreenOn(val action: Int) : MirrorControlDto

    data object RotateDevice : MirrorControlDto
}
