package com.tlpcraft.adbdesktop.feature.shell

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import com.tlpcraft.adbdesktop.feature.shell.contract.SessionState
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellIntent
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellUiState
import com.tlpcraft.adbdesktop.uikit.component.TerminalRow
import com.tlpcraft.adbdesktop.uikit.component.TerminalSpan
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

@OptIn(ExperimentalTestApi::class)
class ShellContentTest : FunSpec({
    val rows = listOf(TerminalRow(1, listOf(TerminalSpan("pixel:/ $ ls"))))

    test("the terminal reports its size, shows its rows and takes keys right away") {
        runComposeUiTest {
            val intents = mutableListOf<ShellIntent>()
            setContent {
                AppTheme { ShellContent(uiState = ShellUiState(session = SessionState.Running, rows = rows), onIntent = { intents += it }) }
            }

            onNodeWithText("pixel:/ $ ls").assertExists()
            intents.first().shouldBeInstanceOf<ShellIntent.TerminalResized>()

            onNode(isFocused()).performKeyInput {
                pressKey(Key.Enter)
                keyDown(Key.CtrlLeft)
                pressKey(Key.C)
                keyUp(Key.CtrlLeft)
            }

            intents shouldContain ShellIntent.InputTyped("\r")
            intents shouldContain ShellIntent.InputTyped("\u0003")
        }
    }

    test("Ctrl+Shift+V pastes instead of sending a control code") {
        runComposeUiTest {
            val intents = mutableListOf<ShellIntent>()
            setContent {
                AppTheme { ShellContent(uiState = ShellUiState(session = SessionState.Running, rows = rows), onIntent = { intents += it }) }
            }

            onNode(isFocused()).performKeyInput {
                keyDown(Key.CtrlLeft)
                keyDown(Key.ShiftLeft)
                pressKey(Key.V)
                keyUp(Key.ShiftLeft)
                keyUp(Key.CtrlLeft)
            }

            intents.filterIsInstance<ShellIntent.InputTyped>().size shouldBe 0
            intents shouldContain ShellIntent.PasteRequested
        }
    }

    test("typed text, as an input method commits it, goes to the device as it is") {
        runComposeUiTest {
            val intents = mutableListOf<ShellIntent>()
            setContent {
                AppTheme { ShellContent(uiState = ShellUiState(session = SessionState.Running, rows = rows), onIntent = { intents += it }) }
            }

            onNode(isFocused()).performTextInput("日本語")

            intents shouldContain ShellIntent.InputTyped("日本語")
        }
    }

    test("an ended session offers a new one") {
        runComposeUiTest {
            val intents = mutableListOf<ShellIntent>()
            setContent {
                AppTheme { ShellContent(uiState = ShellUiState(session = SessionState.Ended), onIntent = { intents += it }) }
            }

            onNodeWithText("New session").performClick()

            intents shouldContain ShellIntent.RestartClicked
        }
    }

    test("a starting session shows the terminal so it can report its size") {
        runComposeUiTest {
            val intents = mutableListOf<ShellIntent>()
            setContent { AppTheme { ShellContent(uiState = ShellUiState(session = SessionState.Starting), onIntent = { intents += it }) } }

            intents.first().shouldBeInstanceOf<ShellIntent.TerminalResized>()
            onNodeWithText("New session").assertDoesNotExist()
        }
    }

    test("after clicking into the terminal, typing still reaches the device") {
        runComposeUiTest {
            val intents = mutableListOf<ShellIntent>()
            setContent {
                AppTheme { ShellContent(uiState = ShellUiState(session = SessionState.Running, rows = rows), onIntent = { intents += it }) }
            }

            onNodeWithText("pixel:/ $ ls").performMouseInput { click(center) }
            waitForIdle()
            onNode(isFocused()).performTextInput("pwd")

            intents shouldContain ShellIntent.InputTyped("pwd")
        }
    }
})
