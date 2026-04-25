package com.tlpcraft.adbdesktop.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview

@Composable
fun AppsScreen() {
    AppsScreenContent()
}

@Composable
fun AppsScreenContent() {
    Text("Apps")
}

@UiKitPreview
@Composable
private fun AppsScreenPreview() = PreviewContext {
    AppsScreenContent()
}
