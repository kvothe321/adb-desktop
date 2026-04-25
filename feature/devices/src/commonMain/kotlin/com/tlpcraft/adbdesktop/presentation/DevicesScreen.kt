package com.tlpcraft.adbdesktop.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.uikit.components.card.DeviceCard
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Devices screen entry point.
 *
 * @param devicesOutcome Latest result from the device-observation flow.
 *   `null` means the first poll hasn't completed yet (loading state).
 * @param selectedSerial Serial of the device currently selected by the user, or `null`.
 * @param onSelectDevice Called with a serial when the user picks a device.
 * @param onDeselectDevice Called when the user removes the current selection.
 */
@Composable
fun DevicesScreen(
    devicesOutcome: Outcome<List<AdbDevice>, DeviceError>?,
    selectedSerial: String?,
    onSelectDevice: (String) -> Unit,
    onDeselectDevice: () -> Unit,
    viewModel: DevicesViewModel = koinViewModel(),
) {
    when {
        devicesOutcome == null -> {
            DevicesLoadingView()
        }

        devicesOutcome is Outcome.Failure -> {
            DevicesErrorView(devicesOutcome.error.message)
        }

        devicesOutcome is Outcome.Success -> {
            val devices = devicesOutcome.data
            LaunchedEffect(devices) { viewModel.updateOnlineDevices(devices) }
            val cpuBySerial by viewModel.cpuBySerial.collectAsState()
            val deviceInfoBySerial by viewModel.deviceInfoBySerial.collectAsState()
            val deviceModels = devices.map { device ->
                val info = if (device.status == "device") deviceInfoBySerial[device.serial] else null
                DeviceUiModel(
                    serial = device.serial,
                    status = device.status,
                    cpuInfo = if (device.status == "device") cpuBySerial[device.serial] else null,
                    modelName = info?.name,
                    androidVersion = info?.androidVersion,
                    batteryLevel = info?.batteryLevel,
                )
            }
            DevicesContentView(
                devices = deviceModels,
                selectedSerial = selectedSerial,
                onSelectDevice = onSelectDevice,
                onDeselectDevice = onDeselectDevice,
            )
        }
    }
}

// ── Loading ───────────────────────────────────────────────────────────────────
@Composable
private fun DevicesLoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(12.dp))
        Text("Scanning for devices…", style = MaterialTheme.typography.bodyMedium)
    }
}

// ── Error ─────────────────────────────────────────────────────────────────────
@Composable
private fun DevicesErrorView(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "ADB error",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Content ───────────────────────────────────────────────────────────────────
@Composable
private fun DevicesContentView(
    devices: List<DeviceUiModel>,
    selectedSerial: String?,
    onSelectDevice: (String) -> Unit,
    onDeselectDevice: () -> Unit,
) {
    if (devices.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("No devices connected.", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Plug in a device or start an emulator.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(devices, key = { it.serial }) { device ->
            val isOnline = device.status == "device"
            val isSelected = device.serial == selectedSerial
            val statusText = when (device.status) {
                "device" -> "Online"
                "offline" -> "Offline"
                "unauthorized" -> "Unauthorized"
                else -> device.status
            }
            DeviceCard(
                deviceName = device.modelName ?: device.serial,
                androidVersion = device.androidVersion,
                batteryLevel = device.batteryLevel,
                cpuInfo = device.cpuInfo,
                isOnline = isOnline,
                statusText = statusText,
                isSelected = isSelected,
                enabled = isOnline,
                onClick = { if (isSelected) onDeselectDevice() else onSelectDevice(device.serial) },
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────
@UiKitPreview
@Composable
private fun DevicesScreenLoadingPreview() = PreviewContext {
    DevicesScreen(devicesOutcome = null, selectedSerial = null, onSelectDevice = {}, onDeselectDevice = {})
}

@UiKitPreview
@Composable
private fun DevicesScreenEmptyPreview() = PreviewContext {
    DevicesScreen(
        devicesOutcome = Outcome.Success(emptyList()),
        selectedSerial = null,
        onSelectDevice = {},
        onDeselectDevice = {},
    )
}

@UiKitPreview
@Composable
private fun DevicesScreenContentPreview() = PreviewContext {
    DevicesContentView(
        devices = listOf(
            DeviceUiModel("emulator-5554", "device", CpuInfo(23.4f, 8, 2400), "Android 14", batteryLevel = 87),
            DeviceUiModel("R5CT10ABCDE", "device", null, "Android 13", androidVersion = "14"),
            DeviceUiModel("192.168.1.42:5555", "offline", null),
        ),
        selectedSerial = "emulator-5554",
        onSelectDevice = {},
        onDeselectDevice = {},
    )
}
