package com.tlpcraft.adbdesktop.feature.shell

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.model.TerminalSize
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.shell.RunShellSessionUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.testing.FakeHostClipboard
import com.tlpcraft.adbdesktop.core.testing.FakeShellRepository
import com.tlpcraft.adbdesktop.core.testing.onlineDevice
import com.tlpcraft.adbdesktop.feature.shell.contract.SessionState
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellIntent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
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

    class Fixture(selectedSerial: String? = "pixel", isMeasured: Boolean = true) {
        val shell = FakeShellRepository()
        val devices = FakeDeviceRepository(listOfNotNull(selectedSerial?.let { onlineDevice(it) }))
        val selection = FakeDeviceSelectionRepository(selectedSerial)
        val clipboard = FakeHostClipboard()
        val viewModel = ShellViewModel(
            observeSelectedDevice = ObserveSelectedDeviceUseCase(ObserveDevicesUseCase(devices), selection),
            runShellSession = RunShellSessionUseCase(shell),
            hostClipboard = clipboard,
        ).apply { if (isMeasured) reduceIntent(ShellIntent.TerminalResized(rows = 24, columns = 80)) }

        val state get() = viewModel.uiState.value

        fun type(text: String) = viewModel.reduceIntent(ShellIntent.InputTyped(text))

        fun screen() = state.rows.map { it.text.trim() }.filter { it.isNotEmpty() }
    }

    test("without a device there is no session") {
        val fixture = Fixture(selectedSerial = null)

        fixture.state.session shouldBe SessionState.NoDevice
        fixture.shell.sessionsStarted shouldBe 0
    }

    test("a session waits until the terminal size is known") {
        val fixture = Fixture(isMeasured = false)
        fixture.shell.sessionsStarted shouldBe 0

        fixture.viewModel.reduceIntent(ShellIntent.TerminalResized(rows = 30, columns = 100))

        fixture.shell.startedWith shouldBe TerminalSize(30, 100)
    }

    test("resizing keeps the session running") {
        val fixture = Fixture()

        fixture.viewModel.reduceIntent(ShellIntent.TerminalResized(rows = 30, columns = 100))

        fixture.shell.sessionsStarted shouldBe 1
    }

    test("keys go to the device as typed and output appears on the screen") {
        val fixture = Fixture()

        fixture.type("\u0003")

        fixture.shell.received shouldContainExactly listOf("\u0003")
        fixture.screen() shouldContain "output of"
    }

    test("input is ignored without a running session") {
        val fixture = Fixture(selectedSerial = null)

        fixture.type("ls")

        fixture.shell.received.shouldBeEmpty()
    }

    test("the output of exit ends the session and a new one can start") {
        val fixture = Fixture()

        fixture.type("exit")
        fixture.state.session shouldBe SessionState.Ended

        fixture.viewModel.reduceIntent(ShellIntent.RestartClicked)

        fixture.state.session shouldBe SessionState.Running
        fixture.shell.sessionsStarted shouldBe 2
    }

    test("losing the device starts over") {
        val fixture = Fixture()
        fixture.type("ls")

        fixture.devices.devices.value = Outcome.Success(emptyList())

        fixture.state.session shouldBe SessionState.NoDevice
        fixture.screen().shouldBeEmpty()
    }

    test("clearing empties the screen and asks the shell for a new prompt") {
        val fixture = Fixture()
        fixture.type("ls")

        fixture.viewModel.reduceIntent(ShellIntent.ClearClicked)

        fixture.shell.received shouldContain "\u000c"
        fixture.screen().size shouldBe 1
    }

    test("pasted lines are sent the way Enter sends them") {
        val fixture = Fixture().apply { clipboard.text = "one\r\ntwo\n" }

        fixture.viewModel.reduceIntent(ShellIntent.PasteRequested)

        fixture.shell.received shouldContainExactly listOf("one\rtwo\r")
    }

    test("an empty clipboard pastes nothing") {
        val fixture = Fixture()

        fixture.viewModel.reduceIntent(ShellIntent.PasteRequested)

        fixture.shell.received.shouldBeEmpty()
    }
})
