package com.tlpcraft.adbdesktop.feature.shell.terminal

private const val TO_START = 1
private const val ALL = 2
private const val TAB_WIDTH = 8

/** Overwriting part of a wide character blanks the rest of it, so no half character is left behind. */
internal fun Array<Cell>.breakWideCharacters(from: Int, until: Int) {
    if (this[from].isContinuation) {
        var start = from - 1
        while (start > 0 && this[start].isContinuation) start--
        fill(BLANK_CELL, start, from)
    }
    var end = until
    while (end < size && this[end].isContinuation) this[end++] = BLANK_CELL
}

internal fun ScreenBuffer.backspace() = moveTo(cursorRow, cursorColumn - 1)

internal fun ScreenBuffer.tab() = moveTo(cursorRow, (cursorColumn / TAB_WIDTH + 1) * TAB_WIDTH)

internal fun ScreenBuffer.moveBy(rowDelta: Int, columnDelta: Int) = moveTo(cursorRow + rowDelta, cursorColumn + columnDelta)

internal fun ScreenBuffer.clear() {
    lines.indices.forEach { lines[it] = blankLine() }
    moveTo(0, 0)
}

internal fun ScreenBuffer.eraseLine(mode: Int, blank: Cell) {
    val line = lines[cursorRow]
    when (mode) {
        TO_START -> line.fill(blank, 0, cursorColumn + 1)
        ALL -> line.fill(blank)
        else -> line.fill(blank, cursorColumn, columns)
    }
}

internal fun ScreenBuffer.eraseDisplay(mode: Int, blank: Cell) {
    val affected = when (mode) {
        TO_START -> 0 until cursorRow
        ALL -> 0 until rows
        else -> cursorRow + 1 until rows
    }
    affected.forEach { lines[it] = blankLine(blank) }
    if (mode != ALL) eraseLine(mode, blank)
}

internal fun ScreenBuffer.eraseCells(count: Int, blank: Cell) {
    lines[cursorRow].fill(blank, cursorColumn, (cursorColumn + count).coerceAtMost(columns))
}

internal fun ScreenBuffer.insertCells(count: Int, blank: Cell) {
    val line = lines[cursorRow]
    val shift = count.coerceAtMost(columns - cursorColumn)
    line.copyInto(line, destinationOffset = cursorColumn + shift, startIndex = cursorColumn, endIndex = columns - shift)
    line.fill(blank, cursorColumn, cursorColumn + shift)
}

internal fun ScreenBuffer.deleteCells(count: Int, blank: Cell) {
    val line = lines[cursorRow]
    val shift = count.coerceAtMost(columns - cursorColumn)
    line.copyInto(line, destinationOffset = cursorColumn, startIndex = cursorColumn + shift, endIndex = columns)
    line.fill(blank, columns - shift, columns)
}

internal fun ScreenBuffer.insertLines(count: Int, blank: Cell) {
    if (cursorRow !in scrollTop..scrollBottom) return
    repeat(count.coerceAtMost(scrollBottom - cursorRow + 1)) {
        lines.removeAt(scrollBottom)
        lines.add(cursorRow, blankLine(blank))
    }
}

internal fun ScreenBuffer.deleteLines(count: Int, blank: Cell) {
    if (cursorRow !in scrollTop..scrollBottom) return
    repeat(count.coerceAtMost(scrollBottom - cursorRow + 1)) {
        lines.removeAt(cursorRow)
        lines.add(scrollBottom, blankLine(blank))
    }
}
