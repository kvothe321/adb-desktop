package com.tlpcraft.adbdesktop.feature.shell.terminal

import com.tlpcraft.adbdesktop.uikit.component.TerminalColor
import com.tlpcraft.adbdesktop.uikit.component.TerminalSpan
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

private const val ESC = "\u001b"

class TerminalEmulatorTest : FunSpec({
    val replies = mutableListOf<String>()

    fun terminal(rows: Int = 4, columns: Int = 20) = TerminalEmulator(rows, columns, onReply = { replies += it })

    fun TerminalEmulator.text() = render().map { it.text.trimEnd() }

    fun TerminalEmulator.spans(row: Int): List<TerminalSpan> = render()[row].spans

    beforeTest { replies.clear() }

    test("text wraps at the last column, and a full line ending in a newline adds no blank line") {
        val terminal = terminal(rows = 3, columns = 4)

        terminal.feed("abcdef\r\n")
        terminal.text().map { it.trimEnd() } shouldContainExactly listOf("abcd", "ef", "")

        terminal.feed("wxyz\r\nq")
        terminal.text().map { it.trimEnd() } shouldContainExactly listOf("abcd", "ef", "wxyz", "q")
    }

    test("lines scrolling off the screen stay in the scrollback, with stable ids") {
        val terminal = terminal(rows = 2)
        terminal.feed("one\r\ntwo")
        val idOfOne = terminal.render().first().id

        terminal.feed("\r\nthree")

        terminal.text() shouldContainExactly listOf("one", "two", "three")
        terminal.render().first().id shouldBe idOfOne
    }

    test("a chunk can end anywhere, even inside a sequence") {
        val terminal = terminal()

        "$ESC[31mred$ESC[0m plain".forEach { terminal.feed(it.toString()) }

        terminal.spans(0).first() shouldBe TerminalSpan("red", foreground = TerminalColor.Red)
        terminal.text().first().trimEnd() shouldBe "red plain"
    }

    test("window titles are not printed") {
        val terminal = terminal()

        terminal.feed("$ESC]0;title\u0007ok")

        terminal.text().first().trimEnd() shouldBe "ok"
    }

    test("the cursor shows as one inverted cell, until a program hides it") {
        val terminal = terminal()

        terminal.feed("ab")
        terminal.spans(0) shouldContainExactly listOf(TerminalSpan("ab"), TerminalSpan(" ", isInverted = true))

        terminal.feed("$ESC[?25l")
        terminal.spans(0) shouldContainExactly listOf(TerminalSpan("ab"))
    }

    context("colors") {
        test("basic, bright, 256 color and RGB") {
            val terminal = terminal(columns = 30)

            terminal.feed("$ESC[?25l$ESC[32ma$ESC[0;95mb$ESC[38;5;196mc$ESC[48;2;255;255;255md")

            val spans = terminal.spans(0)
            spans[0] shouldBe TerminalSpan("a", foreground = TerminalColor.Green)
            spans[1] shouldBe TerminalSpan("b", foreground = TerminalColor.BrightMagenta)
            spans[2].foreground shouldBe TerminalColor.BrightRed
            spans[3].background shouldBe TerminalColor.BrightWhite
        }

        test("bold, dim, underline and inverse switch on and off") {
            val terminal = terminal()

            terminal.feed("$ESC[?25l$ESC[1;4;7ma$ESC[22;24;27mb$ESC[2mc")

            terminal.spans(0) shouldContainExactly listOf(
                TerminalSpan("a", isBold = true, isUnderlined = true, isInverted = true),
                TerminalSpan("b"),
                TerminalSpan("c", isDim = true),
            )
        }
    }

    context("editing") {
        test("erasing to the end of the line") {
            val terminal = terminal()

            terminal.feed("abcdef\r$ESC[3C$ESC[K")

            terminal.text().first().trimEnd() shouldBe "abc"
        }

        test("inserting and deleting characters") {
            val terminal = terminal()

            terminal.feed("abcd\r$ESC[C$ESC[P")
            terminal.text().first().trimEnd() shouldBe "acd"

            terminal.feed("$ESC[2@")
            terminal.text().first().trimEnd() shouldBe "a  cd"
        }

        test("a scroll region keeps the lines around it in place") {
            val terminal = terminal(rows = 4)

            terminal.feed("top$ESC[2;3r$ESC[2;1Ha$ESC[3;1Hb\n\n")
            terminal.feed("$ESC[4;1Hend")

            terminal.text().map { it.trimEnd() } shouldContainExactly listOf("top", "", "", "end")
        }

        test("insert line and delete line stay inside the region") {
            val terminal = terminal(rows = 3)

            terminal.feed("a\r\nb\r\nc$ESC[2;1H$ESC[L")
            terminal.text().map { it.trimEnd() } shouldContainExactly listOf("a", "", "b")

            terminal.feed("$ESC[M")
            terminal.text().map { it.trimEnd() } shouldContainExactly listOf("a", "b", "")
        }
    }

    context("screens") {
        test("the alternate screen leaves the shell's screen and scrollback untouched") {
            val terminal = terminal(rows = 2)
            terminal.feed("one\r\ntwo\r\nprompt")

            terminal.feed("$ESC[?1049h$ESC[H$ESC[2Jfull screen")
            terminal.text().map { it.trimEnd() } shouldContainExactly listOf("full screen", "")

            terminal.feed("$ESC[?1049l")
            terminal.text().map { it.trimEnd() } shouldContainExactly listOf("one", "two", "prompt")
        }

        test("clearing forgets the history") {
            val terminal = terminal(rows = 2)
            terminal.feed("one\r\ntwo\r\nthree")

            terminal.clear()

            terminal.text().map { it.trimEnd() } shouldContainExactly listOf("", "")
        }
    }

    context("resizing") {
        test("shrinking keeps the cursor line and moves older lines to the scrollback") {
            val terminal = terminal(rows = 3)
            terminal.feed("one\r\ntwo\r\nthree")

            terminal.resize(rows = 2, columns = 20)

            terminal.text().map { it.trimEnd() } shouldContainExactly listOf("one", "two", "three")
            terminal.render().size shouldBe 3
        }

        test("shrinking drops the blank lines under the cursor first") {
            val terminal = terminal(rows = 4)
            terminal.feed("one")

            terminal.resize(rows = 2, columns = 20)

            terminal.text().map { it.trimEnd() } shouldContainExactly listOf("one", "")
        }

        test("growing adds blank lines and columns") {
            val terminal = terminal(rows = 1, columns = 3)
            terminal.feed("abc")

            terminal.resize(rows = 2, columns = 5)
            terminal.feed("d")

            terminal.text().map { it.trimEnd() } shouldContainExactly listOf("abcd", "")
        }
    }

    test("a cursor position request is answered") {
        val terminal = terminal()

        terminal.feed("$ESC[2;5H$ESC[6n")

        replies shouldContainExactly listOf("$ESC[2;5R")
    }

    test("programs choose how the arrow keys are sent, and bracketed paste") {
        val terminal = terminal()

        terminal.feed("$ESC[?1;2004h")

        terminal.hasApplicationCursorKeys shouldBe true
        terminal.hasBracketedPaste shouldBe true
    }

    context("recorded output") {
        test("top: frames redraw in place, with an inverted header") {
            val terminal = terminal(rows = 5, columns = 40)
            val frame = "$ESC[H$ESC[J$ESC[?25l$ESC[H$ESC[JTasks: 336 total\r\n  Mem: 2018940K\r\n" +
                "$ESC[7m  PID USER        $ESC[0m\r\n$ESC[1m10219 shell top -d 1\r$ESC[m\n10214 shell sh"

            terminal.feed("top -d 1\r\r\n$frame$frame")

            terminal.render().size shouldBe 5
            terminal.text().map { it.trimEnd() } shouldContainExactly listOf(
                "Tasks: 336 total",
                "  Mem: 2018940K",
                "  PID USER",
                "10219 shell top -d 1",
                "10214 shell sh",
            )
            terminal.spans(2).first().isInverted shouldBe true
            terminal.spans(3).first().isBold shouldBe true

            terminal.feed("$ESC[?25h$ESC[0m$ESC[999H$ESC[Kprompt $ ")
            terminal.text().last().trimEnd() shouldBe "prompt $"
        }

        test("vi: columns of 0 mean the first one, and dim tildes mark empty lines") {
            val terminal = terminal(rows = 4, columns = 20)

            terminal.feed(
                "$ESC[?1049h$ESC[H$ESC[2J$ESC[H$ESC[1;0H$ESC[2Khello$ESC[2;0H$ESC[2K$ESC[2m~$ESC[m$ESC[3;0H$ESC[2K$ESC[2m~$ESC[m" +
                    "$ESC[4;0H$ESC[2K$ESC[1m-- INSERT --$ESC[m$ESC[4;16H0/0C$ESC[1;6H",
            )

            terminal.text().map { it.trimEnd() } shouldContainExactly listOf("hello", "~", "~", "-- INSERT --   0/0C")
            terminal.spans(1).first().isDim shouldBe true
            terminal.spans(3).first().isBold shouldBe true
            terminal.spans(0).lastOrNull { it.isInverted }.shouldNotBeNull().text shouldBe " "
        }

        test("the shell redraws an edited line with spaces and backspaces") {
            val terminal = terminal(rows = 2, columns = 30)

            terminal.feed("pixel:/ $ ls /sys      " + "\b".repeat(6) + "\rpixel:/ $ echo      " + "\b".repeat(6))

            terminal.text().first().trimEnd() shouldBe "pixel:/ $ echo"
        }
    }
})
