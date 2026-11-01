package com.tlpcraft.adbdesktop.feature.screen

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.DeviceKey
import com.tlpcraft.adbdesktop.core.domain.model.KeyAction
import com.tlpcraft.adbdesktop.core.domain.model.MirrorFrame
import com.tlpcraft.adbdesktop.core.domain.model.MirrorInput
import com.tlpcraft.adbdesktop.core.domain.model.MirrorPoint
import com.tlpcraft.adbdesktop.core.domain.model.TouchAction
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.ObserveMirrorUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.testing.FakeHostClipboard
import com.tlpcraft.adbdesktop.core.testing.FakeScreenRepository
import com.tlpcraft.adbdesktop.core.testing.onlineDevice
import com.tlpcraft.adbdesktop.feature.screen.contract.DeviceButton
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorIntent
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorSideEffect
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class MirrorViewModelTest : FunSpec({
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    val frame = MirrorFrame(width = 1, height = 1, pixels = byteArrayOf(1, 2, 3, -1))
    val tap = MirrorInput.Touch(TouchAction.Down, MirrorPoint(0, 0, 1, 1))

    class Fixture(selectedSerial: String? = "pixel") {
        val screen = FakeScreenRepository()
        val clipboard = FakeHostClipboard()
        val viewModel = MirrorViewModel(
            observeSelectedDevice = ObserveSelectedDeviceUseCase(
                ObserveDevicesUseCase(FakeDeviceRepository(listOfNotNull(selectedSerial?.let { onlineDevice(it) }))),
                FakeDeviceSelectionRepository(selectedSerial),
            ),
            observeMirror = ObserveMirrorUseCase(screen),
            hostClipboard = clipboard,
        ).apply { reduceIntent(MirrorIntent.VisibilityChanged(true)) }

        val state get() = viewModel.uiState.value

        suspend fun goLive(liveFrame: MirrorFrame) {
            viewModel.reduceIntent(MirrorIntent.MirrorClicked)
            screen.mirror.emit(Outcome.Success(liveFrame))
        }
    }

    test("stays off until asked for") {
        val fixture = Fixture()

        fixture.state.mirror shouldBe MirrorState.Off
        fixture.screen.activeMirrors shouldBe 0
    }

    test("shows a spinner until the first frame, then every new frame") {
        val fixture = Fixture()

        fixture.viewModel.reduceIntent(MirrorIntent.MirrorClicked)
        fixture.state.mirror shouldBe MirrorState.Starting

        fixture.screen.mirror.emit(Outcome.Success(frame))
        fixture.state.mirror.shouldBeInstanceOf<MirrorState.Live>().frame shouldBeSameInstanceAs frame
    }

    test("stopping ends the stream") {
        val fixture = Fixture()
        fixture.viewModel.reduceIntent(MirrorIntent.MirrorClicked)

        fixture.viewModel.reduceIntent(MirrorIntent.MirrorClicked)

        fixture.state.mirror shouldBe MirrorState.Off
        fixture.screen.activeMirrors shouldBe 0
    }

    test("streams only while on show and resumes on return") {
        val fixture = Fixture()
        fixture.viewModel.reduceIntent(MirrorIntent.MirrorClicked)

        fixture.viewModel.reduceIntent(MirrorIntent.VisibilityChanged(false))
        fixture.screen.activeMirrors shouldBe 0

        fixture.viewModel.reduceIntent(MirrorIntent.VisibilityChanged(true))
        fixture.screen.activeMirrors shouldBe 1
        fixture.state.isOn shouldBe true
    }

    test("a failure turns mirroring off and reports it") {
        val fixture = Fixture()
        fixture.viewModel.reduceIntent(MirrorIntent.MirrorClicked)

        fixture.viewModel.sideEffects.test {
            fixture.screen.mirror.emit(Outcome.Failure(DeviceError.CommandFailed("no encoder")))

            awaitItem() shouldBe MirrorSideEffect.Failed("no encoder")
        }
        fixture.state.isOn shouldBe false
        fixture.state.mirror shouldBe MirrorState.Off
        fixture.screen.activeMirrors shouldBe 0
    }

    test("nothing streams without a device") {
        val fixture = Fixture(selectedSerial = null)

        fixture.viewModel.reduceIntent(MirrorIntent.MirrorClicked)

        fixture.screen.mirroredSerials.shouldBeEmpty()
    }

    test("input on a live mirror reaches the device") {
        val fixture = Fixture()
        fixture.goLive(frame)

        fixture.viewModel.reduceIntent(MirrorIntent.InputGiven(tap))

        fixture.screen.mirrorInput shouldContainExactly listOf(tap)
    }

    test("input before the first frame is dropped rather than replayed later") {
        val fixture = Fixture()
        fixture.viewModel.reduceIntent(MirrorIntent.MirrorClicked)

        fixture.viewModel.reduceIntent(MirrorIntent.InputGiven(tap))
        fixture.screen.mirror.emit(Outcome.Success(frame))

        fixture.screen.mirrorInput.shouldBeEmpty()
    }

    test("a device button is a full key press") {
        val fixture = Fixture()
        fixture.goLive(frame)

        fixture.viewModel.reduceIntent(MirrorIntent.DeviceButtonClicked(DeviceButton.Recents))

        fixture.screen.mirrorInput shouldContainExactly listOf(
            MirrorInput.Key(KeyAction.Down, DeviceKey.AppSwitch),
            MirrorInput.Key(KeyAction.Up, DeviceKey.AppSwitch),
        )
    }

    test("rotate asks the device to rotate") {
        val fixture = Fixture()
        fixture.goLive(frame)

        fixture.viewModel.reduceIntent(MirrorIntent.DeviceButtonClicked(DeviceButton.Rotate))

        fixture.screen.mirrorInput shouldContainExactly listOf(MirrorInput.Rotate)
    }

    test("pasting sends the computer's clipboard text, and nothing when it holds none") {
        val fixture = Fixture()
        fixture.goLive(frame)

        fixture.viewModel.reduceIntent(MirrorIntent.PasteRequested)
        fixture.clipboard.text = "hello"
        fixture.viewModel.reduceIntent(MirrorIntent.PasteRequested)

        fixture.screen.mirrorInput shouldContainExactly listOf(MirrorInput.Paste("hello"))
    }

    test("a session restarted after hiding keeps taking input") {
        val fixture = Fixture()
        fixture.goLive(frame)
        fixture.viewModel.reduceIntent(MirrorIntent.VisibilityChanged(false))
        fixture.viewModel.reduceIntent(MirrorIntent.VisibilityChanged(true))
        fixture.screen.mirror.emit(Outcome.Success(frame))

        fixture.viewModel.reduceIntent(MirrorIntent.InputGiven(tap))

        fixture.screen.mirrorInput shouldContainExactly listOf(tap)
    }
})
