package com.tlpcraft.adbdesktop.shell

import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.SelectDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.ObserveMirrorPinnedUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveThemeModeUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetThemeModeUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeAppearanceRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.testing.offlineDevice
import com.tlpcraft.adbdesktop.core.testing.onlineDevice
import com.tlpcraft.adbdesktop.shell.contract.AppShellIntent
import com.tlpcraft.adbdesktop.shell.contract.AppShellUiState
import com.tlpcraft.adbdesktop.shell.contract.DeviceOption
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AppShellViewModelTest : FunSpec({
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    class Fixture {
        val appearance = FakeAppearanceRepository()
        private val selection = FakeDeviceSelectionRepository()
        private val observeDevices = ObserveDevicesUseCase(FakeDeviceRepository(listOf(onlineDevice("pixel", model = "Pixel 8"), offlineDevice("old-phone"))))
        val viewModel = AppShellViewModel(
            observeDevices = observeDevices,
            observeSelectedDevice = ObserveSelectedDeviceUseCase(observeDevices, selection),
            selectDevice = SelectDeviceUseCase(selection),
            observeThemeMode = ObserveThemeModeUseCase(appearance),
            setThemeMode = SetThemeModeUseCase(appearance),
            observeMirrorPinned = ObserveMirrorPinnedUseCase(appearance),
        )
    }

    test("only online devices can be picked, and picking one selects it") {
        val fixture = Fixture()

        fixture.viewModel.uiState.value.devices shouldContainExactly listOf(DeviceOption("pixel", "Pixel 8"))

        fixture.viewModel.reduceIntent(AppShellIntent.DeviceSelected("pixel"))

        fixture.viewModel.uiState.value.selectedDevice shouldBe DeviceOption("pixel", "Pixel 8")
    }

    test("follows whether the mirror is pinned") {
        val fixture = Fixture()
        fixture.viewModel.uiState.value.isMirrorPinned.shouldBeFalse()

        fixture.appearance.mirrorPinned.value = true

        fixture.viewModel.uiState.value.isMirrorPinned.shouldBeTrue()
    }

    test("the toggle stores the opposite of the theme showing") {
        val fixture = Fixture()

        fixture.viewModel.reduceIntent(AppShellIntent.ThemeToggled(isDarkShowing = true))

        fixture.appearance.themeMode.value shouldBe ThemeMode.LIGHT
    }

    test("the system setting only applies in system mode") {
        AppShellUiState(themeMode = ThemeMode.SYSTEM).isDarkTheme(isSystemDark = true).shouldBeTrue()
        AppShellUiState(themeMode = ThemeMode.LIGHT).isDarkTheme(isSystemDark = true).shouldBeFalse()
        AppShellUiState(themeMode = ThemeMode.DARK).isDarkTheme(isSystemDark = false).shouldBeTrue()
    }
})
