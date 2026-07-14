package com.tlpcraft.adbdesktop.feature.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlpcraft.adbdesktop.feature.shell.contract.SessionState
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellIntent
import com.tlpcraft.adbdesktop.feature.shell.contract.ShellUiState
import com.tlpcraft.adbdesktop.feature.shell.contract.TerminalLine
import com.tlpcraft.adbdesktop.feature.shell.resources.Res
import com.tlpcraft.adbdesktop.feature.shell.resources.shell_clear
import com.tlpcraft.adbdesktop.feature.shell.resources.shell_input_placeholder
import com.tlpcraft.adbdesktop.feature.shell.resources.shell_no_device_description
import com.tlpcraft.adbdesktop.feature.shell.resources.shell_no_device_title
import com.tlpcraft.adbdesktop.feature.shell.resources.shell_restart
import com.tlpcraft.adbdesktop.feature.shell.resources.shell_session_ended
import com.tlpcraft.adbdesktop.feature.shell.resources.shell_title
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ButtonVariant
import com.tlpcraft.adbdesktop.uikit.component.Divider
import com.tlpcraft.adbdesktop.uikit.component.EmptyState
import com.tlpcraft.adbdesktop.uikit.component.MonospaceText
import com.tlpcraft.adbdesktop.uikit.component.Text
import com.tlpcraft.adbdesktop.uikit.component.TextField
import com.tlpcraft.adbdesktop.uikit.component.Tone
import com.tlpcraft.adbdesktop.uikit.component.TopBar
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val PROMPT = "$ "

/** Interactive shell on the selected device. */
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
            Transcript(lines = uiState.lines, modifier = Modifier.weight(1f))
            Divider()
            SessionFooter(session = uiState.session, input = uiState.input, onIntent = onIntent)
        }
    }
}

@Composable
private fun Transcript(lines: List<TerminalLine>, modifier: Modifier = Modifier, listState: LazyListState = rememberLazyListState()) {
    LaunchedEffect(lines.lastOrNull()?.id, lines.lastOrNull()?.text) {
        if (lines.isNotEmpty()) listState.scrollToItem(lines.lastIndex)
    }
    SelectionContainer(modifier) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(AppTheme.spacing.md)) {
            items(lines, key = { it.id }) { line ->
                if (line.isCommand) {
                    MonospaceText(PROMPT + line.text, tone = Tone.Accent)
                } else {
                    MonospaceText(line.text)
                }
            }
        }
    }
}

@Composable
private fun SessionFooter(session: SessionState, input: String, onIntent: (ShellIntent) -> Unit) {
    Box(Modifier.fillMaxWidth().padding(AppTheme.spacing.md)) {
        if (session == SessionState.Running) {
            TextField(
                value = input,
                onValueChange = { onIntent(ShellIntent.InputChanged(it)) },
                placeholder = stringResource(Res.string.shell_input_placeholder),
                leadingIcon = IconName.Terminal,
                monospace = true,
                autoFocus = true,
                onSubmit = { onIntent(ShellIntent.CommandSubmitted) },
                onArrowUp = { onIntent(ShellIntent.PreviousCommandRequested) },
                onArrowDown = { onIntent(ShellIntent.NextCommandRequested) },
            )
        } else {
            SessionClosedBar(session = session, onRestart = { onIntent(ShellIntent.RestartClicked) })
        }
    }
}

@Composable
private fun SessionClosedBar(session: SessionState, onRestart: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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

private val previewLines = listOf(
    TerminalLine(id = 1, text = "getprop ro.product.model", isCommand = true),
    TerminalLine(id = 2, text = "Pixel 8", isCommand = false),
)

@UiKitPreview
@Composable
private fun ShellContentRunningPreview() = PreviewContext {
    ShellContent(uiState = ShellUiState(session = SessionState.Running, lines = previewLines, input = "ls /sdcard"), onIntent = {})
}

@UiKitPreview
@Composable
private fun ShellContentEndedPreview() = PreviewContext {
    ShellContent(uiState = ShellUiState(session = SessionState.Ended, lines = previewLines), onIntent = {})
}

@UiKitPreview
@Composable
private fun ShellContentNoDevicePreview() = PreviewContext {
    ShellContent(uiState = ShellUiState(), onIntent = {})
}
