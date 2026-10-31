package com.tlpcraft.adbdesktop.core.data.source.adb

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageActionDto
import com.tlpcraft.adbdesktop.core.data.source.adb.fake.FakeCommandExecutor
import com.tlpcraft.adbdesktop.core.data.source.adb.fake.testAdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.fixtures.AdbOutputs
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class AdbHostOperationsTest : FunSpec({
    test("logcat batches entries and reassembles lines cut by chunks") {
        val executor = FakeCommandExecutor().apply {
            respondStream(
                "-s",
                "pixel",
                "logcat",
                "-v",
                "threadtime",
                "-T",
                "1000",
                chunks = listOf("--------- beginning of main\n10-31 23:05:50.598  1537  4112 I Ta", "g: hello\n"),
            )
        }

        AdbLogcatDataSourceImpl(testAdbClient(executor)).streamLogs("pixel").test {
            awaitItem().single().message shouldBe "hello"
            awaitComplete()
        }
    }

    context("files") {
        test("lists with a trailing slash so links to directories are followed, and quotes the path") {
            val executor = FakeCommandExecutor().apply { respond("-s", "pixel", "shell", "ls", "-la", "'/sdcard/a b/'", stdout = AdbOutputs.LS_SPACES) }

            AdbFileDataSourceImpl(testAdbClient(executor)).list("pixel", "/sdcard/a b").single().name shouldBe "it's a file.txt"
        }

        test("the root directory is not doubled") {
            val executor = FakeCommandExecutor().apply { respond("-s", "pixel", "shell", "ls", "-la", "'/'", stdout = AdbOutputs.LS_ROOT) }

            AdbFileDataSourceImpl(testAdbClient(executor)).list("pixel", "/").size shouldBe 4
        }

        test("a partly unreadable directory still lists what it can") {
            val executor = FakeCommandExecutor().apply {
                respond("-s", "pixel", "shell", "ls", "-la", "'/'", stdout = AdbOutputs.LS_ROOT, stderr = "ls: /x: Permission denied", exitCode = 1)
            }

            AdbFileDataSourceImpl(testAdbClient(executor)).list("pixel", "/").size shouldBe 4
        }

        test("a missing directory fails with adb's message") {
            val executor = FakeCommandExecutor().apply {
                respond("-s", "pixel", "shell", "ls", "-la", "'/nonexistent/'", stderr = AdbOutputs.LS_MISSING, exitCode = 1)
            }

            shouldThrow<AdbException.CommandFailed> { AdbFileDataSourceImpl(testAdbClient(executor)).list("pixel", "/nonexistent") }.output shouldBe
                "ls: /nonexistent: No such file or directory"
        }

        test("deleting quotes the path") {
            val executor = FakeCommandExecutor()

            AdbFileDataSourceImpl(testAdbClient(executor)).delete("pixel", "/sdcard/it's")

            executor.executed shouldContain listOf("-s", "pixel", "shell", "rm", "-rf", "'/sdcard/it'\\''s'")
        }
    }

    context("screen") {
        test("a screenshot returns the raw bytes") {
            val png = byteArrayOf(0x89.toByte(), 0x50)
            val executor = FakeCommandExecutor().apply { respondBinary("-s", "pixel", "exec-out", "screencap", "-p", stdout = png) }

            AdbScreenDataSourceImpl(testAdbClient(executor)).captureScreenshot("pixel").toList() shouldBe png.toList()
        }

        test("moving a recording to the host removes it from the device") {
            val executor = FakeCommandExecutor()

            AdbScreenDataSourceImpl(testAdbClient(executor)).moveToHost("pixel", "/sdcard/r.mp4", "/home/me/r.mp4")

            executor.executed shouldContainExactly listOf(
                listOf("-s", "pixel", "pull", "/sdcard/r.mp4", "/home/me/r.mp4"),
                listOf("-s", "pixel", "shell", "rm", "-f", "'/sdcard/r.mp4'"),
            )
        }
    }

    context("package actions") {
        test("clearing the cache runs inside the app") {
            val executor = FakeCommandExecutor()

            AdbPackageDataSourceImpl(testAdbClient(executor)).runAction("pixel", "com.example.notes", PackageActionDto.CLEAR_CACHE)

            executor.executed shouldContainExactly listOf(
                listOf("-s", "pixel", "shell", "run-as", "com.example.notes", "sh", "-c", "'rm -rf cache/* code_cache/*'"),
            )
        }

        test("a command that never returns times out") {
            val executor = FakeCommandExecutor().apply { hangs = true }

            runTest {
                shouldThrow<AdbException.CommandFailed> {
                    AdbPackageDataSourceImpl(testAdbClient(executor)).runAction("pixel", "com.example.notes", PackageActionDto.FORCE_STOP)
                }.output shouldBe "Timed out"
            }
        }
    }

    context("install") {
        test("several APKs install as one app") {
            val executor = FakeCommandExecutor().apply { respond("-s", "pixel", "install-multiple", "-r", "a.apk", "b.apk", stdout = "Success\n") }

            AdbPackageDataSourceImpl(testAdbClient(executor)).install("pixel", listOf("a.apk", "b.apk"))
        }

        test("a refused install reports adb's reason") {
            val executor = FakeCommandExecutor().apply {
                respond(
                    "-s",
                    "pixel",
                    "install",
                    "-r",
                    "a.apk",
                    stderr = "adb: failed to install a.apk: Failure [INSTALL_FAILED_OLDER_SDK: nope]",
                    exitCode = 1,
                )
            }

            shouldThrow<AdbException.CommandFailed> { AdbPackageDataSourceImpl(testAdbClient(executor)).install("pixel", listOf("a.apk")) }.output shouldBe
                "INSTALL_FAILED_OLDER_SDK: nope"
        }
    }

    test("adb info reports the resolved path and version") {
        val executor = FakeCommandExecutor().apply { respond("version", stdout = AdbOutputs.ADB_VERSION) }

        AdbInfoDataSourceImpl(testAdbClient(executor)).readInfo().version shouldBe "37.0.0p1-android-tools"
    }

    test("the shell session sends each command as a line") {
        val executor = FakeCommandExecutor().apply { respondStream("-s", "pixel", "shell", chunks = listOf("hello\n")) }

        AdbShellDataSourceImpl(testAdbClient(executor)).openSession("pixel", flowOf("echo hello")).test {
            awaitItem() shouldBe "hello\n"
            awaitComplete()
        }
    }
})
