package com.tlpcraft.adbdesktop.feature.shell.terminal

import com.tlpcraft.adbdesktop.uikit.component.TerminalSpan

internal data class Cell(val text: String, val style: TerminalSpan)

internal val BLANK_STYLE = TerminalSpan(text = "")
internal val BLANK_CELL = Cell(" ", BLANK_STYLE)

/** The visible character grid and its cursor. Lines leaving the top of the full screen go to [onLineScrolledOff]. */
internal class ScreenBuffer(rows: Int, columns: Int, private val onLineScrolledOff: (Array<Cell>) -> Unit) {
    var rows = rows
        private set
    var columns = columns
        private set
    var cursorRow = 0
        private set
    var cursorColumn = 0
        private set
    var isAutoWrapOn = true
    var scrollTop = 0
        private set
    var scrollBottom = rows - 1
        private set

    /** Reaching the last column waits for the next character before wrapping, so a full line ends without a blank one. */
    private var isWrapPending = false
    private var savedRow = 0
    private var savedColumn = 0

    val lines: MutableList<Array<Cell>> = MutableList(rows) { blankLine() }

    fun blankLine(blank: Cell = BLANK_CELL): Array<Cell> = Array(columns) { blank }

    fun put(text: String, style: TerminalSpan) {
        if (isWrapPending) {
            carriageReturn()
            lineFeed(BLANK_CELL)
        }
        lines[cursorRow][cursorColumn] = Cell(text, style)
        if (cursorColumn < columns - 1) cursorColumn++ else isWrapPending = isAutoWrapOn
    }

    fun carriageReturn() {
        cursorColumn = 0
        isWrapPending = false
    }

    fun lineFeed(blank: Cell) {
        isWrapPending = false
        when {
            cursorRow == scrollBottom -> scrollUp(1, blank)
            cursorRow < rows - 1 -> cursorRow++
        }
    }

    fun reverseLineFeed(blank: Cell) {
        isWrapPending = false
        when {
            cursorRow == scrollTop -> scrollDown(1, blank)
            cursorRow > 0 -> cursorRow--
        }
    }

    /** Rows and columns count from 0. Positions off the screen end up at its edge, which programs rely on. */
    fun moveTo(row: Int, column: Int) {
        cursorRow = row.coerceIn(0, rows - 1)
        cursorColumn = column.coerceIn(0, columns - 1)
        isWrapPending = false
    }

    fun scrollUp(count: Int, blank: Cell) {
        repeat(count.coerceAtMost(rows)) {
            val line = lines.removeAt(scrollTop)
            if (scrollTop == 0) onLineScrolledOff(line)
            lines.add(scrollBottom, blankLine(blank))
        }
    }

    fun scrollDown(count: Int, blank: Cell) {
        repeat(count.coerceAtMost(rows)) {
            lines.removeAt(scrollBottom)
            lines.add(scrollTop, blankLine(blank))
        }
    }

    fun setScrollRegion(top: Int, bottom: Int) {
        if (top >= bottom || bottom >= rows) return
        scrollTop = top
        scrollBottom = bottom
        moveTo(0, 0)
    }

    fun saveCursor() {
        savedRow = cursorRow
        savedColumn = cursorColumn
    }

    fun restoreCursor() = moveTo(savedRow, savedColumn)

    /** Lines keep their content instead of reflowing. The program redraws them when it learns the new size. */
    fun resize(newRows: Int, newColumns: Int) {
        val nextColumn = cursorColumn + if (isWrapPending) 1 else 0
        columns = newColumns
        lines.indices.forEach { index -> lines[index] = Array(newColumns) { lines[index].getOrElse(it) { BLANK_CELL } } }
        while (lines.size > newRows) removeSpareLine()
        while (lines.size < newRows) lines.add(blankLine())
        rows = newRows
        scrollTop = 0
        scrollBottom = newRows - 1
        moveTo(cursorRow, nextColumn)
    }

    /** Prefers empty lines below the cursor, so what was just printed stays on the screen. */
    private fun removeSpareLine() {
        if (cursorRow < lines.lastIndex) {
            lines.removeAt(lines.lastIndex)
        } else {
            onLineScrolledOff(lines.removeAt(0))
            cursorRow--
        }
    }
}
