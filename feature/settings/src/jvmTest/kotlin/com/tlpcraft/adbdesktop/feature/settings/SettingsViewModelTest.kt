package com.tlpcraft.adbdesktop.feature.settings

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AdbInfo
import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveAdbInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.ObserveThemeModeUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetThemeModeUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeAdbSettingsRepository
import com.tlpcraft.adbdesktop.core.testing.FakeAppearanceRepository
import com.tlpcraft.adbdesktop.feature.settings.contract.AdbStatus
import com.tlpcraft.adbdesktop.feature.settings.contract.SettingsIntent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest : FunSpec({
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    class Fixture(initialPath: String? = null, isVisible: Boolean = true) {
        val adb = FakeAdbSettingsRepository(initialPath)
        val appearance = FakeAppearanceRepository()
        val viewModel = SettingsViewModel(
            observeAdbPathOverride = ObserveAdbPathOverrideUseCase(adb),
            setAdbPathOverride = SetAdbPathOverrideUseCase(adb),
            observeAdbInfo = ObserveAdbInfoUseCase(adb),
            observeThemeMode = ObserveThemeModeUseCase(appearance),
            setThemeMode = SetThemeModeUseCase(appearance),
        ).apply { reduceIntent(SettingsIntent.VisibilityChanged(isVisible)) }

        val state get() = viewModel.uiState.value
    }

    test("shows the stored path and the adb in use") {
        val fixture = Fixture(initialPath = "/opt/adb")

        fixture.state.adbPathInput shouldBe "/opt/adb"
        fixture.state.adbStatus shouldBe AdbStatus.Found(AdbInfo("/usr/bin/adb", "37.0.0"))
    }

    test("saving stores the path") {
        val fixture = Fixture()

        fixture.viewModel.reduceIntent(SettingsIntent.AdbPathChanged("/nope"))
        fixture.viewModel.reduceIntent(SettingsIntent.AdbPathSaved)

        fixture.adb.adbPath.value shouldBe "/nope"
    }

    test("the adb status follows what the device layer reports") {
        val fixture = Fixture()

        fixture.adb.adbInfo.value = Outcome.Failure(DeviceError.InvalidAdbPath("/nope"))

        fixture.state.adbStatus shouldBe AdbStatus.Unavailable("Configured adb path is not an executable file: /nope")
    }

    test("a path changed elsewhere fills the field") {
        val fixture = Fixture()

        fixture.adb.adbPath.value = "/opt/adb"

        fixture.state.adbPathInput shouldBe "/opt/adb"
    }

    test("adb is not checked while the screen is hidden") {
        Fixture(isVisible = false).state.adbStatus shouldBe AdbStatus.Checking
    }

    test("resetting returns to automatic detection and drops unsaved text") {
        val fixture = Fixture(initialPath = "/opt/adb")

        fixture.viewModel.reduceIntent(SettingsIntent.AdbPathChanged("/typed"))
        fixture.viewModel.reduceIntent(SettingsIntent.AdbPathReset)

        fixture.adb.adbPath.value.shouldBeNull()
        fixture.state.adbPathInput shouldBe ""
    }

    test("choosing a theme stores it") {
        val fixture = Fixture()

        fixture.viewModel.reduceIntent(SettingsIntent.ThemeSelected(ThemeMode.DARK))

        fixture.appearance.themeMode.value shouldBe ThemeMode.DARK
        fixture.state.themeMode shouldBe ThemeMode.DARK
    }
})
