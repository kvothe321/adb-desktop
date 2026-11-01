package com.tlpcraft.adbdesktop.feature.apps

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.ExecuteAppActionUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.InstallApkUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.ObserveAppDetailsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.ObserveAppsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeAppsRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.testing.FakeHostFiles
import com.tlpcraft.adbdesktop.core.testing.onlineDevice
import com.tlpcraft.adbdesktop.feature.apps.contract.AppDetailsState
import com.tlpcraft.adbdesktop.feature.apps.contract.AppListState
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsIntent
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsSideEffect
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
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
class AppsViewModelTest : FunSpec({
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    class Fixture(selectedSerial: String? = "pixel", isVisible: Boolean = true) {
        val apps = FakeAppsRepository().apply {
            apps.value = Outcome.Success(listOf(AppInfo("com.example.notes"), AppInfo("com.android.settings")))
        }
        val selection = FakeDeviceSelectionRepository(selectedSerial)
        val hostFiles = FakeHostFiles()
        val viewModel = AppsViewModel(
            observeSelectedDevice = ObserveSelectedDeviceUseCase(ObserveDevicesUseCase(FakeDeviceRepository(listOf(onlineDevice("pixel")))), selection),
            observeApps = ObserveAppsUseCase(apps),
            observeAppDetails = ObserveAppDetailsUseCase(apps),
            executeAppAction = ExecuteAppActionUseCase(apps),
            installApk = InstallApkUseCase(apps),
            hostFiles = hostFiles,
        ).apply { reduceIntent(AppsIntent.VisibilityChanged(isVisible)) }
    }

    test("without a selected device there is nothing to list") {
        Fixture(selectedSerial = null).viewModel.uiState.value.list shouldBe AppListState.NoDevice
    }

    test("loads user apps of the selected device, sorted") {
        val fixture = Fixture()

        fixture.viewModel.uiState.value.list shouldBe AppListState.Loaded(listOf("com.android.settings", "com.example.notes"))
        fixture.apps.requestedFilters shouldContainExactly listOf(AppFilter.USER)
    }

    test("an app installed elsewhere shows up on its own") {
        val fixture = Fixture()

        fixture.apps.apps.value = Outcome.Success(listOf(AppInfo("com.example.new")))

        fixture.viewModel.uiState.value.list shouldBe AppListState.Loaded(listOf("com.example.new"))
    }

    test("a failing list recovers on its own") {
        val fixture = Fixture()

        fixture.apps.apps.value = Outcome.Failure(DeviceError.CommandFailed("offline"))
        fixture.viewModel.uiState.value.list.shouldBeInstanceOf<AppListState.Failed>()

        fixture.apps.apps.value = Outcome.Success(listOf(AppInfo("com.example.notes")))
        fixture.viewModel.uiState.value.list shouldBe AppListState.Loaded(listOf("com.example.notes"))
    }

    test("nothing is read from the device while the screen is hidden") {
        val fixture = Fixture(isVisible = false)

        fixture.apps.requestedFilters.shouldBeEmpty()
        fixture.viewModel.uiState.value.list shouldBe AppListState.Loading

        fixture.viewModel.reduceIntent(AppsIntent.VisibilityChanged(true))

        fixture.apps.requestedFilters shouldContainExactly listOf(AppFilter.USER)
    }

    test("the list stays on screen while hidden and shown again") {
        val fixture = Fixture()
        val shown = fixture.viewModel.uiState.value.list

        fixture.viewModel.reduceIntent(AppsIntent.VisibilityChanged(false))

        fixture.viewModel.uiState.value.list shouldBe shown
    }

    test("changing the filter reloads the list") {
        val fixture = Fixture()

        fixture.viewModel.reduceIntent(AppsIntent.FilterSelected(AppFilter.SYSTEM))

        fixture.apps.requestedFilters shouldContainExactly listOf(AppFilter.USER, AppFilter.SYSTEM)
    }

    test("the query filters visible packages without reloading") {
        val fixture = Fixture()

        fixture.viewModel.reduceIntent(AppsIntent.QueryChanged("NOTES"))

        fixture.viewModel.uiState.value.visiblePackages shouldContainExactly listOf("com.example.notes")
        fixture.apps.requestedFilters.size shouldBe 1
    }

    test("selecting an app loads its details") {
        val fixture = Fixture()

        fixture.viewModel.reduceIntent(AppsIntent.AppClicked("com.example.notes"))

        val details = fixture.viewModel.uiState.value.details
        details.shouldBeInstanceOf<AppDetailsState.Loaded>()
        details.details.packageName shouldBe "com.example.notes"
    }

    context("install") {
        test("installs the picked APKs") {
            val fixture = Fixture().apply { hostFiles.pickedFiles = listOf("/home/me/base.apk", "/home/me/split.apk") }

            fixture.viewModel.sideEffects.test {
                fixture.viewModel.reduceIntent(AppsIntent.InstallClicked)

                awaitItem() shouldBe AppsSideEffect.Installed(fileCount = 2)
            }
            fixture.apps.installedApks shouldContainExactly listOf(listOf("/home/me/base.apk", "/home/me/split.apk"))
        }

        test("dropped APKs install together, and other files are left out") {
            val fixture = Fixture()

            fixture.viewModel.reduceIntent(AppsIntent.FilesDropped(listOf("/home/me/base.APK", "/home/me/notes.txt", "/home/me/split.apk")))

            fixture.apps.installedApks shouldContainExactly listOf(listOf("/home/me/base.APK", "/home/me/split.apk"))
        }

        test("a drop without APKs says so") {
            val fixture = Fixture()

            fixture.viewModel.sideEffects.test {
                fixture.viewModel.reduceIntent(AppsIntent.FilesDropped(listOf("/home/me/notes.txt")))

                awaitItem() shouldBe AppsSideEffect.NoApkDropped
            }
            fixture.apps.installedApks.shouldBeEmpty()
        }

        test("cancelling the file dialog installs nothing") {
            val fixture = Fixture()

            fixture.viewModel.reduceIntent(AppsIntent.InstallClicked)

            fixture.apps.installedApks.shouldBeEmpty()
        }

        test("a refused install reports the reason") {
            val fixture = Fixture().apply {
                hostFiles.pickedFiles = listOf("/home/me/old.apk")
                apps.installResult = Outcome.Failure(DeviceError.CommandFailed("INSTALL_FAILED_OLDER_SDK"))
            }

            fixture.viewModel.sideEffects.test {
                fixture.viewModel.reduceIntent(AppsIntent.InstallClicked)

                awaitItem() shouldBe AppsSideEffect.InstallFailed("INSTALL_FAILED_OLDER_SDK")
            }
        }
    }

    context("actions") {
        test("destructive actions wait for confirmation") {
            val fixture = Fixture()
            fixture.viewModel.reduceIntent(AppsIntent.AppClicked("com.example.notes"))

            fixture.viewModel.reduceIntent(AppsIntent.ActionClicked(AppAction.CLEAR_DATA))

            fixture.viewModel.uiState.value.pendingConfirmation shouldBe AppAction.CLEAR_DATA
            fixture.apps.executedActions.shouldBeEmpty()

            fixture.viewModel.reduceIntent(AppsIntent.ActionCancelled)
            fixture.viewModel.uiState.value.pendingConfirmation.shouldBeNull()
        }

        test("a confirmed uninstall clears the selection and reports success") {
            val fixture = Fixture()
            fixture.viewModel.reduceIntent(AppsIntent.AppClicked("com.example.notes"))

            fixture.viewModel.sideEffects.test {
                fixture.viewModel.reduceIntent(AppsIntent.ActionClicked(AppAction.UNINSTALL))
                fixture.viewModel.reduceIntent(AppsIntent.ActionConfirmed)

                awaitItem() shouldBe AppsSideEffect.ActionSucceeded(AppAction.UNINSTALL, "com.example.notes")
            }
            fixture.apps.executedActions shouldContainExactly listOf("com.example.notes" to AppAction.UNINSTALL)
            fixture.viewModel.uiState.value.selectedPackage.shouldBeNull()
            fixture.viewModel.uiState.value.details shouldBe AppDetailsState.Hidden
        }

        test("a failed action is reported") {
            val fixture = Fixture().apply { apps.actionResult = Outcome.Failure(DeviceError.CommandFailed("Failure [NOT_INSTALLED]")) }
            fixture.viewModel.reduceIntent(AppsIntent.AppClicked("com.example.notes"))

            fixture.viewModel.sideEffects.test {
                fixture.viewModel.reduceIntent(AppsIntent.ActionClicked(AppAction.FORCE_STOP))
                awaitItem() shouldBe AppsSideEffect.ActionFailed(AppAction.FORCE_STOP, "com.example.notes", "Failure [NOT_INSTALLED]")
            }
        }
    }
})
