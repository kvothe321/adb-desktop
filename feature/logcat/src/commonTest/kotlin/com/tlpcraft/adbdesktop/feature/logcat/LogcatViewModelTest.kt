package com.tlpcraft.adbdesktop.feature.logcat

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import com.tlpcraft.adbdesktop.core.domain.model.LogLevel
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.logcat.ClearLogcatUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.logcat.ObserveAppProcessesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.logcat.ObserveLogcatUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.testing.FakeHostFiles
import com.tlpcraft.adbdesktop.core.testing.FakeLogcatRepository
import com.tlpcraft.adbdesktop.core.testing.onlineDevice
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogcatIntent
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogcatSideEffect
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

private fun entry(message: String, level: LogLevel = LogLevel.INFO, processId: Int = 1) = LogEntry("10-31 23:05:50.598", processId, 2, level, "Tag", message)

@OptIn(ExperimentalCoroutinesApi::class)
class LogcatViewModelTest : FunSpec({
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    class Fixture(selectedSerial: String? = "pixel") {
        val logcat = FakeLogcatRepository()
        val hostFiles = FakeHostFiles()
        val viewModel = LogcatViewModel(
            observeSelectedDevice = ObserveSelectedDeviceUseCase(
                ObserveDevicesUseCase(FakeDeviceRepository(listOf(onlineDevice("pixel")))),
                FakeDeviceSelectionRepository(selectedSerial),
            ),
            observeLogcat = ObserveLogcatUseCase(logcat),
            clearLogcat = ClearLogcatUseCase(logcat),
            observeAppProcesses = ObserveAppProcessesUseCase(logcat),
            hostFiles = hostFiles,
        )

        fun messages() = viewModel.uiState.value.visibleRows.map { it.entry.message }
    }

    test("does not stream until the screen opens, and stops when it closes") {
        val fixture = Fixture()
        fixture.logcat.streamsStarted shouldBe 0

        fixture.viewModel.reduceIntent(LogcatIntent.ScreenOpened)
        fixture.logcat.streamsStarted shouldBe 1

        fixture.viewModel.reduceIntent(LogcatIntent.ScreenClosed)
        fixture.logcat.logs.trySend(Outcome.Success(listOf(entry("late"))))
        fixture.messages().shouldBeEmpty()
    }

    test("without a device nothing streams") {
        val fixture = Fixture(selectedSerial = null)

        fixture.viewModel.reduceIntent(LogcatIntent.ScreenOpened)

        fixture.logcat.streamsStarted shouldBe 0
        fixture.viewModel.uiState.value.serial.shouldBeNull()
    }

    test("appends batches, and the filter narrows what is visible") {
        val fixture = Fixture()
        fixture.viewModel.reduceIntent(LogcatIntent.ScreenOpened)

        fixture.logcat.logs.trySend(Outcome.Success(listOf(entry("first"), entry("boom", LogLevel.ERROR))))
        fixture.messages() shouldContainExactly listOf("first", "boom")

        fixture.viewModel.reduceIntent(LogcatIntent.LevelSelected(LogLevel.ERROR))
        fixture.messages() shouldContainExactly listOf("boom")

        fixture.viewModel.reduceIntent(LogcatIntent.LevelSelected(LogLevel.VERBOSE))
        fixture.viewModel.reduceIntent(LogcatIntent.QueryChanged("FIRST"))
        fixture.messages() shouldContainExactly listOf("first")
    }

    test("pausing holds new entries back without losing them") {
        val fixture = Fixture()
        fixture.viewModel.reduceIntent(LogcatIntent.ScreenOpened)

        fixture.viewModel.reduceIntent(LogcatIntent.PauseToggled)
        fixture.logcat.logs.trySend(Outcome.Success(listOf(entry("held"))))
        fixture.messages().shouldBeEmpty()

        fixture.viewModel.reduceIntent(LogcatIntent.PauseToggled)
        fixture.messages() shouldContainExactly listOf("held")
    }

    test("keeps only the newest entries") {
        val fixture = Fixture()
        fixture.viewModel.reduceIntent(LogcatIntent.ScreenOpened)

        fixture.logcat.logs.trySend(Outcome.Success(List(5_100) { entry("line $it") }))

        fixture.viewModel.uiState.value.rows.size shouldBe 5_000
        fixture.messages().first() shouldBe "line 100"
    }

    test("clearing empties the device log and the view") {
        val fixture = Fixture()
        fixture.viewModel.reduceIntent(LogcatIntent.ScreenOpened)
        fixture.logcat.logs.trySend(Outcome.Success(listOf(entry("old"))))

        fixture.viewModel.reduceIntent(LogcatIntent.ClearClicked)

        fixture.logcat.clearedSerials shouldContainExactly listOf("pixel")
        fixture.messages().shouldBeEmpty()
    }

    context("package filter") {
        test("shows only that package's processes") {
            val fixture = Fixture()
            fixture.viewModel.reduceIntent(LogcatIntent.ScreenOpened)
            fixture.logcat.logs.trySend(Outcome.Success(listOf(entry("mine", processId = 42), entry("other", processId = 7))))

            fixture.viewModel.reduceIntent(LogcatIntent.PackageChanged("com.example.notes"))
            fixture.viewModel.uiState.value.showsPackageNotRunning shouldBe true
            fixture.messages().shouldBeEmpty()

            fixture.logcat.processIds.value = setOf(42)

            fixture.viewModel.uiState.value.showsPackageNotRunning shouldBe false
            fixture.messages() shouldContainExactly listOf("mine")
        }

        test("keeps the logs of a crashed process after the app restarts") {
            val fixture = Fixture()
            fixture.viewModel.reduceIntent(LogcatIntent.ScreenOpened)
            fixture.viewModel.reduceIntent(LogcatIntent.PackageChanged("com.example.notes"))
            fixture.logcat.processIds.value = setOf(42)
            fixture.logcat.logs.trySend(Outcome.Success(listOf(entry("crash", processId = 42))))

            fixture.logcat.processIds.value = emptySet()
            fixture.logcat.processIds.value = setOf(43)
            fixture.logcat.logs.trySend(Outcome.Success(listOf(entry("restarted", processId = 43))))

            fixture.messages() shouldContainExactly listOf("crash", "restarted")
        }

        test("clearing the field shows everything again") {
            val fixture = Fixture()
            fixture.viewModel.reduceIntent(LogcatIntent.ScreenOpened)
            fixture.logcat.logs.trySend(Outcome.Success(listOf(entry("any", processId = 7))))

            fixture.viewModel.reduceIntent(LogcatIntent.PackageChanged("com.example.notes"))
            fixture.viewModel.reduceIntent(LogcatIntent.PackageChanged(""))

            fixture.messages() shouldContainExactly listOf("any")
        }
    }

    test("export writes the visible entries in logcat format") {
        val fixture = Fixture().apply { hostFiles.saveLocation = "/home/me/logcat.txt" }
        fixture.viewModel.reduceIntent(LogcatIntent.ScreenOpened)
        fixture.logcat.logs.trySend(Outcome.Success(listOf(entry("hidden", LogLevel.DEBUG), entry("boom", LogLevel.ERROR))))
        fixture.viewModel.reduceIntent(LogcatIntent.LevelSelected(LogLevel.ERROR))

        fixture.viewModel.sideEffects.test {
            fixture.viewModel.reduceIntent(LogcatIntent.ExportClicked)

            awaitItem() shouldBe LogcatSideEffect.Exported(1, "/home/me/logcat.txt")
        }
        fixture.hostFiles.written["/home/me/logcat.txt"]?.decodeToString() shouldBe "10-31 23:05:50.598 1 2 E Tag: boom\n"
    }

    test("a stream failure is shown and retrying reconnects") {
        val fixture = Fixture()
        fixture.viewModel.reduceIntent(LogcatIntent.ScreenOpened)

        fixture.logcat.logs.trySend(Outcome.Failure(DeviceError.CommandFailed("device offline")))
        fixture.viewModel.uiState.value.failure shouldBe "adb command failed: device offline"

        fixture.viewModel.reduceIntent(LogcatIntent.RetryClicked)

        fixture.viewModel.uiState.value.failure.shouldBeNull()
        fixture.logcat.streamsStarted shouldBe 2
    }
})
