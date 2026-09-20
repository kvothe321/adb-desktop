package com.tlpcraft.adbdesktop.feature.shell.terminal

import com.tlpcraft.adbdesktop.uikit.component.TerminalRow
import com.tlpcraft.adbdesktop.uikit.component.TerminalSpan

private const val MAX_SCROLLBACK = 5_000
private const val ERASE_SCROLLBACK = 3
private const val CURSOR_POSITION_REPORT = 6
private const val DEVICE_STATUS_OK = 5
private const val APPLICATION_CURSOR_KEYS = 1
private const val AUTO_WRAP = 7
private const val CURSOR_VISIBLE = 25
private const val BRACKETED_PASTE = 2004
private const val ALTERNATE_SCREEN_LEGACY = 47
private const val ALTERNATE_SCREEN_CLEARED = 1047
private const val ALTERNATE_SCREEN_SAVED = 1049

/** The VT100 and xterm commands `toybox` programs and common tools use. Anything else is ignored. */
internal class TerminalEmulator(rows: Int, columns: Int, private val onReply: (String) -> Unit) : AnsiHandler {
    private val parser = AnsiParser(this)
    private val scrollback = ArrayDeque<TerminalRow>()
    private var nextLineId = 0L
    private val primary = ScreenBuffer(rows, columns, ::archive)
    private val alternate = ScreenBuffer(rows, columns) {}
    private var screen = primary
    private var style = BLANK_STYLE
    private var isCursorVisible = true

    var hasApplicationCursorKeys = false
        private set
    var hasBracketedPaste = false
        private set

    private val blank: Cell
        get() = Cell(" ", BLANK_STYLE.copy(background = style.background))

    fun feed(output: String) = parser.feed(output)

    fun resize(rows: Int, columns: Int) {
        primary.resize(rows, columns)
        alternate.resize(rows, columns)
    }

    /** Forgets what was printed. The size and the modes the program asked for stay. */
    fun clear() {
        scrollback.clear()
        primary.clear()
    }

    /** Everything to show, oldest line first. The alternate screen has no history, as it belongs to one program. */
    fun render(): List<TerminalRow> {
        val isAlternate = screen === alternate
        val visible = screen.lines.mapIndexed { index, line ->
            val cursorColumn = screen.cursorColumn.takeIf { isCursorVisible && index == screen.cursorRow }
            line.toRow(id = if (isAlternate) -1L - index else nextLineId + index, cursorColumn = cursorColumn)
        }
        return if (isAlternate) visible else scrollback + visible
    }

    override fun print(text: String) = screen.put(text, style)

    override fun control(code: Char) {
        when (code) {
            '\r' -> screen.carriageReturn()
            '\n', '\u000b', '\u000c' -> screen.lineFeed(blank)
            '\b' -> screen.backspace()
            '\t' -> screen.tab()
        }
    }

    override fun escape(code: Char) {
        when (code) {
            '7' -> {
                screen.saveCursor()
            }

            '8' -> {
                screen.restoreCursor()
            }

            'D' -> {
                screen.lineFeed(blank)
            }

            'E' -> {
                screen.carriageReturn()
                screen.lineFeed(blank)
            }

            'M' -> {
                screen.reverseLineFeed(blank)
            }
        }
    }

    override fun csi(final: Char, params: CsiParams, prefix: Char?) {
        when {
            prefix == null -> command(final, params)
            prefix == '?' && (final == 'h' || final == 'l') -> params.values.filterNotNull().forEach { setMode(it, isOn = final == 'h') }
        }
    }

    private fun command(final: Char, params: CsiParams) {
        val count = params.count(0)
        when (final) {
            'A' -> screen.moveBy(-count, 0)
            'B', 'e' -> screen.moveBy(count, 0)
            'C', 'a' -> screen.moveBy(0, count)
            'D' -> screen.moveBy(0, -count)
            'E' -> screen.moveTo(screen.cursorRow + count, 0)
            'F' -> screen.moveTo(screen.cursorRow - count, 0)
            'G', '`' -> screen.moveTo(screen.cursorRow, count - 1)
            'd' -> screen.moveTo(count - 1, screen.cursorColumn)
            'H', 'f' -> screen.moveTo(count - 1, params.count(1) - 1)
            'J' -> eraseDisplay(params.mode())
            'K' -> screen.eraseLine(params.mode(), blank)
            'L' -> screen.insertLines(count, blank)
            'M' -> screen.deleteLines(count, blank)
            '@' -> screen.insertCells(count, blank)
            'P' -> screen.deleteCells(count, blank)
            'X' -> screen.eraseCells(count, blank)
            'S' -> screen.scrollUp(count, blank)
            'T' -> screen.scrollDown(count, blank)
            'r' -> screen.setScrollRegion(count - 1, params.count(1, default = screen.rows) - 1)
            's' -> screen.saveCursor()
            'u' -> screen.restoreCursor()
            'm' -> style = style.withRendition(params.values)
            'n' -> reportStatus(params.mode())
        }
    }

    private fun eraseDisplay(mode: Int) {
        if (mode == ERASE_SCROLLBACK) scrollback.clear() else screen.eraseDisplay(mode, blank)
    }

    private fun reportStatus(request: Int) {
        when (request) {
            DEVICE_STATUS_OK -> onReply("\u001b[0n")
            CURSOR_POSITION_REPORT -> onReply("\u001b[${screen.cursorRow + 1};${screen.cursorColumn + 1}R")
        }
    }

    private fun setMode(mode: Int, isOn: Boolean) {
        when (mode) {
            APPLICATION_CURSOR_KEYS -> hasApplicationCursorKeys = isOn
            AUTO_WRAP -> screen.isAutoWrapOn = isOn
            CURSOR_VISIBLE -> isCursorVisible = isOn
            BRACKETED_PASTE -> hasBracketedPaste = isOn
            ALTERNATE_SCREEN_LEGACY, ALTERNATE_SCREEN_CLEARED, ALTERNATE_SCREEN_SAVED -> switchScreen(toAlternate = isOn)
        }
    }

    private fun switchScreen(toAlternate: Boolean) {
        if (toAlternate == (screen === alternate)) return
        if (toAlternate) {
            primary.saveCursor()
            alternate.clear()
            screen = alternate
        } else {
            screen = primary
            primary.restoreCursor()
        }
    }

    private fun archive(line: Array<Cell>) {
        scrollback.addLast(line.toRow(id = nextLineId++, cursorColumn = null))
        if (scrollback.size > MAX_SCROLLBACK) scrollback.removeFirst()
    }
}

/** Blank cells at the end are dropped, so copied text has no trailing spaces. */
private fun Array<Cell>.toRow(id: Long, cursorColumn: Int?): TerminalRow {
    val lastUsed = indexOfLast { it != BLANK_CELL }
    val end = maxOf(lastUsed, cursorColumn ?: -1) + 1
    val spans = mutableListOf<TerminalSpan>()
    for (column in 0 until end) {
        val cell = this[column]
        val cellStyle = if (column == cursorColumn) cell.style.copy(isInverted = !cell.style.isInverted) else cell.style
        val last = spans.lastOrNull()
        if (last != null && last.copy(text = "") == cellStyle) {
            spans[spans.lastIndex] = last.copy(text = last.text + cell.text)
        } else {
            spans += cellStyle.copy(text = cell.text)
        }
    }
    return TerminalRow(id, spans)
}
