package com.tlpcraft.adbdesktop.feature.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlpcraft.adbdesktop.core.domain.model.ConnectionType
import com.tlpcraft.adbdesktop.core.domain.model.DeviceState
import com.tlpcraft.adbdesktop.feature.devices.component.AdbSetupPanel
import com.tlpcraft.adbdesktop.feature.devices.component.ConnectDialog
import com.tlpcraft.adbdesktop.feature.devices.component.DeviceCard
import com.tlpcraft.adbdesktop.feature.devices.component.QuickActionTile
import com.tlpcraft.adbdesktop.feature.devices.component.SectionHeader
import com.tlpcraft.adbdesktop.feature.devices.component.StatCard
import com.tlpcraft.adbdesktop.feature.devices.component.batteryTone
import com.tlpcraft.adbdesktop.feature.devices.component.cpuTone
import com.tlpcraft.adbdesktop.feature.devices.component.label
import com.tlpcraft.adbdesktop.feature.devices.contract.ConnectFormState
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceItem
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceListState
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceStats
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesFailure
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesIntent
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesSideEffect
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesUiState
import com.tlpcraft.adbdesktop.feature.devices.contract.QuickAction
import com.tlpcraft.adbdesktop.feature.devices.resources.Res
import com.tlpcraft.adbdesktop.feature.devices.resources.device_android
import com.tlpcraft.adbdesktop.feature.devices.resources.device_battery
import com.tlpcraft.adbdesktop.feature.devices.resources.device_connection
import com.tlpcraft.adbdesktop.feature.devices.resources.device_cpu
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_connect_wifi
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_empty_description
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_empty_title
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_loading
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_title
import com.tlpcraft.adbdesktop.feature.devices.resources.message_action_failed
import com.tlpcraft.adbdesktop.feature.devices.resources.message_connected
import com.tlpcraft.adbdesktop.feature.devices.resources.message_disconnect_failed
import com.tlpcraft.adbdesktop.feature.devices.resources.message_disconnected
import com.tlpcraft.adbdesktop.feature.devices.resources.message_installed
import com.tlpcraft.adbdesktop.feature.devices.resources.message_paired
import com.tlpcraft.adbdesktop.feature.devices.resources.message_screenshot_copied
import com.tlpcraft.adbdesktop.feature.devices.resources.overview_all_devices
import com.tlpcraft.adbdesktop.feature.devices.resources.overview_other_devices
import com.tlpcraft.adbdesktop.feature.devices.resources.overview_quick_actions
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ButtonVariant
import com.tlpcraft.adbdesktop.uikit.component.EmptyState
import com.tlpcraft.adbdesktop.uikit.component.Page
import com.tlpcraft.adbdesktop.uikit.component.ProgressIndicator
import com.tlpcraft.adbdesktop.uikit.component.Text
import com.tlpcraft.adbdesktop.uikit.component.TextEmphasis
import com.tlpcraft.adbdesktop.uikit.component.TopBar
import com.tlpcraft.adbdesktop.uikit.component.VisibilityEffect
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The overview of the selected device, with every other device below it. One-shot results go through [onShowMessage],
 * and quick actions that belong to another section go to [onSectionRequest].
 */
@Composable
fun DevicesScreen(
    onShowMessage: (String) -> Unit,
    onSectionRequest: (QuickAction) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DevicesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)
    val currentOnSectionRequest by rememberUpdatedState(onSectionRequest)

    VisibilityEffect { viewModel.reduceIntent(DevicesIntent.VisibilityChanged(it)) }
    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect ->
            if (effect is DevicesSideEffect.SectionRequested) currentOnSectionRequest(effect.action)
            effect.message()?.let(currentOnShowMessage)
        }
    }

    DevicesContent(uiState = uiState, onIntent = viewModel::reduceIntent, modifier = modifier)
}

@Composable
fun DevicesContent(uiState: DevicesUiState, onIntent: (DevicesIntent) -> Unit, modifier: Modifier = Modifier) {
    val selected = uiState.selectedDevice
    Page(
        title = selected?.let { it.name ?: it.serial } ?: stringResource(Res.string.devices_title),
        modifier = modifier,
        actions = {
            Button(
                text = stringResource(Res.string.devices_connect_wifi),
                onClick = { onIntent(DevicesIntent.ConnectFormOpened) },
                variant = ButtonVariant.Primary,
                icon = IconName.Wifi,
            )
        },
    ) {
        Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val list = uiState.list) {
                DeviceListState.Loading -> LoadingState()

                is DeviceListState.Failed -> AdbSetupPanel(
                    failure = list.failure,
                    adbPathInput = uiState.adbPathInput,
                    onAdbPathChange = { onIntent(DevicesIntent.AdbPathChanged(it)) },
                    onAdbPathSubmit = { onIntent(DevicesIntent.AdbPathSubmitted) },
                )

                is DeviceListState.Loaded -> if (list.devices.isEmpty()) {
                    EmptyState(
                        title = stringResource(Res.string.devices_empty_title),
                        description = stringResource(Res.string.devices_empty_description),
                        icon = IconName.Devices,
                    )
                } else {
                    OverviewGrid(uiState = uiState, onIntent = onIntent)
                }
            }
        }
        uiState.connectForm?.let { form ->
            ConnectDialog(
                form = form,
                onModeSelect = { onIntent(DevicesIntent.ConnectModeSelected(it)) },
                onAddressChange = { onIntent(DevicesIntent.AddressChanged(it)) },
                onPairingCodeChange = { onIntent(DevicesIntent.PairingCodeChanged(it)) },
                onSubmit = { onIntent(DevicesIntent.ConnectSubmitted) },
                onDismiss = { onIntent(DevicesIntent.ConnectFormDismissed) },
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
        ProgressIndicator()
        Text(stringResource(Res.string.devices_loading), emphasis = TextEmphasis.Medium)
    }
}

