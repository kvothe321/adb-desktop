package com.tlpcraft.adbdesktop.feature.logcat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import com.tlpcraft.adbdesktop.core.domain.model.LogFilter
import com.tlpcraft.adbdesktop.core.domain.model.LogLevel
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogRow
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogcatIntent
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogcatSideEffect
import com.tlpcraft.adbdesktop.feature.logcat.contract.LogcatUiState
import com.tlpcraft.adbdesktop.feature.logcat.resources.Res
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_clear
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_clear_failed
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_export
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_exported
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_level_debug
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_level_error
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_level_fatal
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_level_info
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_level_verbose
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_level_warning
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_no_device_description
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_no_device_title
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_package_not_running
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_package_placeholder
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_pause
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_reconnecting
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_resume
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_retry
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_search_placeholder
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_title
import com.tlpcraft.adbdesktop.feature.logcat.resources.logcat_waiting
import com.tlpcraft.adbdesktop.uikit.component.Badge
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ButtonVariant
import com.tlpcraft.adbdesktop.uikit.component.Chip
import com.tlpcraft.adbdesktop.uikit.component.Divider
import com.tlpcraft.adbdesktop.uikit.component.EmptyState
import com.tlpcraft.adbdesktop.uikit.component.MonospaceText
import com.tlpcraft.adbdesktop.uikit.component.Page
import com.tlpcraft.adbdesktop.uikit.component.Panel
import com.tlpcraft.adbdesktop.uikit.component.SearchField
import com.tlpcraft.adbdesktop.uikit.component.Text
import com.tlpcraft.adbdesktop.uikit.component.TextEmphasis
import com.tlpcraft.adbdesktop.uikit.component.TextField
import com.tlpcraft.adbdesktop.uikit.component.Tone
import com.tlpcraft.adbdesktop.uikit.component.TopBar
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Live device log. The stream runs only while this screen is in the composition. */
@Composable
fun LogcatScreen(onShowMessage: (String) -> Unit, modifier: Modifier = Modifier, viewModel: LogcatViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)

    DisposableEffect(viewModel) {
        viewModel.reduceIntent(LogcatIntent.ScreenOpened)
        onDispose { viewModel.reduceIntent(LogcatIntent.ScreenClosed) }
    }
    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect ->
            currentOnShowMessage(effect.message())
        }
    }

    LogcatContent(uiState = uiState, onIntent = viewModel::reduceIntent, modifier = modifier)
}

@Composable
fun LogcatContent(uiState: LogcatUiState, onIntent: (LogcatIntent) -> Unit, modifier: Modifier = Modifier) {
    Page(
        title = stringResource(Res.string.logcat_title),
        modifier = modifier,
        actions = { if (uiState.serial != null) LogcatActions(isPaused = uiState.isPaused, onIntent = onIntent) },
    ) {
        if (uiState.serial == null) {
            Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = stringResource(Res.string.logcat_no_device_title),
                    description = stringResource(Res.string.logcat_no_device_description),
                    icon = IconName.List,
                )
            }
        } else {
            FilterBar(
                filter = uiState.filter,
                packageName = uiState.packageName,
                showsPackageNotRunning = uiState.showsPackageNotRunning,
                onIntent = onIntent,
            )
            Panel(Modifier.weight(1f).fillMaxWidth()) {
                LogBody(uiState = uiState, onRetry = { onIntent(LogcatIntent.RetryClicked) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RowScope.LogcatActions(isPaused: Boolean, onIntent: (LogcatIntent) -> Unit) {
    Button(
        text = stringResource(if (isPaused) Res.string.logcat_resume else Res.string.logcat_pause),
        onClick = { onIntent(LogcatIntent.PauseToggled) },
        icon = if (isPaused) IconName.Play else IconName.Pause,
    )
    Button(text = stringResource(Res.string.logcat_export), onClick = {
        onIntent(LogcatIntent.ExportClicked)
    }, variant = ButtonVariant.Ghost, icon = IconName.Download)
    Button(
        text = stringResource(Res.string.logcat_clear),
        onClick = { onIntent(LogcatIntent.ClearClicked) },
        variant = ButtonVariant.Ghost,
        icon = IconName.Delete,
    )
}

/** One line when there is room, otherwise the levels go on a line of their own. */
@Composable
private fun FilterBar(
    filter: LogFilter,
    packageName: String,
    showsPackageNotRunning: Boolean,
    onIntent: (LogcatIntent) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(bottom = AppTheme.spacing.lg)) {
        val search: @Composable RowScope.() -> Unit = {
            SearchField(
                query = filter.query,
                onQueryChange = { onIntent(LogcatIntent.QueryChanged(it)) },
                placeholder = stringResource(Res.string.logcat_search_placeholder),
                modifier = Modifier.weight(1f),
            )
            TextField(
                value = packageName,
                onValueChange = { onIntent(LogcatIntent.PackageChanged(it)) },
                modifier = Modifier.width(240.dp),
                placeholder = stringResource(Res.string.logcat_package_placeholder),
                leadingIcon = IconName.Package,
                monospace = true,
                trailing = if (showsPackageNotRunning) {
                    { Badge(stringResource(Res.string.logcat_package_not_running), tone = Tone.Warning) }
                } else {
                    null
                },
            )
        }
        if (maxWidth < SINGLE_LINE_FILTERS_WIDTH) {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md), content = search)
                LevelChips(filter.minimumLevel, onIntent)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                search()
                LevelChips(filter.minimumLevel, onIntent)
            }
        }
    }
}

