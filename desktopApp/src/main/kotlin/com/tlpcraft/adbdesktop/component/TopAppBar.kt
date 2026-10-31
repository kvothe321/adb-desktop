package com.tlpcraft.adbdesktop.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tlpcraft.adbdesktop.core.domain.model.ConnectionType
import com.tlpcraft.adbdesktop.core.domain.model.Device
import com.tlpcraft.adbdesktop.core.domain.model.DeviceState
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme
import com.tlpcraft.adbdesktop.uikit.theme.dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    devices: List<Device>,
    selectedDevice: Device?,
    onDeviceSelected: (Device) -> Unit,
    onDeviceDeselected: () -> Unit,
) {
    Surface(
        color = colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = dimensions.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            DeviceDropdown(
                devices = devices,
                selectedDevice = selectedDevice,
                onDeviceSelected = onDeviceSelected,
                onDeviceDeselected = onDeviceDeselected,
            )
        }
    }
}

@UiKitPreview
@Composable
fun TopAppBarPreview() = PreviewContext {
    TopAppBar(
        devices = listOf(
            Device("emulator-5554", DeviceState.ONLINE, ConnectionType.EMULATOR),
            Device("emulator-5556", DeviceState.ONLINE, ConnectionType.EMULATOR),
        ),
        selectedDevice = null,
        onDeviceSelected = {},
        onDeviceDeselected = {},
    )
}
