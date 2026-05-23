package com.tlpcraft.adbdesktop.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.core.domain.model.ConnectionType
import com.tlpcraft.adbdesktop.core.domain.model.Device
import com.tlpcraft.adbdesktop.core.domain.model.DeviceState
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme
import com.tlpcraft.adbdesktop.uikit.theme.dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDropdown(
    devices: List<Device>,
    selectedDevice: Device?,
    onDeviceSelected: (Device) -> Unit,
    onDeviceDeselected: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(dimensions.radius.md)
    val label = selectedDevice?.serial ?: "No device selected"
    val hasDevice = selectedDevice != null

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.widthIn(min = 220.dp, max = 360.dp),
    ) {
        // ── Trigger ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .clip(shape)
                .background(
                    if (hasDevice) {
                        colorScheme.primaryContainer.copy(alpha = 0.15f)
                    } else {
                        colorScheme.onSurface.copy(alpha = 0.06f)
                    },
                )
                .clickable {}
                .padding(horizontal = dimensions.spacing.md, vertical = dimensions.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacing.sm),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasDevice) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (hasDevice) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp),
            )
        }

        // ── Menu ──────────────────────────────────────────────────────
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(dimensions.radius.md),
            containerColor = colorScheme.surface,
        ) {
            DeviceMenuItems(
                devices = devices,
                selectedDevice = selectedDevice,
                onDeviceSelected = onDeviceSelected,
                onDeviceDeselected = onDeviceDeselected,
                onDismiss = { expanded = false },
            )
        }
    }
}

@Composable
private fun DeviceMenuItems(
    devices: List<Device>,
    selectedDevice: Device?,
    onDeviceSelected: (Device) -> Unit,
    onDeviceDeselected: () -> Unit,
    onDismiss: () -> Unit,
) {
    val hasDevice = selectedDevice != null
    if (devices.isEmpty()) {
        DropdownMenuItem(
            text = {
                Text(
                    text = "No devices available",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurface.copy(alpha = 0.4f),
                )
            },
            onClick = { onDismiss() },
            colors = MenuDefaults.itemColors(textColor = colorScheme.onSurface),
        )
    } else {
        if (hasDevice) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Deselect",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                },
                onClick = {
                    onDeviceDeselected()
                    onDismiss()
                },
            )
            HorizontalDivider(
                color = colorScheme.onSurface.copy(alpha = 0.08f),
                modifier = Modifier.padding(horizontal = dimensions.spacing.sm),
            )
        }
        devices.forEach { device ->
            DeviceMenuItem(
                device = device,
                isSelected = device.serial == selectedDevice?.serial,
                onClick = {
                    onDeviceSelected(device)
                    onDismiss()
                },
            )
        }
    }
}

@Composable
private fun DeviceMenuItem(device: Device, isSelected: Boolean, onClick: () -> Unit) {
    val isOnline = device.isOnline
    DropdownMenuItem(
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = device.serial,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = device.state.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOnline) {
                        colorScheme.primary.copy(alpha = 0.7f)
                    } else {
                        colorScheme.error
                    },
                )
            }
        },
        onClick = onClick,
        modifier = if (isSelected) {
            Modifier
                .padding(horizontal = dimensions.spacing.xs)
                .clip(RoundedCornerShape(dimensions.radius.sm))
                .background(colorScheme.primaryContainer.copy(alpha = 0.15f))
        } else {
            Modifier
        },
    )
}

@UiKitPreview
@Composable
private fun DeviceDropdownPreview() {
    val devices = listOf(
        Device("emulator-5554", DeviceState.ONLINE, ConnectionType.USB),
        Device("emulator-5556", DeviceState.OFFLINE, ConnectionType.USB),
        Device("emulator-5558", DeviceState.ONLINE, ConnectionType.USB),
    )
    Column {
        DeviceDropdown(
            devices = devices,
            selectedDevice = null,
            onDeviceSelected = {},
            onDeviceDeselected = {},
        )
        DeviceDropdown(
            devices = devices,
            selectedDevice = devices.firstOrNull { it.isOnline },
            onDeviceSelected = {},
            onDeviceDeselected = {},
        )
    }
}
