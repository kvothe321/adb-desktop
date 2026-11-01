package com.tlpcraft.adbdesktop.shell

import androidx.compose.runtime.Composable
import com.tlpcraft.adbdesktop.navigation.Section
import com.tlpcraft.adbdesktop.navigation.shortcutHint
import com.tlpcraft.adbdesktop.resources.Res
import com.tlpcraft.adbdesktop.resources.palette_empty
import com.tlpcraft.adbdesktop.resources.palette_group_app
import com.tlpcraft.adbdesktop.resources.palette_group_devices
import com.tlpcraft.adbdesktop.resources.palette_group_go
import com.tlpcraft.adbdesktop.resources.palette_placeholder
import com.tlpcraft.adbdesktop.resources.palette_switch_device
import com.tlpcraft.adbdesktop.resources.phone_hide
import com.tlpcraft.adbdesktop.resources.phone_show
import com.tlpcraft.adbdesktop.resources.theme_switch_to_dark
import com.tlpcraft.adbdesktop.resources.theme_switch_to_light
import com.tlpcraft.adbdesktop.shell.contract.AppShellIntent
import com.tlpcraft.adbdesktop.shell.contract.AppShellUiState
import com.tlpcraft.adbdesktop.uikit.component.Command
import com.tlpcraft.adbdesktop.uikit.component.CommandPalette
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import org.jetbrains.compose.resources.stringResource

/** Every section, every online device and the app toggles, one search away. */
@Composable
internal fun AppCommandPalette(
    uiState: AppShellUiState,
    isDarkTheme: Boolean,
    onSectionSelect: (Section) -> Unit,
    onIntent: (AppShellIntent) -> Unit,
    onDismiss: () -> Unit,
) {
    val goTo = stringResource(Res.string.palette_group_go)
    val devices = stringResource(Res.string.palette_group_devices)
    val app = stringResource(Res.string.palette_group_app)
    val sectionCommands = Section.entries.map { section ->
        Command(id = "$SECTION_PREFIX${section.name}", title = stringResource(section.label), icon = section.icon, group = goTo, hint = section.shortcutHint())
    }
    val deviceCommands = uiState.devices.map { device ->
        Command(
            id = "$DEVICE_PREFIX${device.serial}",
            title = stringResource(Res.string.palette_switch_device, device.name),
            icon = IconName.Devices,
            group = devices,
            hint = device.serial,
        )
    }
    val appCommands = listOf(
        Command(PHONE_ID, stringResource(if (uiState.isPhonePanelShown) Res.string.phone_hide else Res.string.phone_show), IconName.Mirror, app),
        Command(
            THEME_ID,
            stringResource(if (isDarkTheme) Res.string.theme_switch_to_light else Res.string.theme_switch_to_dark),
            if (isDarkTheme) IconName.Sun else IconName.Moon,
            app,
        ),
    )
    CommandPalette(
        commands = sectionCommands + deviceCommands + appCommands,
        onSelect = { command ->
            onDismiss()
            when {
                command.id.startsWith(SECTION_PREFIX) -> onSectionSelect(Section.valueOf(command.id.removePrefix(SECTION_PREFIX)))
                command.id.startsWith(DEVICE_PREFIX) -> onIntent(AppShellIntent.DeviceSelected(command.id.removePrefix(DEVICE_PREFIX)))
                command.id == PHONE_ID -> onIntent(AppShellIntent.PhoneToggled)
                command.id == THEME_ID -> onIntent(AppShellIntent.ThemeToggled(isDarkTheme))
            }
        },
        onDismissRequest = onDismiss,
        placeholder = stringResource(Res.string.palette_placeholder),
        emptyText = stringResource(Res.string.palette_empty),
    )
}

private const val SECTION_PREFIX = "section:"
private const val DEVICE_PREFIX = "device:"
private const val PHONE_ID = "phone"
private const val THEME_ID = "theme"
