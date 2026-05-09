package com.tlpcraft.adbdesktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.tlpcraft.adbdesktop.component.SideDrawer
import com.tlpcraft.adbdesktop.component.TopAppBar
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.navigation.AppsRoute
import com.tlpcraft.adbdesktop.navigation.DevicesRoute
import com.tlpcraft.adbdesktop.navigation.FilesRoute
import com.tlpcraft.adbdesktop.navigation.config
import com.tlpcraft.adbdesktop.presentation.AppsScreen
import com.tlpcraft.adbdesktop.presentation.DevicesScreen
import com.tlpcraft.adbdesktop.presentation.FilesScreen
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppShell(isDark: Boolean, onToggleTheme: () -> Unit) {
    val sharedViewModel = koinViewModel<SharedDeviceViewModel>()
    val devicesState by sharedViewModel.devicesState.collectAsState()
    val selectedDevice by sharedViewModel.selectedDevice.collectAsState()
    val devices = devicesState?.fold(onSuccess = { it }, onFailure = { emptyList() }) ?: emptyList()

    val devicesStack = rememberNavBackStack(config, DevicesRoute)
    val appsStack = rememberNavBackStack(config, AppsRoute)
    val filesStack = rememberNavBackStack(config, FilesRoute)

    var selectedSection by remember { mutableStateOf<NavKey>(DevicesRoute) }

    val activeStack = when (selectedSection) {
        is DevicesRoute -> devicesStack
        is AppsRoute -> appsStack
        is FilesRoute -> filesStack
        else -> devicesStack
    }

    AppShellContent(
        selectedSection = selectedSection,
        devices = devices,
        selectedDevice = selectedDevice,
        isDark = isDark,
        onSectionSelected = { selectedSection = it },
        onToggleTheme = onToggleTheme,
        onDeviceSelected = { sharedViewModel.select(it.serial) },
        onDeviceDeselected = { sharedViewModel.deselect() },
    ) {
        NavDisplay(
            backStack = activeStack,
            onBack = { activeStack.removeLastOrNull() },
            sceneStrategies = listOf(SinglePaneSceneStrategy()),
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
            entryProvider = { key ->
                when (key) {
                    is DevicesRoute -> NavEntry(key) {
                        DevicesScreen(
                            devicesOutcome = devicesState,
                            selectedSerial = selectedDevice?.serial,
                            onSelectDevice = { sharedViewModel.select(it) },
                            onDeselectDevice = { sharedViewModel.deselect() },
                        )
                    }

                    is AppsRoute -> NavEntry(key) { AppsScreen(selectedDevice = selectedDevice) }

                    is FilesRoute -> NavEntry(key) { FilesScreen() }

                    else -> NavEntry(key) { Text("Unknown route") }
                }
            },
        )
    }
}

@Composable
fun AppShellContent(
    selectedSection: NavKey,
    devices: List<AdbDevice>,
    selectedDevice: AdbDevice?,
    isDark: Boolean,
    onSectionSelected: (NavKey) -> Unit,
    onToggleTheme: () -> Unit,
    onDeviceSelected: (AdbDevice) -> Unit,
    onDeviceDeselected: () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(Modifier.fillMaxSize().background(colorScheme.background)) {
        SideDrawer(
            selectedSection = selectedSection,
            isDark = isDark,
            onSectionSelected = onSectionSelected,
            onToggleTheme = onToggleTheme,
        )
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                devices = devices,
                selectedDevice = selectedDevice,
                onDeviceSelected = onDeviceSelected,
                onDeviceDeselected = onDeviceDeselected,
            )
            content()
        }
    }
}

@UiKitPreview
@Composable
private fun AppShellPreview() = PreviewContext {
    AppShellContent(
        selectedSection = DevicesRoute,
        devices = listOf(AdbDevice("emulator-5554", "device")),
        selectedDevice = null,
        isDark = false,
        onSectionSelected = {},
        onToggleTheme = {},
        onDeviceSelected = {},
        onDeviceDeselected = {},
    ) {
        Box(Modifier.fillMaxSize())
    }
}
