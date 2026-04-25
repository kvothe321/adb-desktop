package com.tlpcraft.adbdesktop.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview

@Composable
fun FilesScreen() {
    FilesContent()
}

@Composable
fun FilesContent() {
    Text("Files Screen")
}

@UiKitPreview
@Composable
private fun FilesScreenPreview() = PreviewContext {
    FilesContent()
}
