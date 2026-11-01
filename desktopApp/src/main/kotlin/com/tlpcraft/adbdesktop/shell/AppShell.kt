package com.tlpcraft.adbdesktop.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlpcraft.adbdesktop.navigation.Section
import com.tlpcraft.adbdesktop.navigation.shortcutHint
import com.tlpcraft.adbdesktop.resources.Res
import com.tlpcraft.adbdesktop.resources.app_logo
import com.tlpcraft.adbdesktop.resources.device_picker_placeholder
import com.tlpcraft.adbdesktop.resources.palette_open
import com.tlpcraft.adbdesktop.resources.phone_hide
import com.tlpcraft.adbdesktop.resources.phone_show
import com.tlpcraft.adbdesktop.resources.section_settings
import com.tlpcraft.adbdesktop.resources.theme_switch_to_dark
import com.tlpcraft.adbdesktop.resources.theme_switch_to_light
import com.tlpcraft.adbdesktop.shell.contract.AppShellIntent
import com.tlpcraft.adbdesktop.shell.contract.AppShellUiState
import com.tlpcraft.adbdesktop.shell.contract.DeviceOption
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.Dropdown
import com.tlpcraft.adbdesktop.uikit.component.IconButton
import com.tlpcraft.adbdesktop.uikit.component.MessageHostState
import com.tlpcraft.adbdesktop.uikit.component.Scaffold
import com.tlpcraft.adbdesktop.uikit.component.SegmentedTabs
import com.tlpcraft.adbdesktop.uikit.component.TabItem
import com.tlpcraft.adbdesktop.uikit.component.rememberMessageHostState
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import com.tlpcraft.adbdesktop.uikit.theme.WindowWidth
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The device workspace: a top bar with the device switcher, section tabs and app actions, the current section in
 * [content], and the live phone from [phone] on the right unless the user hid it. Both slots receive a callback to show
 * transient messages. While [isPaletteOpen] the command palette floats above everything.
 */
