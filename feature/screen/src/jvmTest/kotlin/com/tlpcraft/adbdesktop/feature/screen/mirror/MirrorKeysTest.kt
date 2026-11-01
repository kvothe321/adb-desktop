package com.tlpcraft.adbdesktop.feature.screen.mirror

import androidx.compose.ui.input.key.Key
import com.tlpcraft.adbdesktop.core.domain.model.DeviceKey
import com.tlpcraft.adbdesktop.core.domain.model.KeyAction
import com.tlpcraft.adbdesktop.core.domain.model.KeyModifier
import com.tlpcraft.adbdesktop.core.domain.model.MirrorInput
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MirrorKeysTest : FunSpec({
    test("typed characters go as text, whatever the key") {
        MirrorKeyPress(Key.A, 'A'.code, isDown = true, isShift = true).toMirrorKey() shouldBe MirrorKeyResult.Send(MirrorInput.Text("A"))
        MirrorKeyPress(Key.Unknown, 'é'.code, isDown = true).toMirrorKey() shouldBe MirrorKeyResult.Send(MirrorInput.Text("é"))
    }

    test("characters outside the basic plane become a surrogate pair") {
        MirrorKeyPress(Key.Unknown, 0x1F600, isDown = true).toMirrorKey() shouldBe MirrorKeyResult.Send(MirrorInput.Text("😀"))
    }

    test("releasing a typed key sends nothing") {
        MirrorKeyPress(Key.A, 'a'.code, isDown = false).toMirrorKey() shouldBe MirrorKeyResult.Ignored
    }

    test("editing keys go as keys, both down and up") {
        MirrorKeyPress(Key.Backspace, 8, isDown = true).toMirrorKey() shouldBe MirrorKeyResult.Send(MirrorInput.Key(KeyAction.Down, DeviceKey.Backspace))
        MirrorKeyPress(Key.Enter, 10, isDown = false).toMirrorKey() shouldBe MirrorKeyResult.Send(MirrorInput.Key(KeyAction.Up, DeviceKey.Enter))
    }

    test("shortcuts go as keys with their modifiers") {
        MirrorKeyPress(Key.A, 1, isDown = true, isCtrl = true).toMirrorKey() shouldBe
            MirrorKeyResult.Send(MirrorInput.Key(KeyAction.Down, DeviceKey.A, setOf(KeyModifier.Ctrl)))
        MirrorKeyPress(Key.Seven, 0, isDown = true, isAlt = true).toMirrorKey() shouldBe
            MirrorKeyResult.Send(MirrorInput.Key(KeyAction.Down, DeviceKey.Digit7, setOf(KeyModifier.Alt)))
    }

    test("Ctrl+V and Cmd+V paste") {
        MirrorKeyPress(Key.V, 22, isDown = true, isCtrl = true).toMirrorKey() shouldBe MirrorKeyResult.Paste
        MirrorKeyPress(Key.V, 'v'.code, isDown = true, isMeta = true).toMirrorKey() shouldBe MirrorKeyResult.Paste
    }

    test("a lone modifier sends nothing") {
        MirrorKeyPress(Key.ShiftLeft, 0, isDown = true, isShift = true).toMirrorKey() shouldBe MirrorKeyResult.Ignored
    }
})
