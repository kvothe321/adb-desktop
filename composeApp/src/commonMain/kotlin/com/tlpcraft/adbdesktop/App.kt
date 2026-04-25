package com.tlpcraft.adbdesktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tlpcraft.adbdesktop.di.appModules
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.koin.compose.KoinApplication

@Composable
fun App() {
    var isDark by remember { mutableStateOf(true) }
    KoinApplication(application = { modules(appModules) }) {
        AppTheme(darkTheme = isDark) {
            AppShell(isDark = isDark, onToggleTheme = { isDark = !isDark })
        }
    }
}

@UiKitPreview
@Composable
fun AppPreview() = PreviewContext {
    AppTheme {
        App()
    }
}