@Composable
fun AppShellScreen(
    currentSection: Section,
    onSectionSelect: (Section) -> Unit,
    isPaletteOpen: Boolean,
    onPaletteOpenChange: (Boolean) -> Unit,
    phone: @Composable (onShowMessage: (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppShellViewModel = koinViewModel(),
    content: @Composable (onShowMessage: (String) -> Unit) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messageHostState = rememberMessageHostState()
    val scope = rememberCoroutineScope()

    val isDarkTheme = uiState.isDarkTheme(isSystemDark = isSystemInDarkTheme())
    val onShowMessage: (String) -> Unit = { message -> scope.launch { messageHostState.show(message) } }

    AppTheme(darkTheme = isDarkTheme) {
        AppShellContent(
            uiState = uiState,
            currentSection = currentSection,
            isDarkTheme = isDarkTheme,
            messageHostState = messageHostState,
            onIntent = viewModel::reduceIntent,
            onSectionSelect = onSectionSelect,
            onPaletteOpen = { onPaletteOpenChange(true) },
            modifier = modifier,
            phone = if (uiState.isPhonePanelShown) ({ phone(onShowMessage) }) else null,
        ) {
            content(onShowMessage)
        }
        if (isPaletteOpen) {
            AppCommandPalette(
                uiState = uiState,
                isDarkTheme = isDarkTheme,
                onSectionSelect = onSectionSelect,
                onIntent = viewModel::reduceIntent,
                onDismiss = { onPaletteOpenChange(false) },
            )
        }
    }
}

@Composable
fun AppShellContent(
    uiState: AppShellUiState,
    currentSection: Section,
    isDarkTheme: Boolean,
    messageHostState: MessageHostState,
    onIntent: (AppShellIntent) -> Unit,
    onSectionSelect: (Section) -> Unit,
    onPaletteOpen: () -> Unit,
    modifier: Modifier = Modifier,
    phone: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        messageHostState = messageHostState,
        side = phone,
        topBar = {
            WorkspaceBar(
                uiState = uiState,
                currentSection = currentSection,
                isDarkTheme = isDarkTheme,
                onIntent = onIntent,
                onSectionSelect = onSectionSelect,
                onPaletteOpen = onPaletteOpen,
            )
        },
        content = content,
    )
}

/** Device on the left, sections in the middle, app actions on the right. Narrow windows keep only the tab icons. */
@Composable
private fun WorkspaceBar(
    uiState: AppShellUiState,
    currentSection: Section,
    isDarkTheme: Boolean,
    onIntent: (AppShellIntent) -> Unit,
    onSectionSelect: (Section) -> Unit,
    onPaletteOpen: () -> Unit,
) {
    val isCompact = AppTheme.windowWidth == WindowWidth.Compact
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            Image(painterResource(Res.drawable.app_logo), contentDescription = null, modifier = Modifier.size(28.dp))
            DeviceSwitcher(uiState = uiState, onDeviceSelect = { onIntent(AppShellIntent.DeviceSelected(it.serial)) })
        }
        SectionTabs(currentSection = currentSection, onSectionSelect = onSectionSelect, isIconOnly = AppTheme.windowWidth != WindowWidth.Expanded)
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            Spacer(Modifier.weight(1f))
            if (isCompact) {
                IconButton(icon = IconName.Search, contentDescription = stringResource(Res.string.palette_open), onClick = onPaletteOpen)
            } else {
                Button(text = stringResource(Res.string.palette_open), onClick = onPaletteOpen, icon = IconName.Search)
            }
            IconButton(
                icon = IconName.Mirror,
                contentDescription = stringResource(if (uiState.isPhonePanelShown) Res.string.phone_hide else Res.string.phone_show),
                onClick = { onIntent(AppShellIntent.PhoneToggled) },
                selected = uiState.isPhonePanelShown,
            )
            IconButton(
                icon = if (isDarkTheme) IconName.Sun else IconName.Moon,
                contentDescription = stringResource(if (isDarkTheme) Res.string.theme_switch_to_light else Res.string.theme_switch_to_dark),
                onClick = { onIntent(AppShellIntent.ThemeToggled(isDarkTheme)) },
            )
            IconButton(
                icon = IconName.Settings,
                contentDescription = stringResource(Res.string.section_settings),
                onClick = { onSectionSelect(Section.Settings) },
                selected = currentSection == Section.Settings,
            )
        }
    }
}

@Composable
private fun DeviceSwitcher(uiState: AppShellUiState, onDeviceSelect: (DeviceOption) -> Unit) {
    Dropdown(
        items = uiState.devices,
        selected = uiState.selectedDevice,
        onSelect = onDeviceSelect,
        itemLabel = DeviceOption::name,
        itemDetail = DeviceOption::serial,
        placeholder = stringResource(Res.string.device_picker_placeholder),
        modifier = Modifier.widthIn(max = DEVICE_SWITCHER_MAX_WIDTH),
        icon = IconName.Devices,
    )
}

@Composable
private fun SectionTabs(currentSection: Section, onSectionSelect: (Section) -> Unit, isIconOnly: Boolean) {
    val tabs = Section.tabs
    SegmentedTabs(
        items = tabs.map { section -> TabItem(stringResource(section.label), section.icon, hint = section.shortcutHint()) },
        selectedIndex = tabs.indexOf(currentSection),
        onSelect = { onSectionSelect(tabs[it]) },
        isIconOnly = isIconOnly,
    )
}

private val DEVICE_SWITCHER_MAX_WIDTH = 260.dp

@UiKitPreview
@Composable
private fun AppShellContentPreview() = PreviewContext {
    AppShellContent(
        uiState = AppShellUiState(devices = listOf(DeviceOption("R5CT10ABCDE", "SM-A525F")), selectedSerial = "R5CT10ABCDE"),
        currentSection = Section.Devices,
        isDarkTheme = false,
        messageHostState = rememberMessageHostState(),
        onIntent = {},
        onSectionSelect = {},
        onPaletteOpen = {},
    ) {}
}
