package com.tlpcraft.adbdesktop.feature.shell.terminal

/** What the parser finds in terminal output. */
internal interface AnsiHandler {
    fun print(text: String)

    fun control(code: Char)

    fun escape(code: Char)

    /** A control sequence. [prefix] is the private marker, as the `?` of `ESC[?25h`. */
    fun csi(final: Char, params: CsiParams, prefix: Char?)
}

/** Numbers of a control sequence. Missing ones are `null`. */
internal data class CsiParams(val values: List<Int?>) {
    /** The value at [index], where a missing value or 0 means [default], as with most sequences. */
    fun count(index: Int, default: Int = 1): Int = values.getOrNull(index)?.takeIf { it > 0 } ?: default

    fun mode(index: Int = 0): Int = values.getOrNull(index) ?: 0
}

/** Splits terminal output into text and commands. Output arrives in arbitrary chunks, so state survives between calls. */
internal class AnsiParser(private val handler: AnsiHandler) {
    private var state = State.Text
    private var pendingSurrogate: Char? = null
    private var prefix: Char? = null
    private val params = StringBuilder()

    fun feed(chunk: String) = chunk.forEach(::consume)

    private fun consume(char: Char) {
        when (state) {
            State.Text -> consumeText(char)
            State.Escape -> consumeEscape(char)
            State.Sequence -> consumeSequence(char)
            State.Skipped -> state = State.Text
            State.Message -> consumeMessage(char)
            State.MessageEnd -> state = State.Text
        }
    }

    private fun consumeText(char: Char) {
        val surrogate = pendingSurrogate
        pendingSurrogate = null
        when {
            char == ESCAPE -> state = State.Escape
            char < FIRST_PRINTABLE || char == DELETE -> handler.control(char)
            char.isHighSurrogate() -> pendingSurrogate = char
            surrogate != null && char.isLowSurrogate() -> handler.print("$surrogate$char")
            else -> handler.print(char.toString())
        }
    }

    private fun consumeEscape(char: Char) {
        state = State.Text
        when (char) {
            '[' -> {
                params.clear()
                prefix = null
                state = State.Sequence
            }

            ']', 'P', 'X', '^', '_' -> {
                state = State.Message
            }

            '(', ')', '*', '+', '#', '%', ' ' -> {
                state = State.Skipped
            }

            ESCAPE -> {
                state = State.Escape
            }

            else -> {
                handler.escape(char)
            }
        }
    }

    private fun consumeSequence(char: Char) {
        when (char) {
            in '0'..'9', ';', ':' -> params.append(if (char == ':') ';' else char)
            '?', '>', '=', '<' -> prefix = char
            ESCAPE -> state = State.Escape
            in FIRST_FINAL..LAST_FINAL -> finishSequence(char)
            else -> if (char < FIRST_PRINTABLE) handler.control(char)
        }
    }

    private fun finishSequence(final: Char) {
        state = State.Text
        val values = if (params.isEmpty()) emptyList() else params.split(';').map { it.toIntOrNull() }
        handler.csi(final, CsiParams(values), prefix)
    }

    /** Window titles and the like, which have no place in this app. */
    private fun consumeMessage(char: Char) {
        when (char) {
            BELL -> state = State.Text
            ESCAPE -> state = State.MessageEnd
            else -> Unit
        }
    }

    private enum class State { Text, Escape, Sequence, Skipped, Message, MessageEnd }

    private companion object {
        const val ESCAPE = '\u001b'
        const val BELL = '\u0007'
        const val DELETE = '\u007f'
        const val FIRST_PRINTABLE = ' '
        const val FIRST_FINAL = '@'
        const val LAST_FINAL = '~'
    }
}
