package com.tlpcraft.adbdesktop.feature.shell

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.runComposeUiTest
import com.tlpcraft.adbdesktop.feature.shell.contract.SessionState
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellIntent
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellUiState
import com.tlpcraft.adbdesktop.feature.shell.contract.TerminalLine
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain

@OptIn(ExperimentalTestApi::class)
class ShellContentTest : FunSpec({
    test("Enter submits and the arrow keys walk the history") {
        runComposeUiTest {
            val intents = mutableListOf<ShellIntent>()
            val lines = listOf(TerminalLine(1, "ls", isCommand = true), TerminalLine(2, "Download", isCommand = false))
            setContent {
                AppTheme { ShellContent(uiState = ShellUiState(session = SessionState.Running, lines = lines, input = "pwd"), onIntent = { intents += it }) }
            }

            onNodeWithText("$ ls").assertExists()
            onNode(hasSetTextAction()).performKeyInput {
                pressKey(Key.Enter)
                pressKey(Key.DirectionUp)
                pressKey(Key.DirectionDown)
            }

            intents shouldContain ShellIntent.CommandSubmitted
            intents shouldContain ShellIntent.PreviousCommandRequested
            intents shouldContain ShellIntent.NextCommandRequested
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
})
