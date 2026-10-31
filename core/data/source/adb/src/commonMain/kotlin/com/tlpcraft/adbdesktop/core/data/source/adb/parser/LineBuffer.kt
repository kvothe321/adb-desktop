package com.tlpcraft.adbdesktop.core.data.source.adb.parser

/** Reassembles lines from chunks that may cut them anywhere. */
internal class LineBuffer {
    private val pending = StringBuilder()

    fun feed(chunk: String): List<String> {
        pending.append(chunk)
        val lastNewline = pending.lastIndexOf("\n")
        if (lastNewline < 0) return emptyList()

        val complete = pending.substring(0, lastNewline)
        pending.deleteRange(0, lastNewline + 1)
        return complete.split('\n').map { it.trimEnd('\r') }
    }
}
