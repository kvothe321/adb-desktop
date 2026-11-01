package com.tlpcraft.adbdesktop.core.data.source.adb.mirror

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException

private const val CONFIG = 1L shl 62
private const val KEY_FRAME = 1L shl 61

private fun stream(vararg packets: Pair<Long, ByteArray>): ByteArray = ByteArrayOutputStream().also { bytes ->
    DataOutputStream(bytes).use { output ->
        packets.forEach { (header, data) ->
            output.writeLong(header)
            output.writeInt(data.size)
            output.write(data)
        }
    }
}.toByteArray()

class ScrcpyPacketReaderTest : FunSpec({
    test("puts parameter sets in front of the next frame only") {
        val reader = ScrcpyPacketReader(ByteArrayInputStream(stream(CONFIG to byteArrayOf(1, 2), (KEY_FRAME or 7) to byteArrayOf(3), 9L to byteArrayOf(4))))

        reader.nextAccessUnit() shouldBe byteArrayOf(1, 2, 3)
        reader.nextAccessUnit() shouldBe byteArrayOf(4)
    }

    test("a clean end between packets is the end of the stream") {
        ScrcpyPacketReader(ByteArrayInputStream(ByteArray(0))).nextAccessUnit().shouldBeNull()
    }

    test("a stream cut inside a packet is a failure") {
        val cut = stream(1L to byteArrayOf(1, 2, 3)).dropLast(1).toByteArray()

        shouldThrow<EOFException> { ScrcpyPacketReader(ByteArrayInputStream(cut)).nextAccessUnit() }
    }

    test("an impossible size is a failure, not a huge allocation") {
        val corrupt = stream(1L to ByteArray(0)).also { it[8] = 0x7F }

        shouldThrow<IOException> { ScrcpyPacketReader(ByteArrayInputStream(corrupt)).nextAccessUnit() }
    }
})
