package com.tlpcraft.adbdesktop.uikit.component

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TerminalGridTest : FunSpec({
    test("a cursor after the last character stays painted") {
        TerminalSpan(" ", isInverted = true).paintedText() shouldBe "\u00A0"
    }

    test("a colored background reaches the end of its text") {
        TerminalSpan(" bg  ", background = TerminalColor.Red).paintedText() shouldBe " bg\u00A0\u00A0"
    }

    test("plain text keeps its spaces, so copying it gives what was printed") {
        TerminalSpan("ls ").paintedText() shouldBe "ls "
    }
})
