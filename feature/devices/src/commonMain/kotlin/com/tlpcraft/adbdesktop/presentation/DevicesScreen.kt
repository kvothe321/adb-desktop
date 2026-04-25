package com.tlpcraft.adbdesktop.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlpcraft.adbdesktop.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DevicesScreen(viewModel: DevicesViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DevicesScreenContent(uiState)
}

@Composable
fun DevicesScreenContent(uiState: DevicesUiState) {
    when (uiState) {
        is DevicesUiState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Scanning for devices…", style = MaterialTheme.typography.bodyMedium)
            }
        }

        is DevicesUiState.Error -> {
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
                    text = uiState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        is DevicesUiState.Content -> {
            if (uiState.devices.isEmpty()) {
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
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.devices, key = { it.serial }) { device ->
                        DeviceCard(device)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(device: DeviceUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Header ─────────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = device.serial,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.width(8.dp))
                StatusBadge(device.status)
            }

            // ── CPU stats ──────────────────────────────────────────────────────
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

@Composable
private fun CpuStatsRow(cpu: CpuInfo) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status) {
        "device" -> MaterialTheme.colorScheme.primary
        "offline" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = status,
        style = MaterialTheme.typography.labelSmall,
        color = color
    )
}

// ── Previews ─────────────────────────────────────────────────────────────────

@UiKitPreview
@Composable
private fun DevicesScreenPreview() = PreviewContext {
    DevicesScreenContent(DevicesUiState.Content(emptyList()))
}

@UiKitPreview
@Composable
private fun DevicesScreenWithDevicesPreview() = PreviewContext {
    DevicesScreenContent(
        DevicesUiState.Content(
            listOf(
                DeviceUiModel(
                    serial = "emulator-5554",
                    status = "device",
                    cpuInfo = CpuInfo(usagePercent = 23.4f, coreCount = 8, maxFrequencyMHz = 2400)
                ),
                DeviceUiModel(
                    serial = "R5CT10ABCDE",
                    status = "device",
                    cpuInfo = null
                ),
                DeviceUiModel(
                    serial = "192.168.1.42:5555",
                    status = "offline",
                    cpuInfo = null
                )
            )
        )
    )
}
