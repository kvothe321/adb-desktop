package com.tlpcraft.adbdesktop.core.data.source.adb.parser

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.LogEntryDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MdnsServiceDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.RemoteFileDto
import com.tlpcraft.adbdesktop.core.data.source.adb.fixtures.AdbOutputs
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class StreamingParsersTest : FunSpec({
    context("line buffer") {
        test("keeps an unfinished line until its newline arrives") {
            val buffer = LineBuffer()

            buffer.feed("one\ntw") shouldContainExactly listOf("one")
            buffer.feed("o\r\n") shouldContainExactly listOf("two")
            buffer.feed("three").shouldBeEmpty()
        }
    }

    context("logcat") {
        val lines = AdbOutputs.LOGCAT.lines()

        test("buffer markers are not entries") {
            LogcatParser.parse(lines[0]).shouldBeNull()
        }

        test("parses every column") {
            LogcatParser.parse(lines[2]) shouldBe LogEntryDto(
                timestamp = "10-31 23:05:50.599",
                processId = 1254,
                threadId = 5024,
                level = 'W',
                tag = "NetworkScheduler.Stats",
                message = "(REDACTED) Task %s/%s finished executing. cause:%s",
            )
        }

        test("accepts empty messages, and tags with spaces and padding") {
            LogcatParser.parse(lines[3])?.message shouldBe ""

            val spaced = LogcatParser.parse(lines[4])
            spaced?.tag shouldBe "Spaced Tag"
            spaced?.message shouldBe "message: with colons"
        }
    }

    context("file list") {
        test("skips the current and parent entries, and resolves links") {
            FileListParser.parse(AdbOutputs.LS_ROOT).map { it.name } shouldContainExactly listOf("adb_keys", "apex", "data_mirror", "init.rc")
            FileListParser.parse(AdbOutputs.LS_ROOT).first().linkTarget shouldBe "/product/etc/security/adb_keys"
        }

        test("keeps entries that could not be read") {
            FileListParser.parse(AdbOutputs.LS_ROOT).first { it.name == "data_mirror" } shouldBe
                RemoteFileDto(name = "data_mirror", permissions = "d?????????", sizeBytes = null, modified = null)
        }

        test("device nodes have no size") {
            FileListParser.parse(AdbOutputs.LS_DEVICE_NODE).single().sizeBytes.shouldBeNull()
        }

        test("keeps spaces and quotes in names") {
            FileListParser.parse(AdbOutputs.LS_SPACES).single().name shouldBe "it's a file.txt"
        }

        test("error output has no entries") {
            FileListParser.parse(AdbOutputs.LS_MISSING).shouldBeEmpty()
        }
    }

    test("adb version") {
        AdbVersionParser.parse(AdbOutputs.ADB_VERSION) shouldBe "37.0.0p1-android-tools"
    }

    test("mdns services are deduplicated") {
        MdnsServicesParser.parse(AdbOutputs.MDNS_SERVICES) shouldContainExactly listOf(
            MdnsServiceDto("adb-EMULATOR36X2X12X0", "_adb._tcp", "10.0.2.16:5555"),
            MdnsServiceDto("adb-R5CT10ABCDE-xYz12", "_adb-tls-pairing._tcp", "192.168.1.42:41567"),
        )
    }

    test("install output") {
        InstallOutputParser.isSuccess("Performing Streamed Install\nSuccess\n").shouldBeTrue()
        InstallOutputParser.isSuccess("adb: failed to install a.apk: Failure [INSTALL_FAILED_OLDER_SDK: nope]").shouldBeFalse()
        InstallOutputParser.failureReason("adb: failed to install a.apk: Failure [INSTALL_FAILED_OLDER_SDK: nope]") shouldBe "INSTALL_FAILED_OLDER_SDK: nope"
    }

    test("shell quoting survives spaces and quotes") {
        "/sdcard/it's a file".shellQuoted() shouldBe "'/sdcard/it'\\''s a file'"
    }
})
