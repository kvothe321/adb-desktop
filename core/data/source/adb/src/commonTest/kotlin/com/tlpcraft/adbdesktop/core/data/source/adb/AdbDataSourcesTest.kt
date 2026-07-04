package com.tlpcraft.adbdesktop.core.data.source.adb

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.DeviceDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.DevicePropertiesDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageActionDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageFilterDto
import com.tlpcraft.adbdesktop.core.data.source.adb.fake.FakeCommandExecutor
import com.tlpcraft.adbdesktop.core.data.source.adb.fake.testAdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.fixtures.AdbOutputs
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class AdbDataSourcesTest : FunSpec({
    context("devices") {
        test("tracks devices across frames and drops repeated listings") {
            val listing = "emulator-5554          device transport_id:1\n"
            val frame = listing.length.toString(16).padStart(4, '0') + listing
            val executor = FakeCommandExecutor().apply {
                respondStream("track-devices", "-l", chunks = listOf(AdbOutputs.TRACK_DEVICES_EMPTY_FRAME + frame.take(3), frame.drop(3), frame))
            }

            AdbDeviceDataSourceImpl(testAdbClient(executor)).trackDevices().test {
                awaitItem() shouldBe emptyList()
                awaitItem() shouldBe listOf(DeviceDto("emulator-5554", "device", transportId = 1))
                awaitComplete()
            }
        }

        test("connect fails with adb's reason even though adb exits with 0") {
            val executor = FakeCommandExecutor().apply { respond("connect", "127.0.0.1:5599", stdout = AdbOutputs.CONNECT_REFUSED) }

            val error = shouldThrow<AdbException.ConnectFailed> { AdbDeviceDataSourceImpl(testAdbClient(executor)).connect("127.0.0.1:5599") }

            error.reason shouldBe "Connection refused"
        }

        test("connect succeeds") {
            val executor = FakeCommandExecutor().apply { respond("connect", "192.168.1.42:5555", stdout = AdbOutputs.CONNECT_SUCCESS) }

            AdbDeviceDataSourceImpl(testAdbClient(executor)).connect("192.168.1.42:5555")
        }

        test("pair reports a wrong code") {
            val executor = FakeCommandExecutor().apply {
                respond("pair", "192.168.1.42:37123", "123456", stdout = AdbOutputs.PAIR_WRONG_CODE, exitCode = 1)
            }

            shouldThrow<AdbException.PairFailed> { AdbDeviceDataSourceImpl(testAdbClient(executor)).pair("192.168.1.42:37123", "123456") }
        }
    }

    context("properties") {
        test("reads properties, battery and cpu in few round trips") {
            val executor = FakeCommandExecutor().apply {
                respond("-s", "pixel", "shell", "getprop", stdout = AdbOutputs.GETPROP)
                respond("-s", "pixel", "shell", "dumpsys", "battery", stdout = AdbOutputs.DUMPSYS_BATTERY)
            }
            val source = AdbDevicePropertiesDataSourceImpl(testAdbClient(executor))

            source.readProperties("pixel") shouldBe DevicePropertiesDto(manufacturer = "samsung", model = "SM-A525F", androidVersion = "14")
            source.readBatteryLevel("pixel") shouldBe 87
        }
    }

    context("packages") {
        test("passes the filter flag to pm") {
            val executor = FakeCommandExecutor().apply {
                respond("-s", "pixel", "shell", "pm", "list", "packages", "-3", stdout = AdbOutputs.PM_LIST_PACKAGES)
            }

            AdbPackageDataSourceImpl(testAdbClient(executor)).listPackages("pixel", PackageFilterDto.THIRD_PARTY).size shouldBe 3
        }

        test("sums split apk sizes into the code size") {
            val pkg = "com.example.notes"
            val apks = AdbOutputs.PM_PATH_SPLIT.lines().map { it.removePrefix("package:") }
            val executor = FakeCommandExecutor().apply {
                respond("-s", "pixel", "shell", "dumpsys", "package", pkg, stdout = AdbOutputs.DUMPSYS_PACKAGE)
                respond("-s", "pixel", "shell", "cmd", "package", "get-app-sizes", pkg, stdout = AdbOutputs.APP_SIZES)
                respond("-s", "pixel", "shell", "pm", "path", pkg, stdout = AdbOutputs.PM_PATH_SPLIT)
                respond("-s", "pixel", "shell", "stat", "-c", "%s", *apks.toTypedArray(), stdout = AdbOutputs.STAT_SIZES)
            }

            val details = AdbPackageDataSourceImpl(testAdbClient(executor)).readPackageDetails("pixel", pkg)

            details.versionName shouldBe "2.4.1"
            details.codeSizeBytes shouldBe 41_943_040L + 1_048_576L
            details.dataSizeBytes shouldBe 12_582_912L
        }

        test("a pm failure printed on stdout fails the action") {
            val executor = FakeCommandExecutor().apply {
                respond("-s", "pixel", "shell", "pm", "uninstall", "com.example.notes", stdout = AdbOutputs.PM_FAILURE)
            }

            shouldThrow<AdbException.CommandFailed> {
                AdbPackageDataSourceImpl(testAdbClient(executor)).runAction("pixel", "com.example.notes", PackageActionDto.UNINSTALL)
            }
        }

        test("force stop runs am") {
            val executor = FakeCommandExecutor()

            AdbPackageDataSourceImpl(testAdbClient(executor)).runAction("pixel", "com.example.notes", PackageActionDto.FORCE_STOP)

            executor.executed shouldContain listOf("-s", "pixel", "shell", "am", "force-stop", "com.example.notes")
            executor.executed shouldContainExactly listOf(listOf("-s", "pixel", "shell", "am", "force-stop", "com.example.notes"))
        }
    }
})
