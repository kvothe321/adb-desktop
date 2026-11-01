package com.tlpcraft.adbdesktop.core.data.source.adb.mirror

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.zip.ZipInputStream

class ScrcpyServerCommandTest : FunSpec({
    test("the session id names the socket and jar in the form the server expects") {
        ScrcpyServerCommand.socketName(0x2a) shouldBe "scrcpy_0000002a"
        ScrcpyServerCommand.remotePath(0x2a) shouldBe "/data/local/tmp/adb-desktop-scrcpy-0000002a.jar"
    }

    test("the stream is video only and keeps its frame headers") {
        val arguments = ScrcpyServerCommand.shellArguments(1)

        arguments shouldContainAll listOf("audio=false", "control=false", "tunnel_forward=true", "video_codec=h264", "send_stream_meta=false")
        arguments.none { it.startsWith("raw_stream") || it.startsWith("send_frame_meta") } shouldBe true
    }

    test("the bundled server is a dex jar") {
        val resource = checkNotNull(ScrcpyServerCommandTest::class.java.getResourceAsStream(ScrcpyServerCommand.RESOURCE))

        ZipInputStream(resource).use { zip ->
            generateSequence { zip.nextEntry }.firstOrNull { it.name == "classes.dex" } shouldNotBe null
        }
    }
})
