package com.tlpcraft.adbdesktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.tlpcraft.adbdesktop.navigation.AppsRoute
import com.tlpcraft.adbdesktop.navigation.DevicesRoute
import com.tlpcraft.adbdesktop.navigation.FilesRoute
import com.tlpcraft.adbdesktop.navigation.config
import com.tlpcraft.adbdesktop.presentation.AppsScreen
import com.tlpcraft.adbdesktop.presentation.DevicesScreen
import com.tlpcraft.adbdesktop.presentation.FilesScreen
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppShell(isDark: Boolean, onToggleTheme: () -> Unit) {
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

    val sharedViewModel = koinViewModel<SharedDeviceViewModel>()
    val devicesState by sharedViewModel.devicesState.collectAsState()
    val selectedDevice by sharedViewModel.selectedDevice.collectAsState()
    val deviceLabel = selectedDevice?.serial ?: "No device selected"

    Row(Modifier.fillMaxSize().background(colorScheme.background)) {
        PermanentDrawerSheet(
            modifier = Modifier.width(220.dp),
            drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            // ── Header ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "ADB Desktop",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = deviceLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedDevice != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))

            // ── Nav items ─────────────────────────────────────────
            Text(
                text = "NAVIGATION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )

            NavigationDrawerItem(
                label = { Text("Devices") },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                selected = selectedSection is DevicesRoute,
                onClick = { selectedSection = DevicesRoute },
                modifier = Modifier.padding(horizontal = 8.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            NavigationDrawerItem(
                label = { Text("Apps") },
                icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                selected = selectedSection is AppsRoute,
                onClick = { selectedSection = AppsRoute },
                modifier = Modifier.padding(horizontal = 8.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            NavigationDrawerItem(
                label = { Text("Files") },
                icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                selected = selectedSection is FilesRoute,
                onClick = { selectedSection = FilesRoute },
                modifier = Modifier.padding(horizontal = 8.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            // ── Push remaining content to bottom ──────────────────
            Spacer(Modifier.weight(1f))

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
                IconButton(onClick = onToggleTheme) {
                    Text(
                        text = if (isDark) "☀" else "🌙",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
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
                            onDeselectDevice = { sharedViewModel.deselect() }
                        )
                    }

                    is AppsRoute -> NavEntry(key) { AppsScreen(selectedDevice = selectedDevice) }

                    is FilesRoute -> NavEntry(key) { FilesScreen() }

                    else -> NavEntry(key) { Text("Unknown route") }
                }
            }
        )
    }
}
