package com.tlpcraft.adbdesktop.shell

import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.SelectDeviceUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.testing.offlineDevice
import com.tlpcraft.adbdesktop.core.testing.onlineDevice
import com.tlpcraft.adbdesktop.shell.contract.DeviceOption
import com.tlpcraft.adbdesktop.shell.contract.ShellIntent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ShellViewModelTest : FunSpec({
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    test("only online devices can be picked, and picking one selects it") {
        val selection = FakeDeviceSelectionRepository()
        val observeDevices = ObserveDevicesUseCase(FakeDeviceRepository(listOf(onlineDevice("pixel", model = "Pixel 8"), offlineDevice("old-phone"))))
        val viewModel = ShellViewModel(observeDevices, ObserveSelectedDeviceUseCase(observeDevices, selection), SelectDeviceUseCase(selection))

        viewModel.uiState.value.devices shouldContainExactly listOf(DeviceOption("pixel", "Pixel 8"))

        viewModel.reduceIntent(ShellIntent.DeviceSelected("pixel"))

        viewModel.uiState.value.selectedDevice shouldBe DeviceOption("pixel", "Pixel 8")
    }
})
