package com.tlpcraft.adbdesktop.core.domain.usecase

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.WirelessEndpoint
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ConnectWirelessDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.PairWirelessDeviceUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.testing.offlineDevice
import com.tlpcraft.adbdesktop.core.testing.onlineDevice
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class DeviceUseCasesTest : FunSpec({
    test("devices are listed online first, then by serial") {
        val repository = FakeDeviceRepository(listOf(offlineDevice("a-offline"), onlineDevice("z-online"), onlineDevice("b-online")))

        ObserveDevicesUseCase(repository)(Unit).test {
            val devices = (awaitItem() as Outcome.Success).data
            devices.map { it.serial } shouldContainExactly listOf("b-online", "z-online", "a-offline")
        }
    }

    context("selected device") {
        test("resolves to the selected online device") {
            val devices = FakeDeviceRepository(listOf(onlineDevice("pixel")))
            val selection = FakeDeviceSelectionRepository("pixel")

            ObserveSelectedDeviceUseCase(ObserveDevicesUseCase(devices), selection)(Unit).test {
                awaitItem()?.serial shouldBe "pixel"
            }
        }

        test("is null while the selected device is offline and returns when it reconnects") {
            val devices = FakeDeviceRepository(listOf(onlineDevice("pixel")))
            val selection = FakeDeviceSelectionRepository("pixel")

            ObserveSelectedDeviceUseCase(ObserveDevicesUseCase(devices), selection)(Unit).test {
                awaitItem()?.serial shouldBe "pixel"
                devices.devices.value = Outcome.Success(listOf(offlineDevice("pixel")))
                awaitItem().shouldBeNull()
                devices.devices.value = Outcome.Success(listOf(onlineDevice("pixel")))
                awaitItem()?.serial shouldBe "pixel"
            }
        }

        test("is null when device tracking fails") {
            val devices = FakeDeviceRepository().apply { devices.value = Outcome.Failure(DeviceError.AdbNotFound(emptyList())) }

            ObserveSelectedDeviceUseCase(ObserveDevicesUseCase(devices), FakeDeviceSelectionRepository("pixel"))(Unit).test {
                awaitItem().shouldBeNull()
            }
        }
    }

    context("connect") {
        test("adds the default adb port") {
            val repository = FakeDeviceRepository()

            ConnectWirelessDeviceUseCase(repository)("192.168.1.42") shouldBe Outcome.Success(Unit)
            repository.connectedEndpoints shouldContainExactly listOf(WirelessEndpoint("192.168.1.42", 5555))
        }

        test("rejects malformed endpoints without calling adb") {
            val repository = FakeDeviceRepository()

            ConnectWirelessDeviceUseCase(repository)("not a host") shouldBe Outcome.Failure(DeviceError.InvalidEndpoint("not a host"))
            repository.connectedEndpoints.shouldBeEmpty()
        }
    }

    context("pair") {
        test("requires an explicit port") {
            val repository = FakeDeviceRepository()

            PairWirelessDeviceUseCase(repository)(PairWirelessDeviceUseCase.Params("192.168.1.42", "123456")) shouldBe
                Outcome.Failure(DeviceError.InvalidEndpoint("192.168.1.42"))
        }

        test("requires a six digit code") {
            val repository = FakeDeviceRepository()

            PairWirelessDeviceUseCase(repository)(PairWirelessDeviceUseCase.Params("192.168.1.42:37123", "12ab56")) shouldBe
                Outcome.Failure(DeviceError.InvalidPairingCode)
            repository.pairings.shouldBeEmpty()
        }

        test("pairs with a trimmed code") {
            val repository = FakeDeviceRepository()

            PairWirelessDeviceUseCase(repository)(PairWirelessDeviceUseCase.Params("192.168.1.42:37123", " 123456 ")) shouldBe Outcome.Success(Unit)
            repository.pairings shouldContainExactly listOf(WirelessEndpoint("192.168.1.42", 37123) to "123456")
        }
    }
})
