package com.tlpcraft.adbdesktop.feature.shell.terminal

import androidx.compose.ui.input.key.Key
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class KeyEncoderTest : FunSpec({
    fun encode(press: KeyPress, hasApplicationCursorKeys: Boolean = false) = encodeKey(press, hasApplicationCursorKeys)

    test("plain typing is left to text input, where input methods can compose it") {
        encode(KeyPress(Key.A, 'a'.code)).shouldBeNull()
        encode(KeyPress(Key.A, 'A'.code, isShift = true)).shouldBeNull()
        encode(KeyPress(Key.A, 'ă'.code)).shouldBeNull()
    }

    test("the keys that edit a line") {
        encode(KeyPress(Key.Enter, '\n'.code)) shouldBe "\r"
        encode(KeyPress(Key.Backspace, 8)) shouldBe "\u007f"
        encode(KeyPress(Key.Tab, '\t'.code)) shouldBe "\t"
        encode(KeyPress(Key.Delete, 127)) shouldBe "\u001b[3~"
    }

    test("Ctrl+C interrupts and Ctrl+D ends input") {
        encode(KeyPress(Key.C, 3, isCtrl = true)) shouldBe "\u0003"
        encode(KeyPress(Key.D, 4, isCtrl = true)) shouldBe "\u0004"
    }

    test("arrow keys depend on what the program asked for") {
        encode(KeyPress(Key.DirectionUp, 0)) shouldBe "\u001b[A"
        encode(KeyPress(Key.DirectionUp, 0), hasApplicationCursorKeys = true) shouldBe "\u001bOA"
    }

    test("Alt prefixes the key with escape") {
        encode(KeyPress(Key.B, 'b'.code, isAlt = true)) shouldBe "\u001bb"
    }

    test("keys without text send nothing") {
        encode(KeyPress(Key.ShiftLeft, 0xFFFF)).shouldBeNull()
        encode(KeyPress(Key.CtrlLeft, 0, isCtrl = true)).shouldBeNull()
    }
})
