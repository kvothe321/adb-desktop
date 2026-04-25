package com.tlpcraft.adbdesktop.feature.devices

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tlpcraft.adbdesktop.uikit.theme.colorScheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DevicesScreen(viewModel: DevicesViewModel = koinViewModel()) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Connected Devices", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (devices.isEmpty()) {
            Text("No devices found.", color = colorScheme.onSurface)
        } else {
            devices.forEach { device ->
                Text("• ${device.serial} (${device.status})", color = colorScheme.onSurface)
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = { viewModel.loadDevices() }) {
            Text("Refresh", color = colorScheme.onPrimary)
        }
    }
}