@Composable
private fun LevelChips(selected: LogLevel, onIntent: (LogcatIntent) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        LogLevel.entries.forEach { level ->
            Chip(stringResource(level.label), selected = level == selected, onClick = { onIntent(LogcatIntent.LevelSelected(level)) })
        }
    }
}

private val SINGLE_LINE_FILTERS_WIDTH = 900.dp

@Composable
private fun LogBody(uiState: LogcatUiState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val rows = uiState.visibleRows
    Column(modifier.fillMaxSize()) {
        uiState.failure?.let { failure ->
            ReconnectingBar(failure = failure, onRetry = onRetry)
            Divider()
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (rows.isEmpty()) {
                Text(stringResource(Res.string.logcat_waiting), emphasis = TextEmphasis.Medium)
            } else {
                LogList(rows = rows, followTail = !uiState.isPaused)
            }
        }
    }
}

@Composable
private fun ReconnectingBar(failure: String, onRetry: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        Badge(stringResource(Res.string.logcat_reconnecting), tone = Tone.Warning)
        Text(failure, modifier = Modifier.weight(1f), tone = Tone.Error, maxLines = 1)
        Button(text = stringResource(Res.string.logcat_retry), onClick = onRetry, variant = ButtonVariant.Ghost, icon = IconName.Refresh)
    }
}

@Composable
private fun LogList(rows: List<LogRow>, followTail: Boolean, listState: LazyListState = rememberLazyListState()) {
    LaunchedEffect(rows.last().id, followTail) {
        if (followTail) listState.scrollToItem(rows.lastIndex)
    }
    SelectionContainer {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(rows, key = { it.id }) { row -> LogLine(row.entry) }
        }
    }
}

@Composable
private fun LogLine(entry: LogEntry) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        MonospaceText(entry.timestamp, emphasis = TextEmphasis.Medium, maxLines = 1)
        MonospaceText(entry.level.name.take(1), tone = entry.level.tone, maxLines = 1)
        MonospaceText(entry.tag, modifier = Modifier.width(180.dp), emphasis = TextEmphasis.Medium, maxLines = 1)
        MonospaceText(entry.message, modifier = Modifier.weight(1f), tone = entry.level.tone)
    }
}

private suspend fun LogcatSideEffect.message(): String = when (this) {
    LogcatSideEffect.ClearFailed -> getString(Res.string.logcat_clear_failed)
    is LogcatSideEffect.Exported -> getString(Res.string.logcat_exported, entryCount, path)
}

private val LogLevel.label: StringResource
    get() = when (this) {
        LogLevel.VERBOSE -> Res.string.logcat_level_verbose
        LogLevel.DEBUG -> Res.string.logcat_level_debug
        LogLevel.INFO -> Res.string.logcat_level_info
        LogLevel.WARNING -> Res.string.logcat_level_warning
        LogLevel.ERROR -> Res.string.logcat_level_error
        LogLevel.FATAL -> Res.string.logcat_level_fatal
    }

private val LogLevel.tone: Tone
    get() = when (this) {
        LogLevel.VERBOSE, LogLevel.DEBUG -> Tone.Neutral
        LogLevel.INFO -> Tone.Info
        LogLevel.WARNING -> Tone.Warning
        LogLevel.ERROR, LogLevel.FATAL -> Tone.Error
    }

private fun previewRow(
    id: Long,
    level: LogLevel,
    tag: String,
    message: String,
) = LogRow(
    id = id,
    entry = LogEntry(timestamp = "10-31 23:05:50.598", processId = 1537, threadId = 4112, level = level, tag = tag, message = message),
)

private val previewRows = listOf(
    previewRow(id = 1, LogLevel.INFO, "ActivityManager", "Start proc for activity"),
    previewRow(id = 2, LogLevel.WARNING, "NetworkScheduler", "Task finished late"),
    previewRow(id = 3, LogLevel.ERROR, "AndroidRuntime", "FATAL EXCEPTION: main"),
)

@UiKitPreview
@Composable
private fun LogcatContentStreamingPreview() = PreviewContext {
    LogcatContent(uiState = LogcatUiState(serial = "emulator-5554", rows = previewRows), onIntent = {})
}

@UiKitPreview
@Composable
private fun LogcatContentNoDevicePreview() = PreviewContext {
    LogcatContent(uiState = LogcatUiState(), onIntent = {})
}

@UiKitPreview
@Composable
private fun LogcatContentFailedPreview() = PreviewContext {
    LogcatContent(uiState = LogcatUiState(serial = "emulator-5554", failure = "error: device offline"), onIntent = {})
}
