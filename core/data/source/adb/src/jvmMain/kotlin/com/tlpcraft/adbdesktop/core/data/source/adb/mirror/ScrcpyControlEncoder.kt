package com.tlpcraft.adbdesktop.core.data.source.adb.mirror

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MirrorControlDto
import java.nio.ByteBuffer

/** Writes control messages the way scrcpy's server reads them: a type byte, then big endian fields. */
internal object ScrcpyControlEncoder {
    private const val TYPE_INJECT_KEYCODE = 0
    private const val TYPE_INJECT_TEXT = 1
    private const val TYPE_INJECT_TOUCH = 2
    private const val TYPE_INJECT_SCROLL = 3
    private const val TYPE_BACK_OR_SCREEN_ON = 4
    private const val TYPE_SET_CLIPBOARD = 9
    private const val TYPE_ROTATE_DEVICE = 11
    private const val POSITION_BYTES = 2 * Int.SIZE_BYTES + 2 * Short.SIZE_BYTES

    /** A finger rather than a mouse, so taps and drags act as touches everywhere. */
    private const val POINTER_ID_GENERIC_FINGER = -2L
    private const val ACTION_UP = 1
    private const val FULL_PRESSURE = 0xFFFF
    private const val NO_SEQUENCE = 0L

    /** The server reads scroll amounts as 16 bit fixed point over -16..16 notches. */
    private const val SCROLL_RANGE = 16f
    private const val I16_SCALE = 0x8000
    private const val TEXT_MAX_BYTES = 300
    private const val CLIPBOARD_MAX_BYTES = (1 shl 18) - 14
    private const val UTF8_CONTINUATION_MASK = 0xC0
    private const val UTF8_CONTINUATION = 0x80

    fun encode(message: MirrorControlDto): ByteArray = when (message) {
        is MirrorControlDto.Key -> {
            message(TYPE_INJECT_KEYCODE, Byte.SIZE_BYTES + Int.SIZE_BYTES + Int.SIZE_BYTES + Int.SIZE_BYTES) {
                put(message.action.toByte())
                putInt(message.keyCode)
                putInt(0)
                putInt(message.metaState)
            }
        }

        is MirrorControlDto.Text -> {
            val text = message.text.encodeToByteArray().truncatedUtf8(TEXT_MAX_BYTES)
            message(TYPE_INJECT_TEXT, Int.SIZE_BYTES + text.size) {
                putInt(text.size)
                put(text)
            }
        }

        is MirrorControlDto.Touch -> {
            message(TYPE_INJECT_TOUCH, Byte.SIZE_BYTES + Long.SIZE_BYTES + POSITION_BYTES + Short.SIZE_BYTES + 2 * Int.SIZE_BYTES) {
                put(message.action.toByte())
                putLong(POINTER_ID_GENERIC_FINGER)
                putPosition(message.x, message.y, message.frameWidth, message.frameHeight)
                putShort((if (message.action == ACTION_UP) 0 else FULL_PRESSURE).toShort())
                putInt(0)
                putInt(0)
            }
        }

        is MirrorControlDto.Scroll -> {
            message(TYPE_INJECT_SCROLL, POSITION_BYTES + 2 * Short.SIZE_BYTES + Int.SIZE_BYTES) {
                putPosition(message.x, message.y, message.frameWidth, message.frameHeight)
                putShort(message.horizontal.toScrollFixedPoint())
                putShort(message.vertical.toScrollFixedPoint())
                putInt(0)
            }
        }

        is MirrorControlDto.SetClipboard -> {
            val text = message.text.encodeToByteArray().truncatedUtf8(CLIPBOARD_MAX_BYTES)
            message(TYPE_SET_CLIPBOARD, Long.SIZE_BYTES + Byte.SIZE_BYTES + Int.SIZE_BYTES + text.size) {
                putLong(NO_SEQUENCE)
                put(if (message.paste) 1 else 0)
                putInt(text.size)
                put(text)
            }
        }

        is MirrorControlDto.BackOrScreenOn -> {
            message(TYPE_BACK_OR_SCREEN_ON, Byte.SIZE_BYTES) { put(message.action.toByte()) }
        }

        MirrorControlDto.RotateDevice -> {
            message(TYPE_ROTATE_DEVICE, 0) {}
        }
    }

    private inline fun message(type: Int, payloadSize: Int, write: ByteBuffer.() -> Unit): ByteArray =
        ByteBuffer.allocate(Byte.SIZE_BYTES + payloadSize).put(type.toByte()).apply(write).array()

    private fun ByteBuffer.putPosition(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        putInt(x)
        putInt(y)
        putShort(width.toShort())
        putShort(height.toShort())
    }

    private fun Float.toScrollFixedPoint(): Short = (this / SCROLL_RANGE * I16_SCALE).toInt().coerceIn(-I16_SCALE, I16_SCALE - 1).toShort()

    /** Cuts at a character boundary, so the device never sees half a character. */
    private fun ByteArray.truncatedUtf8(maxBytes: Int): ByteArray {
        if (size <= maxBytes) return this
        var end = maxBytes
        while (end > 0 && this[end].isUtf8Continuation()) end--
        return copyOf(end)
    }

    private fun Byte.isUtf8Continuation(): Boolean = toInt() and UTF8_CONTINUATION_MASK == UTF8_CONTINUATION
}
