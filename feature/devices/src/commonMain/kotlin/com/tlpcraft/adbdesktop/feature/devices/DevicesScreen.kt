package com.tlpcraft.adbdesktop.feature.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
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
import com.tlpcraft.adbdesktop.feature.devices.contract.ConnectFormState
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceItem
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceListState
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceStats
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesFailure
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesIntent
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesSideEffect
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesUiState
import com.tlpcraft.adbdesktop.feature.devices.resources.Res
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_connect_wifi
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_empty_description
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_empty_title
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_loading
import com.tlpcraft.adbdesktop.feature.devices.resources.devices_title
import com.tlpcraft.adbdesktop.feature.devices.resources.message_connected
import com.tlpcraft.adbdesktop.feature.devices.resources.message_disconnect_failed
import com.tlpcraft.adbdesktop.feature.devices.resources.message_disconnected
import com.tlpcraft.adbdesktop.feature.devices.resources.message_paired
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

/** Live device list. One-shot results are reported through [onShowMessage]. */
@Composable
fun DevicesScreen(onShowMessage: (String) -> Unit, modifier: Modifier = Modifier, viewModel: DevicesViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)

    VisibilityEffect { viewModel.reduceIntent(DevicesIntent.VisibilityChanged(it)) }
    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect -> currentOnShowMessage(effect.message()) }
    }

    DevicesContent(uiState = uiState, onIntent = viewModel::reduceIntent, modifier = modifier)
}

@Composable
fun DevicesContent(uiState: DevicesUiState, onIntent: (DevicesIntent) -> Unit, modifier: Modifier = Modifier) {
    Page(
        title = stringResource(Res.string.devices_title),
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
                    DeviceGrid(devices = list.devices, stats = uiState.stats, selectedSerial = uiState.selectedSerial, onIntent = onIntent)
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

@Composable
private fun DeviceGrid(
    devices: List<DeviceItem>,
    stats: Map<String, DeviceStats>,
    selectedSerial: String?,
    onIntent: (DevicesIntent) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = AppTheme.spacing.xxs, bottom = AppTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
    ) {
        items(devices, key = { it.serial }) { device ->
            DeviceCard(
                device = device,
                stats = stats[device.serial],
                selected = device.serial == selectedSerial,
                onClick = { onIntent(DevicesIntent.DeviceClicked(device.serial)) },
                onDisconnect = { onIntent(DevicesIntent.DisconnectClicked(device.serial)) },
            )
        }
    }
}

private suspend fun DevicesSideEffect.message(): String = when (this) {
    is DevicesSideEffect.Connected -> getString(Res.string.message_connected, address)
    is DevicesSideEffect.Paired -> getString(Res.string.message_paired, address)
    is DevicesSideEffect.Disconnected -> getString(Res.string.message_disconnected, serial)
    is DevicesSideEffect.DisconnectFailed -> getString(Res.string.message_disconnect_failed, serial)
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