/** Figures and actions for the selected device first, then every other device to switch to. */
@Composable
private fun OverviewGrid(uiState: DevicesUiState, onIntent: (DevicesIntent) -> Unit) {
    val selected = uiState.selectedDevice
    val stats = selected?.let { uiState.stats[it.serial] }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = TILE_MIN_WIDTH),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = AppTheme.spacing.xxs, bottom = AppTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
    ) {
        if (selected != null) {
            stats?.batteryLevel?.let { level ->
                item(key = "battery") { StatCard(stringResource(Res.string.device_battery), "$level%", fraction = level / PERCENT, tone = batteryTone(level)) }
            }
            stats?.cpuUsagePercent?.let { usage ->
                item(key = "cpu") { StatCard(stringResource(Res.string.device_cpu), "${usage.toInt()}%", fraction = usage / PERCENT, tone = cpuTone(usage)) }
            }
            stats?.androidVersion?.let { version -> item(key = "android") { StatCard(stringResource(Res.string.device_android), version) } }
            item(key = "connection") { StatCard(stringResource(Res.string.device_connection), stringResource(selected.connectionType.label)) }
            item(key = "actions-header", span = { GridItemSpan(maxLineSpan) }) { SectionHeader(stringResource(Res.string.overview_quick_actions)) }
            items(QuickAction.entries, key = { it.name }) { action ->
                QuickActionTile(
                    action = action,
                    isRunning = uiState.runningAction == action,
                    onClick = { onIntent(DevicesIntent.QuickActionClicked(action)) },
                )
            }
        }
        val others = uiState.otherDevices
        if (others.isNotEmpty()) {
            item(key = "devices-header", span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(stringResource(if (selected == null) Res.string.overview_all_devices else Res.string.overview_other_devices))
            }
        }
        items(others, key = { it.serial }, span = { GridItemSpan(minOf(DEVICE_CARD_SPAN, maxLineSpan)) }) { device ->
            DeviceCard(
                device = device,
                stats = uiState.stats[device.serial],
                selected = false,
                onClick = { onIntent(DevicesIntent.DeviceClicked(device.serial)) },
                onDisconnect = { onIntent(DevicesIntent.DisconnectClicked(device.serial)) },
            )
        }
    }
}

private val TILE_MIN_WIDTH = 200.dp
private const val DEVICE_CARD_SPAN = 2
private const val PERCENT = 100f

/** The text to show for [this], or `null` for effects that are not messages. */
private suspend fun DevicesSideEffect.message(): String? = when (this) {
    is DevicesSideEffect.Connected -> getString(Res.string.message_connected, address)
    is DevicesSideEffect.Paired -> getString(Res.string.message_paired, address)
    is DevicesSideEffect.Disconnected -> getString(Res.string.message_disconnected, serial)
    is DevicesSideEffect.DisconnectFailed -> getString(Res.string.message_disconnect_failed, serial)
    DevicesSideEffect.ScreenshotCopied -> getString(Res.string.message_screenshot_copied)
    is DevicesSideEffect.Installed -> getString(Res.string.message_installed, count)
    is DevicesSideEffect.ActionFailed -> getString(Res.string.message_action_failed, details)
    is DevicesSideEffect.SectionRequested -> null
}

@UiKitPreview
@Composable
private fun DevicesContentLoadedPreview() = PreviewContext {
    DevicesContent(
        uiState = DevicesUiState(
            list = DeviceListState.Loaded(
                listOf(
                    DeviceItem("R5CT10ABCDE", "SM-A525F", DeviceState.ONLINE, ConnectionType.USB),
                    DeviceItem("192.168.1.42:5555", "Pixel 8", DeviceState.ONLINE, ConnectionType.WIRELESS),
                    DeviceItem("emulator-5554", null, DeviceState.OFFLINE, ConnectionType.EMULATOR),
                ),
            ),
            stats = mapOf("R5CT10ABCDE" to DeviceStats("14", batteryLevel = 87, cpuUsagePercent = 23f)),
            selectedSerial = "R5CT10ABCDE",
        ),
        onIntent = {},
    )
}

@UiKitPreview
@Composable
private fun DevicesContentEmptyPreview() = PreviewContext {
    DevicesContent(uiState = DevicesUiState(list = DeviceListState.Loaded(emptyList())), onIntent = {})
}

@UiKitPreview
@Composable
private fun DevicesContentLoadingPreview() = PreviewContext {
    DevicesContent(uiState = DevicesUiState(), onIntent = {})
}

@UiKitPreview
@Composable
private fun DevicesContentAdbMissingPreview() = PreviewContext {
    DevicesContent(uiState = DevicesUiState(list = DeviceListState.Failed(DevicesFailure.AdbNotFound(listOf("/usr/bin/adb")))), onIntent = {})
}

@UiKitPreview
@Composable
private fun DevicesContentConnectFormPreview() = PreviewContext {
    DevicesContent(uiState = DevicesUiState(list = DeviceListState.Loaded(emptyList()), connectForm = ConnectFormState()), onIntent = {})
}
