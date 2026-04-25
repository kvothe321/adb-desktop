package com.tlpcraft.adbdesktop.presentation
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    viewModel: DevicesViewModel = koinViewModel()
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
            val deviceModels = devices.map { device ->
                DeviceUiModel(
                    serial = device.serial,
                    status = device.status,
                    cpuInfo = if (device.status == "device") cpuBySerial[device.serial] else null
                )
            }
            DevicesContentView(
                devices = deviceModels,
                selectedSerial = selectedSerial,
                onSelectDevice = onSelectDevice,
                onDeselectDevice = onDeselectDevice
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
        horizontalAlignment = Alignment.CenterHorizontally
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ADB error",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Content ───────────────────────────────────────────────────────────────────
@Composable
private fun DevicesContentView(
    devices: List<DeviceUiModel>,
    selectedSerial: String?,
    onSelectDevice: (String) -> Unit,
    onDeselectDevice: () -> Unit
) {
    if (devices.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No devices connected.", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Plug in a device or start an emulator.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(devices, key = { it.serial }) { device ->
            val isSelected = device.serial == selectedSerial
            DeviceCard(
                device = device,
                isSelected = isSelected,
                onSelect = { if (isSelected) onDeselectDevice() else onSelectDevice(device.serial) }
            )
        }
    }
}

// ── Device card ───────────────────────────────────────────────────────────────
@Composable
private fun DeviceCard(device: DeviceUiModel, isSelected: Boolean, onSelect: () -> Unit) {
    val borderMod = if (isSelected) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    } else {
        Modifier
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderMod)
            .clickable(enabled = device.status == "device", onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = device.serial, style = MaterialTheme.typography.titleSmall)
                    if (isSelected) {
                        Text(
                            text = "SELECTED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                StatusBadge(device.status)
                if (device.status == "device") {
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onSelect) {
                        Text(
                            text = if (isSelected) "Deselect" else "Select",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            if (device.status == "device") {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                val cpu = device.cpuInfo
                if (cpu == null) {
                    Text(
                        text = "Collecting CPU stats…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    CpuStatsRow(cpu)
                }
            }
        }
    }
}

// ── CPU stats row ─────────────────────────────────────────────────────────────
@Composable
private fun CpuStatsRow(cpu: CpuInfo) {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
        CpuStatItem(label = "CPU", value = "%.1f%%".format(cpu.usagePercent))
        CpuStatItem(label = "Cores", value = cpu.coreCount.toString())
        CpuStatItem(
            label = "Max freq",
            value = if (cpu.maxFrequencyMHz >= 1_000) {
                "%.2f GHz".format(cpu.maxFrequencyMHz / 1_000.0)
            } else {
                "${cpu.maxFrequencyMHz} MHz"
            }
        )
    }
}

@Composable
private fun CpuStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status) {
        "device" -> MaterialTheme.colorScheme.primary
        "offline" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(text = status, style = MaterialTheme.typography.labelSmall, color = color)
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
        onDeselectDevice = {}
    )
}

@UiKitPreview
@Composable
private fun DevicesScreenContentPreview() = PreviewContext {
    DevicesContentView(
        devices = listOf(
            DeviceUiModel("emulator-5554", "device", CpuInfo(23.4f, 8, 2400)),
            DeviceUiModel("R5CT10ABCDE", "device", null),
            DeviceUiModel("192.168.1.42:5555", "offline", null)
        ),
        selectedSerial = "emulator-5554",
        onSelectDevice = {},
        onDeselectDevice = {}
    )
}
