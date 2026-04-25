package com.tlpcraft.adbdesktop.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview

@Composable
fun DevicesScreen() {
    DevicesScreenContent()
}

@Composable
fun DevicesScreenContent() {
    Text("Devices")
}

@UiKitPreview
@Composable
private fun DevicesScreenPreview() = PreviewContext {
    DevicesScreenContent()
}
