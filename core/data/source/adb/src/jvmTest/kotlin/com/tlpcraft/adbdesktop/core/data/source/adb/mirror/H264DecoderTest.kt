package com.tlpcraft.adbdesktop.core.data.source.adb.mirror

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MirrorFrameDto
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe

private const val NAL_TYPE_MASK = 0x1F
private val FRAME_NAL_TYPES = setOf(1, 5)

private fun fixture(name: String): ByteArray = checkNotNull(H264DecoderTest::class.java.getResourceAsStream("/mirror/$name")).use { it.readBytes() }

/** Cuts an Annex B stream into access units, ending one after each slice, which suits these one slice per frame fixtures. */
private fun accessUnits(stream: ByteArray): List<ByteArray> {
    val starts = (0..stream.size - 4).filter { i -> stream[i].toInt() == 0 && stream[i + 1].toInt() == 0 && stream[i + 2].toInt() == 1 }
    val units = mutableListOf<ByteArray>()
    var unitStart = 0
    starts.forEachIndexed { index, start ->
        if (stream[start + 3].toInt() and NAL_TYPE_MASK in FRAME_NAL_TYPES) {
            val end = starts.getOrElse(index + 1) { stream.size }
            units += stream.copyOfRange(unitStart, end)
            unitStart = end
        }
    }
    return units
}

private fun H264Decoder.decodeAll(stream: ByteArray): List<MirrorFrameDto> = accessUnits(stream).flatMap(::decode)

private fun MirrorFrameDto.channel(index: Int): Int = pixels[index].toInt() and 0xFF

class H264DecoderTest : FunSpec({
    test("a single frame comes out at once, as a still screen needs") {
        H264Decoder().use { decoder ->
            decoder.decode(accessUnits(fixture("red-64x48.h264")).first()).size shouldBe 1
        }
    }

    test("decodes access units into BGRA frames") {
        H264Decoder().use { decoder ->
            val frame = decoder.decodeAll(fixture("red-64x48.h264")).apply { shouldNotBeEmpty() }.last()

            frame.width shouldBe 64
            frame.height shouldBe 48
            frame.pixels.size shouldBe 64 * 48 * 4
            frame.channel(0) shouldBeLessThan 40
            frame.channel(1) shouldBeLessThan 40
            frame.channel(2) shouldBeGreaterThan 200
            frame.channel(3) shouldBe 255
        }
    }

    test("follows a size change, as when the device rotates") {
        H264Decoder().use { decoder ->
            val frames = decoder.decodeAll(fixture("red-64x48.h264") + fixture("blue-48x64.h264"))

            frames.map { it.width to it.height }.distinct().first() shouldBe (64 to 48)
            frames.last().width shouldBe 48
            frames.last().height shouldBe 64
            frames.last().channel(0) shouldBeGreaterThan 200
            frames.last().pixels.size shouldBe 48 * 64 * 4
        }
    }

    test("skips a broken packet and recovers at the next key frame") {
        H264Decoder().use { decoder ->
            decoder.decode(ByteArray(32) { 7 })

            decoder.decodeAll(fixture("red-64x48.h264")).shouldNotBeEmpty()
        }
    }
})
