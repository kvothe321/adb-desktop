package com.tlpcraft.adbdesktop.integration

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.common.getOrNull
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFileType
import com.tlpcraft.adbdesktop.core.domain.model.WirelessEndpoint
import com.tlpcraft.adbdesktop.core.domain.repository.AdbSettingsRepository
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import com.tlpcraft.adbdesktop.core.domain.repository.CpuRepository
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceInfoRepository
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceRepository
import com.tlpcraft.adbdesktop.core.domain.repository.FilesRepository
import com.tlpcraft.adbdesktop.core.domain.repository.LogcatRepository
import com.tlpcraft.adbdesktop.core.domain.repository.ScreenRepository
import com.tlpcraft.adbdesktop.core.domain.repository.ShellRepository
import com.tlpcraft.adbdesktop.di.appModules
import io.kotest.assertions.withClue
import io.kotest.core.annotation.Condition
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import java.io.File
import java.nio.file.Files
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeout
import org.koin.dsl.koinApplication

private val testSerial: String? = System.getenv("ADB_TEST_SERIAL")
private val TIMEOUT = 60.seconds
private val RECORDING_LENGTH = 3.seconds

private fun <T> Outcome<T, DeviceError>.orFail(): T = withClue(this) { getOrNull().shouldNotBeNull() }

class DeviceConfigured : Condition {
    override fun evaluate(kclass: KClass<out Spec>): Boolean = testSerial != null
}

/**
 * Runs the real app graph against a real device or emulator:
 * `ADB_TEST_SERIAL=emulator-5554 ./gradlew :desktopApp:test --rerun --tests '*DeviceIntegrationTest'`.
 * Without that variable it does nothing, so `check` and CI never need a device.
 */
