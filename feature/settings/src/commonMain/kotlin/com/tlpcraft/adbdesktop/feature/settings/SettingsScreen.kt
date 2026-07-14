package com.tlpcraft.adbdesktop.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlpcraft.adbdesktop.core.domain.model.AdbInfo
import com.tlpcraft.adbdesktop.core.domain.model.ThemeMode
import com.tlpcraft.adbdesktop.feature.settings.contract.AdbStatus
import com.tlpcraft.adbdesktop.feature.settings.contract.SettingsIntent
import com.tlpcraft.adbdesktop.feature.settings.contract.SettingsUiState
import com.tlpcraft.adbdesktop.feature.settings.resources.Res
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_adb_check
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_adb_checking
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_adb_found
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_adb_path_label
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_adb_path_placeholder
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_adb_reset
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_adb_save
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_adb_section
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_adb_version
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_appearance_section
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_theme_dark
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_theme_light
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_theme_system
import com.tlpcraft.adbdesktop.feature.settings.resources.settings_title
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ButtonVariant
import com.tlpcraft.adbdesktop.uikit.component.Card
import com.tlpcraft.adbdesktop.uikit.component.Chip
import com.tlpcraft.adbdesktop.uikit.component.Text
import com.tlpcraft.adbdesktop.uikit.component.TextEmphasis
import com.tlpcraft.adbdesktop.uikit.component.TextField
import com.tlpcraft.adbdesktop.uikit.component.TextVariant
import com.tlpcraft.adbdesktop.uikit.component.Tone
import com.tlpcraft.adbdesktop.uikit.component.TopBar
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** App preferences: which adb to use and the theme. */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(uiState = uiState, onIntent = viewModel::reduceIntent, modifier = modifier)
}

@Composable
fun SettingsContent(uiState: SettingsUiState, onIntent: (SettingsIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        TopBar(title = stringResource(Res.string.settings_title))
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
        ) {
            Section(title = stringResource(Res.string.settings_adb_section)) {
                AdbStatusText(uiState.adbStatus)
                AdbPathForm(adbPathInput = uiState.adbPathInput, onIntent = onIntent)
            }
            Section(title = stringResource(Res.string.settings_appearance_section)) {
                ThemePicker(selected = uiState.themeMode, onSelect = { onIntent(SettingsIntent.ThemeSelected(it)) })
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.widthIn(max = 640.dp).fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            Text(title, variant = TextVariant.Heading)
            content()
        }
    }
}

@Composable
private fun AdbStatusText(status: AdbStatus) {
    when (status) {
        AdbStatus.Checking -> Text(stringResource(Res.string.settings_adb_checking), emphasis = TextEmphasis.Medium)
        is AdbStatus.Found -> AdbFound(status.info)
        is AdbStatus.Unavailable -> Text(status.details, tone = Tone.Error)
    }
}

@Composable
private fun AdbFound(info: AdbInfo) {
    Column {
        Text(stringResource(Res.string.settings_adb_found, info.path), tone = Tone.Success)
        info.version?.let { Text(stringResource(Res.string.settings_adb_version, it), emphasis = TextEmphasis.Medium) }
    }
}

@Composable
private fun AdbPathForm(adbPathInput: String, onIntent: (SettingsIntent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
        TextField(
            value = adbPathInput,
            onValueChange = { onIntent(SettingsIntent.AdbPathChanged(it)) },
            label = stringResource(Res.string.settings_adb_path_label),
            placeholder = stringResource(Res.string.settings_adb_path_placeholder),
            monospace = true,
            onSubmit = { onIntent(SettingsIntent.AdbPathSaved) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            Button(text = stringResource(Res.string.settings_adb_save), onClick = { onIntent(SettingsIntent.AdbPathSaved) }, variant = ButtonVariant.Primary)
            Button(text = stringResource(Res.string.settings_adb_reset), onClick = { onIntent(SettingsIntent.AdbPathReset) })
            Button(text = stringResource(Res.string.settings_adb_check), onClick = { onIntent(SettingsIntent.AdbCheckClicked) }, variant = ButtonVariant.Ghost)
        }
    }
}

@Composable
private fun ThemePicker(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        ThemeMode.entries.forEach { mode ->
            Chip(stringResource(mode.label), selected = mode == selected, onClick = { onSelect(mode) })
        }
    }
}

private val ThemeMode.label: StringResource
    get() = when (this) {
        ThemeMode.SYSTEM -> Res.string.settings_theme_system
        ThemeMode.LIGHT -> Res.string.settings_theme_light
        ThemeMode.DARK -> Res.string.settings_theme_dark
    }

@UiKitPreview
@Composable
private fun SettingsContentFoundPreview() = PreviewContext {
    SettingsContent(uiState = SettingsUiState(adbStatus = AdbStatus.Found(AdbInfo("/usr/bin/adb", "37.0.0"))), onIntent = {})
}

@UiKitPreview
@Composable
private fun SettingsContentUnavailablePreview() = PreviewContext {
    SettingsContent(
        uiState = SettingsUiState(adbStatus = AdbStatus.Unavailable("adb not found"), adbPathInput = "/opt/adb", themeMode = ThemeMode.DARK),
        onIntent = {},
    )
}
