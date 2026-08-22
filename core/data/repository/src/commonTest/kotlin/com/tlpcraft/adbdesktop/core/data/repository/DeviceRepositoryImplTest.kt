package com.tlpcraft.adbdesktop.core.data.repository

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakeAdbDeviceDataSource
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakePreferencesDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.DeviceDto
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.ConnectionType
import com.tlpcraft.adbdesktop.core.domain.model.Device
import com.tlpcraft.adbdesktop.core.domain.model.DeviceState
import com.tlpcraft.adbdesktop.core.domain.model.WirelessEndpoint
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first

class DeviceRepositoryImplTest : FunSpec({
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    afterSpec { scope.cancel() }

    test("maps states, transports and model names") {
        val source = FakeAdbDeviceDataSource().apply {
            devices = listOf(
                DeviceDto("emulator-5554", "device", model = "sdk_gphone64_x86_64"),
                DeviceDto("192.168.1.42:5555", "offline"),
                DeviceDto("adb-R5CT10ABCDE-xYz12._adb-tls-connect._tcp", "device"),
                DeviceDto("R5CT10ABCDE", "unauthorized"),
                DeviceDto("FA7AB1A02345", "no permissions"),
                DeviceDto("0123", "host"),
            )
        }

        DeviceRepositoryImpl(source, FakePreferencesDataSource(), scope).observeDevices().test {
            (awaitItem() as Outcome.Success).data shouldContainExactly listOf(
                Device("emulator-5554", DeviceState.ONLINE, ConnectionType.EMULATOR, model = "sdk gphone64 x86 64"),
                Device("192.168.1.42:5555", DeviceState.OFFLINE, ConnectionType.WIRELESS),
                Device("adb-R5CT10ABCDE-xYz12._adb-tls-connect._tcp", DeviceState.ONLINE, ConnectionType.WIRELESS),
                Device("R5CT10ABCDE", DeviceState.UNAUTHORIZED, ConnectionType.USB),
                Device("FA7AB1A02345", DeviceState.NO_PERMISSIONS, ConnectionType.USB),
                Device("0123", DeviceState.UNKNOWN, ConnectionType.USB),
            )
        }
    }

    test("a missing adb becomes a typed error and tracking restarts when the path changes") {
        val source = FakeAdbDeviceDataSource().apply { trackingError = AdbException.ExecutableNotFound(listOf("/usr/bin/adb")) }
        val preferences = FakePreferencesDataSource()

        DeviceRepositoryImpl(source, preferences, scope).observeDevices().test {
            awaitItem() shouldBe Outcome.Failure(DeviceError.AdbNotFound(listOf("/usr/bin/adb")))

            source.trackingError = null
            preferences.writeAdbPath("/opt/adb")

            awaitItem() shouldBe Outcome.Success(emptyList())
            source.trackingStarts shouldBe 2
        }
    }

    test("every observer shares one tracking process") {
        val source = FakeAdbDeviceDataSource()
        val repository = DeviceRepositoryImpl(source, FakePreferencesDataSource(), scope)

        repository.observeDevices().test {
            awaitItem()

            repository.observeDevices().first()

            source.trackingStarts shouldBe 1
        }
    }

    test("tracking recovers on its own after a failure") {
        val source = FakeAdbDeviceDataSource().apply { trackingError = AdbException.ExecutableNotFound(listOf("/usr/bin/adb")) }

        DeviceRepositoryImpl(source, FakePreferencesDataSource(), scope, retryInterval = 5.milliseconds).observeDevices().test {
            awaitItem() shouldBe Outcome.Failure(DeviceError.AdbNotFound(listOf("/usr/bin/adb")))

            source.trackingError = null

            awaitItem() shouldBe Outcome.Success(emptyList())
        }
    }

    test("connect failures keep adb's reason") {
        val source = FakeAdbDeviceDataSource().apply { connectError = AdbException.ConnectFailed("10.0.0.2:5555", "Connection refused") }

        DeviceRepositoryImpl(source, FakePreferencesDataSource(), scope).connect(WirelessEndpoint("10.0.0.2", 5555)) shouldBe
            Outcome.Failure(DeviceError.ConnectionFailed("10.0.0.2:5555", "Connection refused"))
    }
})
