package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.data.repository.mapper.toControlMessages
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MirrorControlDto
import com.tlpcraft.adbdesktop.core.domain.model.DeviceKey
import com.tlpcraft.adbdesktop.core.domain.model.KeyAction
import com.tlpcraft.adbdesktop.core.domain.model.KeyModifier
import com.tlpcraft.adbdesktop.core.domain.model.MirrorInput
import com.tlpcraft.adbdesktop.core.domain.model.MirrorPoint
import com.tlpcraft.adbdesktop.core.domain.model.TouchAction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MirrorInputMapperTest : FunSpec({
    fun keyCodeOf(key: DeviceKey) = (MirrorInput.Key(KeyAction.Down, key).toControlMessages().single() as MirrorControlDto.Key).keyCode

    test("keys use Android's key codes") {
        keyCodeOf(DeviceKey.Home) shouldBe 3
        keyCodeOf(DeviceKey.AppSwitch) shouldBe 187
        keyCodeOf(DeviceKey.A) shouldBe 29
        keyCodeOf(DeviceKey.Z) shouldBe 54
        keyCodeOf(DeviceKey.Digit0) shouldBe 7
        keyCodeOf(DeviceKey.Digit9) shouldBe 16
    }

    test("modifiers set Android's meta flags") {
        MirrorInput.Key(KeyAction.Up, DeviceKey.C, setOf(KeyModifier.Ctrl, KeyModifier.Shift)).toControlMessages() shouldBe
            listOf(MirrorControlDto.Key(action = 1, keyCode = 31, metaState = 0x3041))
    }

    test("a touch keeps its position in the shown frame") {
        MirrorInput.Touch(TouchAction.Move, MirrorPoint(1, 2, 30, 40)).toControlMessages() shouldBe
            listOf(MirrorControlDto.Touch(action = 2, x = 1, y = 2, frameWidth = 30, frameHeight = 40))
    }

    test("back is a full press") {
        MirrorInput.BackOrScreenOn.toControlMessages() shouldBe listOf(MirrorControlDto.BackOrScreenOn(0), MirrorControlDto.BackOrScreenOn(1))
    }

    test("pasting sets the device clipboard and pastes it") {
        MirrorInput.Paste("hello").toControlMessages() shouldBe listOf(MirrorControlDto.SetClipboard("hello", paste = true))
    }
})
