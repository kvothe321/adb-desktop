package com.tlpcraft.adbdesktop.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlpcraft.adbdesktop.navigation.Section
import com.tlpcraft.adbdesktop.navigation.isMacOs
import com.tlpcraft.adbdesktop.resources.Res
import com.tlpcraft.adbdesktop.resources.app_logo
import com.tlpcraft.adbdesktop.resources.app_name
import com.tlpcraft.adbdesktop.resources.device_picker_placeholder
import com.tlpcraft.adbdesktop.resources.shortcut_cmd
import com.tlpcraft.adbdesktop.resources.shortcut_ctrl
import com.tlpcraft.adbdesktop.resources.theme_switch_to_dark
import com.tlpcraft.adbdesktop.resources.theme_switch_to_light
import com.tlpcraft.adbdesktop.shell.contract.AppShellIntent
import com.tlpcraft.adbdesktop.shell.contract.AppShellUiState
import com.tlpcraft.adbdesktop.shell.contract.DeviceOption
import com.tlpcraft.adbdesktop.uikit.component.Dropdown
import com.tlpcraft.adbdesktop.uikit.component.IconButton
import com.tlpcraft.adbdesktop.uikit.component.MessageHostState
import com.tlpcraft.adbdesktop.uikit.component.Scaffold
import com.tlpcraft.adbdesktop.uikit.component.Sidebar
import com.tlpcraft.adbdesktop.uikit.component.SidebarItem
import com.tlpcraft.adbdesktop.uikit.component.Text
import com.tlpcraft.adbdesktop.uikit.component.TextVariant
import com.tlpcraft.adbdesktop.uikit.component.rememberMessageHostState
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Window chrome: sidebar navigation, device picker, the message host and the mirror dock. [content] renders the
 * current section and [mirror] the device mirror; both receive a callback to show transient messages. The mirror
 * shows beside the Screen section, or beside every section once pinned.
 */
@Composable
fun AppShellScreen(
    currentSection: Section,
    onSectionSelect: (Section) -> Unit,
    mirror: @Composable (onShowMessage: (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppShellViewModel = koinViewModel(),
    content: @Composable (onShowMessage: (String) -> Unit) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messageHostState = rememberMessageHostState()
    val scope = rememberCoroutineScope()

    val isDarkTheme = uiState.isDarkTheme(isSystemDark = isSystemInDarkTheme())
    val onShowMessage: (String) -> Unit = { message -> scope.launch { messageHostState.show(message) } }
    val isMirrorShown = uiState.isMirrorPinned || currentSection == Section.Screen

    AppTheme(darkTheme = isDarkTheme) {
        AppShellContent(
            uiState = uiState,
            currentSection = currentSection,
            isDarkTheme = isDarkTheme,
            messageHostState = messageHostState,
            onIntent = viewModel::reduceIntent,
            onSectionSelect = onSectionSelect,
            modifier = modifier,
            dock = if (isMirrorShown) ({ mirror(onShowMessage) }) else null,
        ) {
            content(onShowMessage)
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
    modifier: Modifier = Modifier,
    dock: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        messageHostState = messageHostState,
        dock = dock,
        sidebar = {
            Sidebar(
                header = { SidebarHeader(uiState = uiState, onDeviceSelect = { onIntent(AppShellIntent.DeviceSelected(it.serial)) }) },
                footer = { ThemeToggle(isDarkTheme = isDarkTheme, onThemeToggle = { onIntent(AppShellIntent.ThemeToggled(isDarkTheme)) }) },
            ) {
                Section.entries.forEach { section ->
                    SidebarItem(
                        label = stringResource(section.label),
                        icon = section.icon,
                        selected = section == currentSection,
                        onClick = { onSectionSelect(section) },
                        shortcut = stringResource(if (isMacOs) Res.string.shortcut_cmd else Res.string.shortcut_ctrl, section.shortcutDigit),
                    )
                }
            }
        },
        content = content,
    )
}

@Composable
private fun SidebarHeader(uiState: AppShellUiState, onDeviceSelect: (DeviceOption) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            Image(painterResource(Res.drawable.app_logo), contentDescription = null, modifier = Modifier.size(28.dp))
            Text(stringResource(Res.string.app_name), variant = TextVariant.Heading)
        }
        Dropdown(
            items = uiState.devices,
            selected = uiState.selectedDevice,
            onSelect = onDeviceSelect,
            itemLabel = DeviceOption::name,
            itemDetail = DeviceOption::serial,
            placeholder = stringResource(Res.string.device_picker_placeholder),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ThemeToggle(isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        IconButton(
            icon = if (isDarkTheme) IconName.Sun else IconName.Moon,
            contentDescription = stringResource(if (isDarkTheme) Res.string.theme_switch_to_light else Res.string.theme_switch_to_dark),
            onClick = onThemeToggle,
        )
    }
}

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
    ) {}
}
