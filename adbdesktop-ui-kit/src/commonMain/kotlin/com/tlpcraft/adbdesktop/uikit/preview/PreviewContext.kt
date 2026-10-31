package com.tlpcraft.adbdesktop.uikit.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/**
 * Wraps preview content in [AppTheme] on the app background. The theme follows the preview's
 * night mode, which the IDE maps onto `isSystemInDarkTheme()`.
 */
@Composable
fun PreviewContext(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    AppTheme(darkTheme = isSystemInDarkTheme()) {
        Box(modifier.background(AppTheme.colors.background).padding(AppTheme.spacing.md)) {
            content()
        }
    }
}
