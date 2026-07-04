package com.tlpcraft.adbdesktop.feature.devices

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.core.domain.model.DeviceInfo
import com.tlpcraft.adbdesktop.core.domain.model.WirelessEndpoint
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ConnectWirelessDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.DisconnectDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveCpuInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDeviceInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.PairWirelessDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.SelectDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeAdbSettingsRepository
import com.tlpcraft.adbdesktop.core.testing.FakeCpuRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceInfoRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.testing.offlineDevice
import com.tlpcraft.adbdesktop.core.testing.onlineDevice
import com.tlpcraft.adbdesktop.feature.devices.contract.ConnectFormError
import com.tlpcraft.adbdesktop.feature.devices.contract.ConnectMode
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceListState
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceStats
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesFailure
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesIntent
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesSideEffect
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class DevicesViewModelTest : FunSpec({
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    class Fixture(
        val devices: FakeDeviceRepository = FakeDeviceRepository(listOf(onlineDevice("pixel", model = "Pixel 8"), offlineDevice("old-phone"))),
        val selection: FakeDeviceSelectionRepository = FakeDeviceSelectionRepository(),
        val settings: FakeAdbSettingsRepository = FakeAdbSettingsRepository(),
        infoBySerial: Map<String, DeviceInfo> = emptyMap(),
        cpuBySerial: Map<String, CpuInfo> = emptyMap(),
    ) {
        private val observeDevices = ObserveDevicesUseCase(devices)
        val viewModel = DevicesViewModel(
            observeDevices = observeDevices,
            observeSelectedDevice = ObserveSelectedDeviceUseCase(observeDevices, selection),
            observeDeviceInfo = ObserveDeviceInfoUseCase(FakeDeviceInfoRepository(infoBySerial)),
            observeCpuInfo = ObserveCpuInfoUseCase(FakeCpuRepository(cpuBySerial)),
            selectDevice = SelectDeviceUseCase(selection),
            connectWirelessDevice = ConnectWirelessDeviceUseCase(devices),
            pairWirelessDevice = PairWirelessDeviceUseCase(devices),
            disconnectDevice = DisconnectDeviceUseCase(devices),
            setAdbPathOverride = SetAdbPathOverrideUseCase(settings),
        )
    }

    test("lists devices online first") {
        val list = Fixture().viewModel.uiState.value.list

        list.shouldBeInstanceOf<DeviceListState.Loaded>()
        list.devices.map { it.serial } shouldContainExactly listOf("pixel", "old-phone")
        list.devices.first().name shouldBe "Pixel 8"
    }

    test("collects stats only for online devices") {
        val fixture = Fixture(
            infoBySerial = mapOf("pixel" to DeviceInfo("Google", "Pixel 8", "15", batteryLevel = 80), "old-phone" to DeviceInfo("x", "y", "9", 5)),
            cpuBySerial = mapOf("pixel" to CpuInfo(usagePercent = 12f, coreCount = 8, maxFrequencyMHz = 3_000)),
        )

        fixture.viewModel.uiState.value.stats shouldBe mapOf("pixel" to DeviceStats(androidVersion = "15", batteryLevel = 80, cpuUsagePercent = 12f))
    }

    test("a missing adb asks for a path, and submitting stores it") {
        val fixture = Fixture().apply { devices.devices.value = Outcome.Failure(DeviceError.AdbNotFound(listOf("/usr/bin/adb"))) }

        fixture.viewModel.uiState.value.list shouldBe DeviceListState.Failed(DevicesFailure.AdbNotFound(listOf("/usr/bin/adb")))

        fixture.viewModel.reduceIntent(DevicesIntent.AdbPathChanged("/opt/platform-tools/adb"))
        fixture.viewModel.reduceIntent(DevicesIntent.AdbPathSubmitted)

        fixture.settings.adbPath.value shouldBe "/opt/platform-tools/adb"
    }

    context("selection") {
        test("clicking an online device selects it, clicking again clears it") {
            val fixture = Fixture()

            fixture.viewModel.reduceIntent(DevicesIntent.DeviceClicked("pixel"))
            fixture.viewModel.uiState.value.selectedSerial shouldBe "pixel"

            fixture.viewModel.reduceIntent(DevicesIntent.DeviceClicked("pixel"))
            fixture.viewModel.uiState.value.selectedSerial.shouldBeNull()
        }

        test("offline devices cannot be selected") {
            val fixture = Fixture()

            fixture.viewModel.reduceIntent(DevicesIntent.DeviceClicked("old-phone"))

            fixture.selection.selectedSerial.value.shouldBeNull()
        }
    }

    context("connect over Wi-Fi") {
        test("an invalid address keeps the form open with an error") {
            val fixture = Fixture()

            fixture.viewModel.reduceIntent(DevicesIntent.ConnectFormOpened)
            fixture.viewModel.reduceIntent(DevicesIntent.AddressChanged("not an address"))
            fixture.viewModel.reduceIntent(DevicesIntent.ConnectSubmitted)

            fixture.viewModel.uiState.value.connectForm?.error shouldBe ConnectFormError.InvalidAddress
        }

        test("a successful connection closes the form and reports it") {
            val fixture = Fixture()

            fixture.viewModel.sideEffects.test {
                fixture.viewModel.reduceIntent(DevicesIntent.ConnectFormOpened)
                fixture.viewModel.reduceIntent(DevicesIntent.AddressChanged("192.168.1.42"))
                fixture.viewModel.reduceIntent(DevicesIntent.ConnectSubmitted)

                awaitItem() shouldBe DevicesSideEffect.Connected("192.168.1.42")
            }
            fixture.viewModel.uiState.value.connectForm.shouldBeNull()
            fixture.devices.connectedEndpoints shouldContainExactly listOf(WirelessEndpoint("192.168.1.42", 5555))
        }

        test("adb's failure reason is shown") {
            val fixture = Fixture().apply { devices.connectResult = Outcome.Failure(DeviceError.ConnectionFailed("10.0.0.2:5555", "Connection refused")) }

            fixture.viewModel.reduceIntent(DevicesIntent.ConnectFormOpened)
            fixture.viewModel.reduceIntent(DevicesIntent.AddressChanged("10.0.0.2"))
            fixture.viewModel.reduceIntent(DevicesIntent.ConnectSubmitted)

            val form = fixture.viewModel.uiState.value.connectForm
            form?.error shouldBe ConnectFormError.Failed("Connection refused")
            form?.inProgress?.shouldBeFalse()
        }

        test("pairing needs a code before it can be submitted") {
            val fixture = Fixture()

            fixture.viewModel.reduceIntent(DevicesIntent.ConnectFormOpened)
            fixture.viewModel.reduceIntent(DevicesIntent.ConnectModeSelected(ConnectMode.Pair))
            fixture.viewModel.reduceIntent(DevicesIntent.AddressChanged("192.168.1.42:37123"))
            fixture.viewModel.uiState.value.connectForm?.canSubmit?.shouldBeFalse()

            fixture.viewModel.reduceIntent(DevicesIntent.ConnectSubmitted)
            fixture.devices.pairings.size shouldBe 0
        }
    }

    test("disconnecting reports the result") {
        val fixture = Fixture()

        fixture.viewModel.sideEffects.test {
            fixture.viewModel.reduceIntent(DevicesIntent.DisconnectClicked("192.168.1.42:5555"))
            awaitItem() shouldBe DevicesSideEffect.Disconnected("192.168.1.42:5555")
        }
    }
})
