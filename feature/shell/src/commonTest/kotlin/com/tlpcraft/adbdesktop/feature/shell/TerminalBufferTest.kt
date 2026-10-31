package com.tlpcraft.adbdesktop.feature.shell

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class TerminalBufferTest : FunSpec({
    fun TerminalBuffer.text() = snapshot().map { it.text }

    test("splits a chunk into lines") {
        TerminalBuffer().apply { addOutput("one\ntwo\n") }.text() shouldContainExactly listOf("one", "two")
    }

    test("joins a line split across chunks") {
        TerminalBuffer().apply {
            addOutput("hel")
            addOutput("lo\nwor")
            addOutput("ld\n")
        }.text() shouldContainExactly listOf("hello", "world")
    }

    test("keeps blank lines and normalizes CRLF") {
        TerminalBuffer().apply { addOutput("a\r\n\r\nb\r\n") }.text() shouldContainExactly listOf("a", "", "b")
    }

    test("a command closes an open output line") {
        TerminalBuffer().apply {
            addOutput("partial")
            addCommand("ls")
            addOutput("next\n")
        }.text() shouldContainExactly listOf("partial", "ls", "next")
    }
})