@EnabledIf(DeviceConfigured::class)
class DeviceIntegrationTest : FunSpec({
    val serial = testSerial.orEmpty()
    val koin = koinApplication { modules(appModules) }.koin

    test("the device is tracked with its model") {
        val devices = withTimeout(TIMEOUT) { koin.get<DeviceRepository>().observeDevices().first() }.orFail()

        val device = devices.first { it.serial == serial }
        device.isOnline.shouldBeTrue()
        device.model.shouldNotBeNull()
    }

    test("adb reports its path and version") {
        val info = koin.get<AdbSettingsRepository>().readAdbInfo().orFail()

        File(info.path).exists().shouldBeTrue()
        info.version.shouldNotBeNull()
    }

    test("device info and cpu stats are read") {
        val info = withTimeout(TIMEOUT) { koin.get<DeviceInfoRepository>().observeDeviceInfo(serial).first() }.orFail()
        val cpu = withTimeout(TIMEOUT) { koin.get<CpuRepository>().observeCpuInfo(serial).first() }.orFail()

        info.androidVersion.isNotBlank().shouldBeTrue()
        (info.batteryLevel in 0..100).shouldBeTrue()
        cpu.coreCount shouldBeGreaterThan 0
    }

    test("connecting to a closed port reports adb's reason") {
        val result = koin.get<DeviceRepository>().connect(WirelessEndpoint("127.0.0.1", 5599))

        result.shouldBeInstanceOf<Outcome.Failure<DeviceError>>().error.shouldBeInstanceOf<DeviceError.ConnectionFailed>()
    }

    test("wireless discovery answers") {
        koin.get<DeviceRepository>().discoverWirelessServices().orFail()
    }

    context("files") {
        val files = koin.get<FilesRepository>()
        val directory = "/sdcard/Download/adb desktop's test"

        test("the root lists, including entries the shell cannot read") {
            files.list(serial, "/").orFail().map { it.name } shouldContain "system"
        }

        test("links to directories can be opened") {
            val sdcard = files.list(serial, "/").orFail().first { it.name == "sdcard" }

            sdcard.type shouldBe RemoteFileType.LINK
            files.list(serial, sdcard.path).orFail().shouldNotBeEmpty()
        }

        test("a missing directory fails with the device's message") {
            val result = files.list(serial, "/does/not/exist")

            result.shouldBeInstanceOf<Outcome.Failure<DeviceError>>().error.message shouldContain "No such file"
        }

        test("create, upload, list, download and delete survive spaces and quotes in paths") {
            val local = Files.createTempDirectory("adb-desktop").toFile()
            val upload = File(local, "it's a file.txt").apply { writeText("hello device") }
            val target = Files.createTempDirectory("adb-desktop-pull").toFile()

            files.createDirectory(serial, directory).orFail()
            files.upload(serial, listOf(upload.path), directory).orFail()

            val listed = files.list(serial, directory).orFail().single()
            listed.name shouldBe upload.name
            listed.sizeBytes shouldBe upload.length()

            files.download(serial, listed.path, target.path).orFail()
            File(target, upload.name).readText() shouldBe "hello device"

            files.delete(serial, directory).orFail()
            files.list(serial, "/sdcard/Download").orFail().map { it.name }.contains("adb desktop's test") shouldBe false
        }
    }

    test("logcat streams and parses real entries") {
        val batch = withTimeout(TIMEOUT) { koin.get<LogcatRepository>().observeLogs(serial).first() }.orFail()

        batch.shouldNotBeEmpty()
        batch.first().tag.isNotEmpty().shouldBeTrue()
    }

    test("the shell keeps state between commands and ends on exit") {
        val commands = Channel<String>(Channel.UNLIMITED)
        val script = listOf("cd /sdcard", "pwd", "echo done-marker", "exit")
        script.forEach { commands.trySend(it) }

        val output = withTimeout(TIMEOUT) {
            koin.get<ShellRepository>().runSession(serial, commands.receiveAsFlow()).toList()
        }.joinToString("") { it.orFail() }

        output shouldContain "/sdcard"
        output shouldContain "done-marker"
    }

    test("a screenshot is a real PNG") {
        val png = koin.get<ScreenRepository>().captureScreenshot(serial).orFail()

        png.take(4).map { it.toInt() and 0xFF } shouldBe listOf(0x89, 0x50, 0x4E, 0x47)
        png.size shouldBeGreaterThan 1_000
    }

    test("a recording stops cleanly and arrives as a playable mp4") {
        val screen = koin.get<ScreenRepository>()
        val target = File.createTempFile("adb-desktop", ".mp4")

        coroutineScope {
            val recording = async { screen.recordScreen(serial) }
            delay(RECORDING_LENGTH)
            screen.stopRecording(serial).orFail()
            withTimeout(TIMEOUT) { recording.await() }.orFail()
        }
        screen.saveRecording(serial, target.path).orFail()

        target.length().toInt() shouldBeGreaterThan 1_000
        target.readBytes().copyOfRange(4, 8).decodeToString() shouldBe "ftyp"
        koin.get<FilesRepository>().list(serial, "/sdcard").orFail().map { it.name }.contains("adb-desktop-recording.mp4") shouldBe false
    }

    context("apps") {
        val apps = koin.get<AppsRepository>()
        val packageName = "com.android.settings"

        test("system packages are listed and described") {
            apps.getApps(serial, AppFilter.SYSTEM).orFail().map { it.packageName } shouldContain packageName

            val details = apps.getAppDetails(serial, packageName).orFail()
            details.versionName.shouldNotBeNull()
            details.targetSdk.shouldNotBeNull()
            details.codeSizeBytes.shouldNotBeNull()
        }

        test("an app can be opened and force stopped") {
            apps.executeAction(serial, packageName, AppAction.OPEN).orFail()
            apps.executeAction(serial, packageName, AppAction.FORCE_STOP).orFail()
        }

        test("a broken APK is refused with a reason") {
            val fake = File.createTempFile("broken", ".apk").apply { writeText("not an apk") }

            val result = apps.install(serial, listOf(fake.path))

            result.shouldBeInstanceOf<Outcome.Failure<DeviceError>>().error.shouldBeInstanceOf<DeviceError.CommandFailed>().details shouldContain "INSTALL"
        }
    }
})
