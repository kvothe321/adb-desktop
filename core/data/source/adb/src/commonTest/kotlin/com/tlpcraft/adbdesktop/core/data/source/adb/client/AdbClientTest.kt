package com.tlpcraft.adbdesktop.core.data.source.adb.client

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException
import com.tlpcraft.adbdesktop.core.data.source.adb.fake.FakeCommandExecutor
import com.tlpcraft.adbdesktop.core.data.source.adb.fake.testAdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.fixtures.AdbOutputs
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class AdbClientTest : FunSpec({
    test("targets the device and runs shell commands") {
        val executor = FakeCommandExecutor().apply { respond("-s", "pixel", "shell", "getprop", stdout = "props") }

        testAdbClient(executor).shell("pixel", "getprop") shouldBe "props"
        executor.executed shouldContainExactly listOf(listOf("-s", "pixel", "shell", "getprop"))
    }

    test("a missing device becomes DeviceNotFound") {
        val executor = FakeCommandExecutor().apply {
            respond("-s", "nonexistent", "shell", "getprop", stderr = AdbOutputs.DEVICE_NOT_FOUND, exitCode = 1)
        }

        shouldThrow<AdbException.DeviceNotFound> { testAdbClient(executor).shell("nonexistent", "getprop") }.serial shouldBe "nonexistent"
    }

    test("other failures keep the command and output") {
        val executor = FakeCommandExecutor().apply { respond("-s", "pixel", "shell", "boom", stderr = "boom: not found", exitCode = 127) }

        val error = shouldThrow<AdbException.CommandFailed> { testAdbClient(executor).shell("pixel", "boom") }

        error.exitCode shouldBe 127
        error.output shouldBe "boom: not found"
    }
})
