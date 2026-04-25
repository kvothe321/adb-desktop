package com.tlpcraft.adbdesktop.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        is DevicesUiState.Loading -> Text("Loading devices...")
        is DevicesUiState.Error -> Text("Error: ${uiState.message}")
        is DevicesUiState.Content -> {
            if (uiState.devices.isEmpty()) {
                Text("No devices found.")
            } else {
                uiState.devices.forEach { device ->
                    Text("${device.serial} (${device.status})")
                }
            }
        }
    }
}

@UiKitPreview
@Composable
private fun DevicesScreenPreview() = PreviewContext {
    DevicesScreenContent(DevicesUiState.Content(emptyList()))
}
