package com.tlpcraft.adbdesktop.uikit.preview

import androidx.compose.runtime.Composable
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

@Composable
fun PreviewContext(content: @Composable () -> Unit) {
    AppTheme {
        content()
    }
}
