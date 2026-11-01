package com.tlpcraft.adbdesktop.core.data.source.adb.mirror

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MirrorControlDto
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.io.DataInputStream

class ScrcpyControlEncoderTest : FunSpec({
    fun read(message: MirrorControlDto) = DataInputStream(ByteArrayInputStream(ScrcpyControlEncoder.encode(message)))

    test("a key is its action, code, no repeat and meta state") {
        val input = read(MirrorControlDto.Key(action = 1, keyCode = 66, metaState = 0x41))

        input.readUnsignedByte() shouldBe 0
        input.readUnsignedByte() shouldBe 1
        input.readInt() shouldBe 66
        input.readInt() shouldBe 0
        input.readInt() shouldBe 0x41
        input.available() shouldBe 0
    }

    test("a touch is a pressed finger at a position in the shown frame") {
        val input = read(MirrorControlDto.Touch(action = 0, x = 100, y = 200, frameWidth = 576, frameHeight = 1280))

        input.readUnsignedByte() shouldBe 2
        input.readUnsignedByte() shouldBe 0
        input.readLong() shouldBe -2L
        input.readInt() shouldBe 100
        input.readInt() shouldBe 200
        input.readUnsignedShort() shouldBe 576
        input.readUnsignedShort() shouldBe 1280
        input.readUnsignedShort() shouldBe 0xFFFF
        input.readInt() shouldBe 0
        input.readInt() shouldBe 0
        input.available() shouldBe 0
    }

    test("lifting the finger releases the pressure") {
        val input = read(MirrorControlDto.Touch(action = 1, x = 0, y = 0, frameWidth = 1, frameHeight = 1))

        input.skipBytes(1 + 1 + 8 + 12)
        input.readUnsignedShort() shouldBe 0
    }

    test("scrolling one notch up is a sixteenth of the fixed point range") {
        val input = read(MirrorControlDto.Scroll(x = 5, y = 6, frameWidth = 10, frameHeight = 20, horizontal = 0f, vertical = 1f))

        input.readUnsignedByte() shouldBe 3
        input.skipBytes(12)
        input.readShort() shouldBe 0.toShort()
        input.readShort() shouldBe 0x0800.toShort()
        input.readInt() shouldBe 0
        input.available() shouldBe 0
    }

    test("scrolling past the range is clamped") {
        val input = read(MirrorControlDto.Scroll(x = 0, y = 0, frameWidth = 1, frameHeight = 1, horizontal = -100f, vertical = 100f))

        input.skipBytes(1 + 12)
        input.readShort() shouldBe Short.MIN_VALUE
        input.readShort() shouldBe Short.MAX_VALUE
    }

    test("text is length prefixed UTF-8") {
        val input = read(MirrorControlDto.Text("hé"))

        input.readUnsignedByte() shouldBe 1
        input.readInt() shouldBe 3
        String(input.readAllBytes()) shouldBe "hé"
    }

    test("long text is cut at a character boundary") {
        val input = read(MirrorControlDto.Text("a" + "é".repeat(200)))

        input.skipBytes(1)
        val size = input.readInt()
        size shouldBe 299
        String(input.readAllBytes()) shouldBe "a" + "é".repeat(149)
    }

    test("pasting sets the clipboard without a sequence and asks to paste") {
        val input = read(MirrorControlDto.SetClipboard("hi", paste = true))

        input.readUnsignedByte() shouldBe 9
        input.readLong() shouldBe 0L
        input.readUnsignedByte() shouldBe 1
        input.readInt() shouldBe 2
        String(input.readAllBytes()) shouldBe "hi"
    }

    test("back and rotate are single type bytes plus their action") {
        ScrcpyControlEncoder.encode(MirrorControlDto.BackOrScreenOn(action = 1)).toList() shouldBe listOf<Byte>(4, 1)
        ScrcpyControlEncoder.encode(MirrorControlDto.RotateDevice).toList() shouldBe listOf<Byte>(11)
    }
})
