package com.tlpcraft.adbdesktop.feature.shell.terminal

import androidx.compose.ui.input.key.Key

private const val ESCAPE = "\u001b"
private const val CONTROL_OFFSET = 1
private const val FIRST_TEXT_CODE = 0x20
private const val DELETE_CODE = 0x7F
private const val LAST_TEXT_CODE = 0xFFFD

private val LETTERS = listOf(
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

private val CONTROL_SYMBOLS = mapOf(
    Key.Spacebar to "\u0000",
    Key.LeftBracket to ESCAPE,
    Key.Backslash to "\u001c",
    Key.RightBracket to "\u001d",
)

private val CURSOR_KEYS = mapOf(Key.DirectionUp to 'A', Key.DirectionDown to 'B', Key.DirectionRight to 'C', Key.DirectionLeft to 'D')

private val SPECIAL_KEYS = mapOf(
    Key.Enter to "\r",
    Key.NumPadEnter to "\r",
    Key.Backspace to "\u007f",
    Key.Tab to "\t",
    Key.Escape to ESCAPE,
    Key.MoveHome to "$ESCAPE[H",
    Key.MoveEnd to "$ESCAPE[F",
    Key.Insert to "$ESCAPE[2~",
    Key.Delete to "$ESCAPE[3~",
    Key.PageUp to "$ESCAPE[5~",
    Key.PageDown to "$ESCAPE[6~",
    Key.F1 to "${ESCAPE}OP",
    Key.F2 to "${ESCAPE}OQ",
    Key.F3 to "${ESCAPE}OR",
    Key.F4 to "${ESCAPE}OS",
    Key.F5 to "$ESCAPE[15~",
    Key.F6 to "$ESCAPE[17~",
    Key.F7 to "$ESCAPE[18~",
    Key.F8 to "$ESCAPE[19~",
    Key.F9 to "$ESCAPE[20~",
    Key.F10 to "$ESCAPE[21~",
    Key.F11 to "$ESCAPE[23~",
    Key.F12 to "$ESCAPE[24~",
)

/** One key press, as the keyboard layout produced it. */
internal data class KeyPress(
    val key: Key,
    val codePoint: Int,
    val isCtrl: Boolean = false,
    val isAlt: Boolean = false,
    val isShift: Boolean = false,
)

/** What a terminal sends for [press], or `null` for keys that send nothing, such as a lone Shift. */
internal fun encodeKey(press: KeyPress, hasApplicationCursorKeys: Boolean): String? {
    val encoded = when {
        press.key in CURSOR_KEYS -> (if (hasApplicationCursorKeys) "${ESCAPE}O" else "$ESCAPE[") + CURSOR_KEYS.getValue(press.key)
        press.key == Key.Tab && press.isShift -> "$ESCAPE[Z"
        press.key in SPECIAL_KEYS -> SPECIAL_KEYS.getValue(press.key)
        press.isCtrl -> press.controlCode()
        else -> press.printedText()
    }
    return if (press.isAlt && encoded != null) ESCAPE + encoded else encoded
}

private fun KeyPress.controlCode(): String? {
    val letter = LETTERS.indexOf(key)
    return if (letter >= 0) (letter + CONTROL_OFFSET).toChar().toString() else CONTROL_SYMBOLS[key]
}

private fun KeyPress.printedText(): String? = codePoint.takeIf { it in FIRST_TEXT_CODE..LAST_TEXT_CODE && it != DELETE_CODE }?.toChar()?.toString()
