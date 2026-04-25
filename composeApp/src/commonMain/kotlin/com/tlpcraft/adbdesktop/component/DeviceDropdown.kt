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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme
import com.tlpcraft.adbdesktop.uikit.theme.dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDropdown(
    devices: List<AdbDevice>,
    selectedDevice: AdbDevice?,
    onDeviceSelected: (AdbDevice) -> Unit,
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
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
            if (devices.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "No devices available",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    },
                    onClick = { expanded = false },
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
                            expanded = false
                        },
                    )
                    HorizontalDivider(
                        color = colorScheme.onSurface.copy(alpha = 0.08f),
                        modifier = Modifier.padding(horizontal = dimensions.spacing.sm),
                    )
                }
                devices.forEach { device ->
                    val isSelected = device.serial == selectedDevice?.serial
                    val isOnline = device.status == "device"
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
                                    text = device.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOnline) {
                                        colorScheme.primary.copy(alpha = 0.7f)
                                    } else {
                                        colorScheme.error
                                    },
                                )
                            }
                        },
                        onClick = {
                            onDeviceSelected(device)
                            expanded = false
                        },
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
            }
        }
    }
}

@UiKitPreview
@Composable
private fun DeviceDropdownPreview() {
    val devices = listOf(
        AdbDevice(serial = "emulator-5554", status = "device"),
        AdbDevice(serial = "emulator-5556", status = "offline"),
        AdbDevice(serial = "emulator-5558", status = "device"),
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
            selectedDevice = devices.firstOrNull { it.status == "device" },
            onDeviceSelected = {},
            onDeviceDeselected = {},
        )
    }
}
