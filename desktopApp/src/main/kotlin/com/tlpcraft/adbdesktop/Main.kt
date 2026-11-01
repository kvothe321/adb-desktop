package com.tlpcraft.adbdesktop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.navigation3.runtime.rememberNavBackStack
import com.tlpcraft.adbdesktop.navigation.Section
import com.tlpcraft.adbdesktop.navigation.isPaletteShortcut
import com.tlpcraft.adbdesktop.navigation.navigationConfig
import com.tlpcraft.adbdesktop.navigation.sectionShortcut
import com.tlpcraft.adbdesktop.navigation.show
import com.tlpcraft.adbdesktop.resources.Res
import com.tlpcraft.adbdesktop.resources.app_logo
import com.tlpcraft.adbdesktop.resources.app_name
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

fun main() = application {
    val backStack = rememberNavBackStack(navigationConfig, Section.Devices.route)
    var isPaletteOpen by remember { mutableStateOf(false) }

    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(size = DpSize(1280.dp, 820.dp)),
        title = stringResource(Res.string.app_name),
        icon = painterResource(Res.drawable.app_logo),
        onPreviewKeyEvent = { event ->
            val section = event.sectionShortcut()
            section?.let(backStack::show)
            if (event.isPaletteShortcut()) isPaletteOpen = true
            section != null || event.isPaletteShortcut()
        },
    ) {
        App(backStack = backStack, isPaletteOpen = isPaletteOpen, onPaletteOpenChange = { isPaletteOpen = it })
    }
}
