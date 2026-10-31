package com.tlpcraft.adbdesktop.feature.shell

import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.shell.RunShellSessionUseCase
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceRepository
import com.tlpcraft.adbdesktop.core.testing.FakeDeviceSelectionRepository
import com.tlpcraft.adbdesktop.core.testing.FakeShellRepository
import com.tlpcraft.adbdesktop.core.testing.onlineDevice
import com.tlpcraft.adbdesktop.feature.shell.contract.SessionState
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellIntent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
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

    class Fixture(selectedSerial: String? = "pixel") {
        val shell = FakeShellRepository()
        val selection = FakeDeviceSelectionRepository(selectedSerial)
        val viewModel = ShellViewModel(
            observeSelectedDevice = ObserveSelectedDeviceUseCase(ObserveDevicesUseCase(FakeDeviceRepository(listOf(onlineDevice("pixel")))), selection),
            runShellSession = RunShellSessionUseCase(shell),
        )

        fun run(command: String) {
            viewModel.reduceIntent(ShellIntent.InputChanged(command))
            viewModel.reduceIntent(ShellIntent.CommandSubmitted)
        }

        fun transcript() = viewModel.uiState.value.lines.map { it.text }
    }

    test("without a device there is no session") {
        val fixture = Fixture(selectedSerial = null)

        fixture.viewModel.uiState.value.session shouldBe SessionState.NoDevice
        fixture.shell.sessionsStarted shouldBe 0
    }

    test("a command is echoed, sent and followed by its output") {
        val fixture = Fixture()

        fixture.run("ls")

        fixture.transcript() shouldContainExactly listOf("ls", "output of ls")
        fixture.viewModel.uiState.value.lines.first().isCommand shouldBe true
        fixture.viewModel.uiState.value.input shouldBe ""
    }

    test("blank input is not sent") {
        val fixture = Fixture()

        fixture.run("   ")

        fixture.shell.received.shouldBeEmpty()
    }

    test("history walks back and forward, ending on an empty prompt") {
        val fixture = Fixture()
        fixture.run("first")
        fixture.run("second")

        fixture.viewModel.reduceIntent(ShellIntent.PreviousCommandRequested)
        fixture.viewModel.uiState.value.input shouldBe "second"
        fixture.viewModel.reduceIntent(ShellIntent.PreviousCommandRequested)
        fixture.viewModel.reduceIntent(ShellIntent.PreviousCommandRequested)
        fixture.viewModel.uiState.value.input shouldBe "first"

        fixture.viewModel.reduceIntent(ShellIntent.NextCommandRequested)
        fixture.viewModel.reduceIntent(ShellIntent.NextCommandRequested)
        fixture.viewModel.uiState.value.input shouldBe ""
    }

    test("exit ends the session and a new one can be started") {
        val fixture = Fixture()

        fixture.run("exit")
        fixture.viewModel.uiState.value.session shouldBe SessionState.Ended

        fixture.viewModel.reduceIntent(ShellIntent.RestartClicked)
        fixture.viewModel.uiState.value.session shouldBe SessionState.Running
        fixture.shell.sessionsStarted shouldBe 2
    }

    test("clearing keeps the session open") {
        val fixture = Fixture()
        fixture.run("ls")

        fixture.viewModel.reduceIntent(ShellIntent.ClearClicked)

        fixture.transcript().shouldBeEmpty()
        fixture.viewModel.uiState.value.session shouldBe SessionState.Running
    }
})
