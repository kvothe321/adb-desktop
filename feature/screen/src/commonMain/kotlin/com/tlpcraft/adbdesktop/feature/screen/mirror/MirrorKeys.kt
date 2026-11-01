package com.tlpcraft.adbdesktop.feature.screen.mirror

import androidx.compose.ui.input.key.Key
import com.tlpcraft.adbdesktop.core.domain.model.DeviceKey
import com.tlpcraft.adbdesktop.core.domain.model.KeyAction
import com.tlpcraft.adbdesktop.core.domain.model.KeyModifier
import com.tlpcraft.adbdesktop.core.domain.model.MirrorInput

internal data class MirrorKeyPress(
    val key: Key,
    val codePoint: Int,
    val isDown: Boolean,
    val isCtrl: Boolean = false,
    val isAlt: Boolean = false,
    val isShift: Boolean = false,
    val isMeta: Boolean = false,
)

internal sealed interface MirrorKeyResult {
    data class Send(val input: MirrorInput) : MirrorKeyResult

    data object Paste : MirrorKeyResult

    data object Ignored : MirrorKeyResult
}

/**
 * Editing and navigation keys go to the device as keys, so do shortcuts like Ctrl+A. Everything else that types a character
 * goes as text, so the device keyboard layout cannot change what arrives. Ctrl+V (Cmd+V) pastes the computer's clipboard.
 */
internal fun MirrorKeyPress.toMirrorKey(): MirrorKeyResult {
    val action = if (isDown) KeyAction.Down else KeyAction.Up
    val isShortcut = isCtrl || isAlt || isMeta
    val shortcutKey = shortcutKeys[key]
    return when {
        isDown && (isCtrl || isMeta) && key == Key.V -> MirrorKeyResult.Paste
        key in editingKeys -> MirrorKeyResult.Send(MirrorInput.Key(action, editingKeys.getValue(key), modifiers()))
        isShortcut && shortcutKey != null -> MirrorKeyResult.Send(MirrorInput.Key(action, shortcutKey, modifiers()))
        isDown && !isShortcut && codePoint.isTypedCharacter() -> MirrorKeyResult.Send(MirrorInput.Text(codePointToString(codePoint)))
        else -> MirrorKeyResult.Ignored
    }
}

private fun MirrorKeyPress.modifiers(): Set<KeyModifier> = buildSet {
    if (isShift) add(KeyModifier.Shift)
    if (isCtrl) add(KeyModifier.Ctrl)
    if (isAlt) add(KeyModifier.Alt)
    if (isMeta) add(KeyModifier.Meta)
}

private const val FIRST_PRINTABLE = 0x20
private const val DELETE_CHARACTER = 0x7F
private const val FIRST_SUPPLEMENTARY = 0x10000
private const val SURROGATE_SHIFT = 10
private const val HIGH_SURROGATE_START = 0xD800
private const val LOW_SURROGATE_START = 0xDC00
private const val SURROGATE_MASK = 0x3FF

private fun Int.isTypedCharacter(): Boolean = this >= FIRST_PRINTABLE && this != DELETE_CHARACTER

private fun codePointToString(codePoint: Int): String {
    if (codePoint < FIRST_SUPPLEMENTARY) return codePoint.toChar().toString()
    val offset = codePoint - FIRST_SUPPLEMENTARY
    return charArrayOf(
        (HIGH_SURROGATE_START + (offset shr SURROGATE_SHIFT)).toChar(),
        (LOW_SURROGATE_START + (offset and SURROGATE_MASK)).toChar(),
    ).concatToString()
}

private val editingKeys: Map<Key, DeviceKey> = mapOf(
    Key.Enter to DeviceKey.Enter,
    Key.NumPadEnter to DeviceKey.Enter,
    Key.Backspace to DeviceKey.Backspace,
    Key.Delete to DeviceKey.ForwardDelete,
    Key.Tab to DeviceKey.Tab,
    Key.Escape to DeviceKey.Escape,
    Key.DirectionUp to DeviceKey.Up,
    Key.DirectionDown to DeviceKey.Down,
    Key.DirectionLeft to DeviceKey.Left,
    Key.DirectionRight to DeviceKey.Right,
    Key.MoveHome to DeviceKey.MoveHome,
    Key.MoveEnd to DeviceKey.MoveEnd,
    Key.PageUp to DeviceKey.PageUp,
    Key.PageDown to DeviceKey.PageDown,
    Key.Insert to DeviceKey.Insert,
)

private val letterKeys = listOf(
    Key.A,
    Key.B,
    Key.C,
    Key.D,
    Key.E,
    Key.F,
    Key.G,
    Key.H,
    Key.I,
    Key.J,
    Key.K,
    Key.L,
    Key.M,
    Key.N,
    Key.O,
    Key.P,
    Key.Q,
    Key.R,
    Key.S,
    Key.T,
    Key.U,
    Key.V,
    Key.W,
    Key.X,
    Key.Y,
    Key.Z,
)

private val digitKeys = listOf(Key.Zero, Key.One, Key.Two, Key.Three, Key.Four, Key.Five, Key.Six, Key.Seven, Key.Eight, Key.Nine)

private val shortcutKeys: Map<Key, DeviceKey> =
    letterKeys.zip(DeviceKey.entries.subList(DeviceKey.A.ordinal, DeviceKey.Z.ordinal + 1)).toMap() +
        digitKeys.zip(DeviceKey.entries.subList(DeviceKey.Digit0.ordinal, DeviceKey.Digit9.ordinal + 1)).toMap() +
        (Key.Spacebar to DeviceKey.Space)
