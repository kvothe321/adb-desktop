package com.tlpcraft.adbdesktop.core.data.source.adb.parser

import com.tlpcraft.adbdesktop.core.data.source.adb.fixtures.AdbOutputs
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly

class TrackDevicesDecoderTest : FunSpec({
    val payload = "emulator-5554          device transport_id:1\n"
    val frame = payload.length.toString(16).padStart(4, '0') + payload

    test("an empty frame yields an empty listing") {
        TrackDevicesDecoder().decode(AdbOutputs.TRACK_DEVICES_EMPTY_FRAME) shouldContainExactly listOf("")
    }

    test("decodes several frames from one chunk") {
        TrackDevicesDecoder().decode(frame + AdbOutputs.TRACK_DEVICES_EMPTY_FRAME) shouldContainExactly listOf(payload, "")
    }

    test("waits for frames split across chunks") {
        val decoder = TrackDevicesDecoder()

        decoder.decode(frame.take(2)).shouldBeEmpty()
        decoder.decode(frame.substring(2, 10)).shouldBeEmpty()
        decoder.decode(frame.substring(10)) shouldContainExactly listOf(payload)
    }

    test("fails on a malformed header") {
        shouldThrow<IllegalStateException> { TrackDevicesDecoder().decode("zz12abc") }
    }
})
