package com.tlpcraft.adbdesktop.feature.shell

import com.tlpcraft.adbdesktop.feature.shell.contract.TerminalLine

/** Transcript of a session. Output arrives in arbitrary chunks, so the last line stays open until its newline. */
internal class TerminalBuffer {
    private val lines = ArrayDeque<TerminalLine>()
    private var nextId = 0L
    private var isLastLineOpen = false

    fun snapshot(): List<TerminalLine> = lines.toList()

    fun addCommand(command: String) {
        add(command, isCommand = true)
        isLastLineOpen = false
    }

    fun addOutput(chunk: String) {
        val parts = chunk.replace("\r\n", "\n").split('\n')
        parts.forEachIndexed { index, part ->
            val isLastPart = index == parts.lastIndex
            when {
                index == 0 && isLastLineOpen -> extendLastLine(part)
                !isLastPart || part.isNotEmpty() -> add(part, isCommand = false)
            }
        }
        isLastLineOpen = parts.last().isNotEmpty()
        repeat((lines.size - MAX_LINES).coerceAtLeast(0)) { lines.removeFirst() }
    }

    fun clear() {
        lines.clear()
        isLastLineOpen = false
    }

    private fun add(text: String, isCommand: Boolean) {
        lines.addLast(TerminalLine(nextId++, text, isCommand))
    }

    private fun extendLastLine(text: String) {
        lines.removeLast().let { lines.addLast(it.copy(text = it.text + text)) }
    }

    private companion object {
        const val MAX_LINES = 5_000
    }
}
