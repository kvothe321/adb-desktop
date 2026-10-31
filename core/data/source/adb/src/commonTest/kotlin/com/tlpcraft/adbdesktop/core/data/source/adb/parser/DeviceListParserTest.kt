package com.tlpcraft.adbdesktop.core.data.source.adb.parser

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.DeviceDto
import com.tlpcraft.adbdesktop.core.data.source.adb.fixtures.AdbOutputs
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class DeviceListParserTest : FunSpec({
    test("an empty listing has no devices") {
        DeviceListParser.parse(AdbOutputs.DEVICES_EMPTY).shouldBeEmpty()
    }

    test("parses every attribute of the long listing") {
        val devices = DeviceListParser.parse(AdbOutputs.DEVICES_LONG)

        devices.first() shouldBe DeviceDto(
            serial = "emulator-5554",
            state = "device",
            product = "sdk_gphone64_x86_64",
            model = "sdk_gphone64_x86_64",
            device = "emu64xa",
            transportId = 1,
        )
        devices[1].usbPath shouldBe "1-1"
    }

    test("keeps states and serials, including mDNS and no-permissions rows") {
        val devices = DeviceListParser.parse(AdbOutputs.DEVICES_LONG)

        devices.map { it.serial to it.state } shouldContainExactly listOf(
            "emulator-5554" to "device",
            "R5CT10ABCDE" to "device",
            "192.168.1.42:5555" to "offline",
            "adb-R5CT10ABCDE-xYz12._adb-tls-connect._tcp" to "device",
            "0123456789ABCDEF" to "unauthorized",
            "FA7AB1A02345" to "no permissions",
        )
        devices.last().transportId shouldBe 7
    }

    test("ignores daemon start messages and accepts tab separated short rows") {
        DeviceListParser.parse(AdbOutputs.DEVICES_WITH_DAEMON_START) shouldContainExactly listOf(DeviceDto("emulator-5554", "device"))
    }
})
