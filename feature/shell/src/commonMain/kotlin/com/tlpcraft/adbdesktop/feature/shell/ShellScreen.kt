package com.tlpcraft.adbdesktop.feature.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlpcraft.adbdesktop.feature.shell.contract.SessionState
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellIntent
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellUiState
import com.tlpcraft.adbdesktop.feature.shell.resources.Res
import com.tlpcraft.adbdesktop.feature.shell.resources.shell_clear
import com.tlpcraft.adbdesktop.feature.shell.resources.shell_no_device_description
import com.tlpcraft.adbdesktop.feature.shell.resources.shell_no_device_title
import com.tlpcraft.adbdesktop.feature.shell.resources.shell_restart
import com.tlpcraft.adbdesktop.feature.shell.resources.shell_session_ended
import com.tlpcraft.adbdesktop.feature.shell.resources.shell_title
import com.tlpcraft.adbdesktop.feature.shell.terminal.KeyPress
import com.tlpcraft.adbdesktop.feature.shell.terminal.encodeKey
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ButtonVariant
import com.tlpcraft.adbdesktop.uikit.component.Divider
import com.tlpcraft.adbdesktop.uikit.component.EmptyState
import com.tlpcraft.adbdesktop.uikit.component.TerminalColor
import com.tlpcraft.adbdesktop.uikit.component.TerminalGrid
import com.tlpcraft.adbdesktop.uikit.component.TerminalRow
import com.tlpcraft.adbdesktop.uikit.component.TerminalSpan
import com.tlpcraft.adbdesktop.uikit.component.Text
import com.tlpcraft.adbdesktop.uikit.component.Tone
import com.tlpcraft.adbdesktop.uikit.component.TopBar
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Interactive terminal on the selected device. */
@Composable
fun ShellScreen(modifier: Modifier = Modifier, viewModel: ShellViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ShellContent(uiState = uiState, onIntent = viewModel::reduceIntent, modifier = modifier)
}

@Composable
fun ShellContent(uiState: ShellUiState, onIntent: (ShellIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        TopBar(title = stringResource(Res.string.shell_title)) {
            if (uiState.session != SessionState.NoDevice) {
                Button(
                    text = stringResource(Res.string.shell_clear),
                    onClick = { onIntent(ShellIntent.ClearClicked) },
                    variant = ButtonVariant.Ghost,
                    icon = IconName.Delete,
                )
            }
        }
        if (uiState.session == SessionState.NoDevice) {
            Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(Res.string.shell_no_device_title),
                    description = stringResource(Res.string.shell_no_device_description),
                    icon = IconName.Terminal,
                )
            }
        } else {
            TerminalGrid(
                rows = uiState.rows,
                onGridSizeChange = { rows, columns -> onIntent(ShellIntent.TerminalResized(rows, columns)) },
                onKeyPress = { event -> handleKey(event, uiState.hasApplicationCursorKeys, onIntent) },
                onTextInput = { text -> onIntent(ShellIntent.InputTyped(text)) },
                modifier = Modifier.weight(1f),
            )
            if (uiState.session != SessionState.Running && uiState.session != SessionState.Starting) {
                Divider()
                SessionClosedBar(
                    session = uiState.session,
                    onRestart = { onIntent(ShellIntent.RestartClicked) },
                    modifier = Modifier.padding(AppTheme.spacing.md),
                )
            }
        }
    }
}

/** Ctrl+C belongs to the running program as in any terminal, so paste takes Ctrl+Shift+V. */
private fun handleKey(event: KeyEvent, hasApplicationCursorKeys: Boolean, onIntent: (ShellIntent) -> Unit): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    if (event.isCtrlPressed && event.isShiftPressed) {
        if (event.key == Key.V) onIntent(ShellIntent.PasteRequested)
        return event.key == Key.V
    }
    val press = KeyPress(event.key, event.utf16CodePoint, event.isCtrlPressed, event.isAltPressed, event.isShiftPressed)
    val input = encodeKey(press, hasApplicationCursorKeys) ?: return false
    onIntent(ShellIntent.InputTyped(input))
    return true
}

@Composable
private fun SessionClosedBar(session: SessionState, onRestart: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        val failure = session as? SessionState.Failed
        Text(
            text = failure?.details ?: stringResource(Res.string.shell_session_ended),
            modifier = Modifier.weight(1f),
            tone = if (failure != null) Tone.Error else Tone.Neutral,
            maxLines = 2,
        )
        Button(text = stringResource(Res.string.shell_restart), onClick = onRestart, variant = ButtonVariant.Primary, icon = IconName.Refresh)
    }
}

private val previewRows = listOf(
    TerminalRow(1, listOf(TerminalSpan("pixel:/ $ ", foreground = TerminalColor.Green), TerminalSpan("getprop ro.product.model"))),
    TerminalRow(2, listOf(TerminalSpan("Pixel 8"))),
)

@UiKitPreview
@Composable
private fun ShellContentRunningPreview() = PreviewContext {
    ShellContent(uiState = ShellUiState(session = SessionState.Running, rows = previewRows), onIntent = {})
}

@UiKitPreview
@Composable
private fun ShellContentEndedPreview() = PreviewContext {
    ShellContent(uiState = ShellUiState(session = SessionState.Ended, rows = previewRows), onIntent = {})
}

@UiKitPreview
@Composable
private fun ShellContentNoDevicePreview() = PreviewContext {
    ShellContent(uiState = ShellUiState(), onIntent = {})
}
