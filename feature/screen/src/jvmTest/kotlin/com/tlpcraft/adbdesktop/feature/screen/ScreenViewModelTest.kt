package com.tlpcraft.adbdesktop.feature.screen

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.CaptureScreenshotUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.RecordScreenUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.SaveRecordingUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.StopRecordingUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.testing.FakeHostClipboard
import com.tlpcraft.adbdesktop.core.testing.FakeHostFiles
import com.tlpcraft.adbdesktop.core.testing.FakeScreenRepository
import com.tlpcraft.adbdesktop.core.testing.onlineDevice
import com.tlpcraft.adbdesktop.feature.screen.contract.RecordingState
import com.tlpcraft.adbdesktop.feature.screen.contract.ScreenIntent
import com.tlpcraft.adbdesktop.feature.screen.contract.ScreenSideEffect
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenViewModelTest : FunSpec({
    val dispatcher = UnconfinedTestDispatcher()

    beforeTest { Dispatchers.setMain(dispatcher) }
    afterTest { Dispatchers.resetMain() }

    class Fixture(selectedSerial: String? = "pixel") {
        val screen = FakeScreenRepository()
        val hostFiles = FakeHostFiles()
        val hostClipboard = FakeHostClipboard()
        val viewModel = ScreenViewModel(
            observeSelectedDevice = ObserveSelectedDeviceUseCase(
                ObserveDevicesUseCase(FakeDeviceRepository(listOfNotNull(selectedSerial?.let { onlineDevice(it) }))),
                FakeDeviceSelectionRepository(selectedSerial),
            ),
            captureScreenshot = CaptureScreenshotUseCase(screen),
            recordScreen = RecordScreenUseCase(screen),
            stopRecording = StopRecordingUseCase(screen),
            saveRecording = SaveRecordingUseCase(screen),
            hostFiles = hostFiles,
            hostClipboard = hostClipboard,
        )

        val state get() = viewModel.uiState.value
    }

    test("nothing can be captured without a device") {
        val fixture = Fixture(selectedSerial = null)

        fixture.viewModel.reduceIntent(ScreenIntent.CaptureClicked)

        fixture.state.canCapture.shouldBeFalse()
        fixture.state.screenshot.shouldBeNull()
    }

    test("a capture shows the image") {
        val fixture = Fixture()

        fixture.viewModel.reduceIntent(ScreenIntent.CaptureClicked)

        fixture.state.screenshot.shouldNotBeNull().png.toList() shouldBe listOf<Byte>(1, 2)
        fixture.state.isCapturing.shouldBeFalse()
    }

    test("a failed capture is reported with the device's message") {
        val fixture = Fixture().apply { screen.screenshotResult = Outcome.Failure(DeviceError.CommandFailed("screencap failed")) }

        fixture.viewModel.sideEffects.test {
            fixture.viewModel.reduceIntent(ScreenIntent.CaptureClicked)

            awaitItem() shouldBe ScreenSideEffect.Failed("screencap failed")
        }
    }

    test("saving writes the screenshot to the picked location") {
        val fixture = Fixture().apply { hostFiles.saveLocation = "/home/me/shot.png" }
        fixture.viewModel.reduceIntent(ScreenIntent.CaptureClicked)

        fixture.viewModel.sideEffects.test {
            fixture.viewModel.reduceIntent(ScreenIntent.SaveScreenshotClicked)

            awaitItem() shouldBe ScreenSideEffect.ScreenshotSaved("/home/me/shot.png")
        }
        fixture.hostFiles.written["/home/me/shot.png"].shouldNotBeNull().size shouldBe 2
    }

    test("copying puts the image on the clipboard") {
        val fixture = Fixture()
        fixture.viewModel.reduceIntent(ScreenIntent.CaptureClicked)

        fixture.viewModel.sideEffects.test {
            fixture.viewModel.reduceIntent(ScreenIntent.CopyScreenshotClicked)

            awaitItem() shouldBe ScreenSideEffect.ScreenshotCopied
        }
        fixture.hostClipboard.copied.shouldNotBeNull().size shouldBe 2
    }

    context("recording") {
        test("the clock counts while recording and stops with it") {
            val fixture = Fixture()

            fixture.viewModel.reduceIntent(ScreenIntent.RecordClicked)
            dispatcher.scheduler.advanceTimeBy(3.seconds + 1.milliseconds)
            fixture.state.recordingSeconds shouldBe 3

            fixture.viewModel.reduceIntent(ScreenIntent.StopClicked)
            dispatcher.scheduler.advanceTimeBy(5.seconds)
            fixture.state.recordingSeconds shouldBe 3
        }

        test("stopping moves the recording to the picked location") {
            val fixture = Fixture().apply { hostFiles.saveLocation = "/home/me/clip.mp4" }

            fixture.viewModel.sideEffects.test {
                fixture.viewModel.reduceIntent(ScreenIntent.RecordClicked)
                fixture.state.recording shouldBe RecordingState.Recording
                fixture.state.canRecord.shouldBeFalse()

                fixture.viewModel.reduceIntent(ScreenIntent.StopClicked)

                awaitItem() shouldBe ScreenSideEffect.RecordingSaved("/home/me/clip.mp4")
            }
            fixture.screen.savedRecordings shouldContainExactly listOf("/home/me/clip.mp4")
            fixture.state.recording shouldBe RecordingState.Idle
        }

        test("cancelling the save dialog says so") {
            val fixture = Fixture()

            fixture.viewModel.sideEffects.test {
                fixture.viewModel.reduceIntent(ScreenIntent.RecordClicked)
                fixture.viewModel.reduceIntent(ScreenIntent.StopClicked)

                awaitItem() shouldBe ScreenSideEffect.RecordingDiscarded
            }
            fixture.state.recording shouldBe RecordingState.Idle
        }
    }
})
