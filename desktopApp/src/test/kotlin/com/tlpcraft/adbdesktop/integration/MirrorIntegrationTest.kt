package com.tlpcraft.adbdesktop.integration

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.model.MirrorFrame
import com.tlpcraft.adbdesktop.core.domain.model.MirrorInput
import com.tlpcraft.adbdesktop.core.domain.model.MirrorPoint
import com.tlpcraft.adbdesktop.core.domain.model.TouchAction
import com.tlpcraft.adbdesktop.core.domain.repository.ScreenRepository
import com.tlpcraft.adbdesktop.di.appModules
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.dsl.koinApplication

private val STREAM_TIME = 5.seconds
private val CLEANUP_TIME = 3.seconds
private val SWIPE_STEP_TIME = 16.milliseconds
private const val SWIPE_STEPS = 20

private fun adb(serial: String, vararg args: String): String {
    val process = ProcessBuilder(listOf("adb", "-s", serial) + args).redirectErrorStream(true).start()
    return process.inputStream.bufferedReader().readText().also { process.waitFor() }
}

private fun MirrorFrame.toImage() = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).also { image ->
    for (y in 0 until height) {
        for (x in 0 until width) {
            val offset = (y * width + x) * 4
            val (blue, green, red) = (0..2).map { pixels[offset + it].toInt() and 0xFF }
            image.setRGB(x, y, (red shl 16) or (green shl 8) or blue)
        }
    }
}

@EnabledIf(DeviceConfigured::class)
class MirrorIntegrationTest : FunSpec({
    val serial = System.getenv("ADB_TEST_SERIAL").orEmpty()
    val koin = koinApplication { modules(appModules) }.koin

    test("streams real frames and leaves nothing behind") {
        val started = TimeSource.Monotonic.markNow()
        var count = 0
        var firstFrameAfter = Duration.ZERO
        var last: MirrorFrame? = null
        withTimeoutOrNull(STREAM_TIME) {
            koin.get<ScreenRepository>().observeMirror(serial, emptyFlow())
                .map { (it as? Outcome.Success)?.data ?: error("Mirror failed: $it") }
                .collect { frame ->
                    if (count++ == 0) firstFrameAfter = started.elapsedNow()
                    last = frame
                }
        }

        val frame = last.shouldNotBeNull()
        println("mirror: first frame after $firstFrameAfter, $count frames by $STREAM_TIME, ${frame.width}x${frame.height}")
        System.getenv("MIRROR_DUMP")?.let { ImageIO.write(frame.toImage(), "png", File(it)) }
        count shouldBeGreaterThan 0
        frame.pixels.size shouldBe frame.width * frame.height * 4

        delay(CLEANUP_TIME)
        adb(serial, "forward", "--list") shouldNotContain "scrcpy_"
        adb(serial, "shell", "ls", "/data/local/tmp") shouldNotContain "adb-desktop-scrcpy"
        adb(serial, "shell", "ps", "-A", "-o", "ARGS") shouldNotContain "com.genymobile.scrcpy"
    }

    test("a swipe down from the top edge opens the notification shade") {
        adb(serial, "shell", "cmd", "statusbar", "collapse")
        val input = Channel<MirrorInput>(Channel.UNLIMITED)
        var hasSwiped = false
        try {
            withTimeoutOrNull(STREAM_TIME) {
                koin.get<ScreenRepository>().observeMirror(serial, input.receiveAsFlow())
                    .map { (it as? Outcome.Success)?.data ?: error("Mirror failed: $it") }
                    .collect { frame ->
                        if (!hasSwiped) {
                            hasSwiped = true
                            launch { swipeDown(frame, input) }
                        }
                    }
            }
            hasSwiped shouldBe true
            adb(serial, "shell", "dumpsys", "window").lineSequence().first { "mCurrentFocus" in it } shouldContain "NotificationShade"
        } finally {
            adb(serial, "shell", "cmd", "statusbar", "collapse")
        }
    }
})

private suspend fun swipeDown(frame: MirrorFrame, input: Channel<MirrorInput>) {
    val x = frame.width / 2

    fun at(y: Int) = MirrorPoint(x, y, frame.width, frame.height)
    input.send(MirrorInput.Touch(TouchAction.Down, at(0)))
    for (step in 1..SWIPE_STEPS) {
        delay(SWIPE_STEP_TIME)
        input.send(MirrorInput.Touch(TouchAction.Move, at(frame.height * step / SWIPE_STEPS * 2 / 3)))
    }
    input.send(MirrorInput.Touch(TouchAction.Up, at(frame.height * 2 / 3)))
}
