package com.tlpcraft.adbdesktop.feature.devices.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.core.domain.model.ConnectionType
import com.tlpcraft.adbdesktop.core.domain.model.DeviceState
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceItem
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceStats
import com.tlpcraft.adbdesktop.feature.devices.resources.Res
import com.tlpcraft.adbdesktop.feature.devices.resources.device_android_version
import com.tlpcraft.adbdesktop.feature.devices.resources.device_battery
import com.tlpcraft.adbdesktop.feature.devices.resources.device_cpu
import com.tlpcraft.adbdesktop.feature.devices.resources.device_disconnect
import com.tlpcraft.adbdesktop.feature.devices.resources.device_unauthorized_hint
import com.tlpcraft.adbdesktop.uikit.component.Badge
import com.tlpcraft.adbdesktop.uikit.component.Button
import com.tlpcraft.adbdesktop.uikit.component.ButtonVariant
import com.tlpcraft.adbdesktop.uikit.component.Card
import com.tlpcraft.adbdesktop.uikit.component.ControlSize
import com.tlpcraft.adbdesktop.uikit.component.MonospaceText
import com.tlpcraft.adbdesktop.uikit.component.ProgressIndicator
import com.tlpcraft.adbdesktop.uikit.component.ProgressStyle
import com.tlpcraft.adbdesktop.uikit.component.Text
import com.tlpcraft.adbdesktop.uikit.component.TextEmphasis
import com.tlpcraft.adbdesktop.uikit.component.TextVariant
import com.tlpcraft.adbdesktop.uikit.component.Tone
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.jetbrains.compose.resources.stringResource

private const val PERCENT = 100f

@Composable
internal fun DeviceCard(
    device: DeviceItem,
    stats: DeviceStats?,
    selected: Boolean,
    onClick: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier, selected = selected, onClick = onClick.takeIf { device.isOnline }) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            DeviceHeader(device)
            when {
                device.isOnline && stats != null -> DeviceStatsRows(stats)
                device.state == DeviceState.UNAUTHORIZED -> Text(stringResource(Res.string.device_unauthorized_hint), emphasis = TextEmphasis.Medium)
            }
            if (device.canDisconnect) {
                Button(
                    text = stringResource(Res.string.device_disconnect),
                    onClick = onDisconnect,
                    variant = ButtonVariant.Ghost,
                    size = ControlSize.Small,
                )
            }
        }
    }
}

@Composable
private fun DeviceHeader(device: DeviceItem) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
        Column(Modifier.weight(1f)) {
            Text(device.name ?: device.serial, variant = TextVariant.Heading, maxLines = 1)
            MonospaceText(device.serial, emphasis = TextEmphasis.Medium, maxLines = 1, selectable = true)
        }
        Badge(stringResource(device.connectionType.label))
        Badge(stringResource(device.state.label), tone = device.state.tone)
    }
}

@Composable
private fun DeviceStatsRows(stats: DeviceStats) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        stats.androidVersion?.let { Text(stringResource(Res.string.device_android_version, it), emphasis = TextEmphasis.Medium) }
        stats.batteryLevel?.let { level ->
            MeterRow(label = stringResource(Res.string.device_battery), fraction = level / PERCENT, value = "$level%", tone = batteryTone(level))
        }
        stats.cpuUsagePercent?.let { usage ->
            MeterRow(label = stringResource(Res.string.device_cpu), fraction = usage / PERCENT, value = "${usage.toInt()}%", tone = cpuTone(usage))
        }
    }
}

@Composable
private fun MeterRow(
    label: String,
    fraction: Float,
    value: String,
    tone: Tone,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        Text(label, modifier = Modifier.width(56.dp), variant = TextVariant.Caption, emphasis = TextEmphasis.Medium)
        ProgressIndicator(Modifier.weight(1f), progress = fraction, style = ProgressStyle.Linear, tone = tone)
        Text(value, modifier = Modifier.width(36.dp), variant = TextVariant.Caption)
    }
}

@UiKitPreview
@Composable
private fun DeviceCardOnlinePreview() = PreviewContext {
    DeviceCard(
        device = DeviceItem("R5CT10ABCDE", "SM-A525F", DeviceState.ONLINE, ConnectionType.USB),
        stats = DeviceStats(androidVersion = "14", batteryLevel = 87, cpuUsagePercent = 23f),
        selected = true,
        onClick = {},
        onDisconnect = {},
    )
}

@UiKitPreview
@Composable
private fun DeviceCardWirelessPreview() = PreviewContext {
    DeviceCard(
        device = DeviceItem("192.168.1.42:5555", "Pixel 8", DeviceState.ONLINE, ConnectionType.WIRELESS),
        stats = DeviceStats(androidVersion = "15", batteryLevel = 15, cpuUsagePercent = 82f),
        selected = false,
        onClick = {},
        onDisconnect = {},
    )
}

@UiKitPreview
@Composable
private fun DeviceCardUnauthorizedPreview() = PreviewContext {
    DeviceCard(
        device = DeviceItem("0123456789ABCDEF", null, DeviceState.UNAUTHORIZED, ConnectionType.USB),
        stats = null,
        selected = false,
        onClick = {},
        onDisconnect = {},
    )
}
