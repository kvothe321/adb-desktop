package com.tlpcraft.adbdesktop.core.data.source.adb.parser

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.CpuSnapshotDto
import com.tlpcraft.adbdesktop.core.data.source.adb.fixtures.AdbOutputs
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class OutputParsersTest : FunSpec({
    context("wireless") {
        test("connect success and already-connected both count as connected") {
            WirelessOutputParser.isConnectSuccess(AdbOutputs.CONNECT_SUCCESS).shouldBeTrue()
            WirelessOutputParser.isConnectSuccess(AdbOutputs.CONNECT_ALREADY).shouldBeTrue()
            WirelessOutputParser.isConnectSuccess(AdbOutputs.CONNECT_REFUSED).shouldBeFalse()
        }

        test("extracts the failure reason") {
            WirelessOutputParser.failureReason(AdbOutputs.CONNECT_REFUSED) shouldBe "Connection refused"
            WirelessOutputParser.failureReason(AdbOutputs.CONNECT_NO_ROUTE) shouldBe "cannot connect to 192.168.1.99:5555: No route to host (113)"
            WirelessOutputParser.failureReason(AdbOutputs.PAIR_WRONG_CODE) shouldBe "Wrong password or connection was dropped."
            WirelessOutputParser.failureReason(AdbOutputs.PAIR_PROTOCOL_FAULT) shouldBe "protocol fault (couldn't read status message): Success"
        }

        test("disconnect and pair outcomes") {
            WirelessOutputParser.isDisconnectSuccess(AdbOutputs.DISCONNECT_SUCCESS).shouldBeTrue()
            WirelessOutputParser.isDisconnectSuccess(AdbOutputs.DISCONNECT_UNKNOWN).shouldBeFalse()
            WirelessOutputParser.isPairSuccess(AdbOutputs.PAIR_SUCCESS).shouldBeTrue()
            WirelessOutputParser.isPairSuccess(AdbOutputs.PAIR_WRONG_CODE).shouldBeFalse()
        }
    }

    test("getprop keeps empty values") {
        val properties = PropertiesParser.parse(AdbOutputs.GETPROP)

        properties shouldContain ("ro.product.model" to "SM-A525F")
        properties shouldContain ("persist.sys.timezone" to "")
    }

    test("battery level") {
        BatteryParser.parseLevel(AdbOutputs.DUMPSYS_BATTERY) shouldBe 87
        BatteryParser.parseLevel("").shouldBeNull()
    }

    context("cpu snapshot") {
        test("idle and iowait are not busy; the max frequency is taken across cores") {
            CpuSnapshotParser.parse(AdbOutputs.CPU_SNAPSHOT) shouldBe CpuSnapshotDto(
                busyJiffies = 14725 + 1022 + 9480 + 211,
                totalJiffies = 14725 + 1022 + 9480 + 120480 + 512 + 211,
                coreCount = 8,
                maxFrequencyKHz = 2_400_000,
            )
        }

        test("devices hiding cpufreq report no frequency") {
            CpuSnapshotParser.parse(AdbOutputs.CPU_SNAPSHOT_NO_FREQUENCY).maxFrequencyKHz.shouldBeNull()
        }
    }

    context("packages") {
        test("package list and apk paths") {
            PackageListParser.parse(AdbOutputs.PM_LIST_PACKAGES) shouldContainExactly listOf(
                "com.android.settings",
                "com.google.android.gms",
                "com.example.notes",
            )
            PackageListParser.parse(AdbOutputs.PM_PATH_SPLIT).size shouldBe 2
        }

        test("package dump") {
            PackageDumpParser.parse(AdbOutputs.DUMPSYS_PACKAGE) shouldBe PackageDump(
                versionName = "2.4.1",
                versionCode = 241,
                minSdk = 26,
                targetSdk = 34,
                firstInstallTime = "2023-03-12 09:15:00",
                lastUpdateTime = "2024-11-20 14:30:22",
            )
        }

        test("storage sizes") {
            StorageParser.parseTotalSize(AdbOutputs.STAT_SIZES) shouldBe 41_943_040L + 1_048_576L
            StorageParser.parseTotalSize("stat: permission denied").shouldBeNull()
            StorageParser.parseAppSizes(AdbOutputs.APP_SIZES) shouldBe StorageParser.AppSizes(dataBytes = 12_582_912, cacheBytes = 2_097_152)
        }

        test("pm failures are detected on stdout") {
            ShellFailureParser.isFailure(AdbOutputs.PM_FAILURE).shouldBeTrue()
            ShellFailureParser.isFailure("Success").shouldBeFalse()
        }
    }
})
