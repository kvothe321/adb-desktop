package com.tlpcraft.adbdesktop

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.tlpcraft.adbdesktop.di.appModules
import com.tlpcraft.adbdesktop.feature.apps.AppsScreen
import com.tlpcraft.adbdesktop.feature.devices.DevicesScreen
import com.tlpcraft.adbdesktop.feature.devices.contract.QuickAction
import com.tlpcraft.adbdesktop.feature.files.FilesScreen
import com.tlpcraft.adbdesktop.feature.logcat.LogcatScreen
import com.tlpcraft.adbdesktop.feature.screen.MirrorPanel
import com.tlpcraft.adbdesktop.feature.screen.ScreenScreen
import com.tlpcraft.adbdesktop.feature.settings.SettingsScreen
import com.tlpcraft.adbdesktop.feature.shell.ShellScreen
import com.tlpcraft.adbdesktop.navigation.AppsRoute
import com.tlpcraft.adbdesktop.navigation.DevicesRoute
import com.tlpcraft.adbdesktop.navigation.FilesRoute
import com.tlpcraft.adbdesktop.navigation.LogcatRoute
import com.tlpcraft.adbdesktop.navigation.ScreenRoute
import com.tlpcraft.adbdesktop.navigation.Section
import com.tlpcraft.adbdesktop.navigation.SettingsRoute
import com.tlpcraft.adbdesktop.navigation.ShellRoute
import com.tlpcraft.adbdesktop.navigation.show
import com.tlpcraft.adbdesktop.shell.AppShellScreen
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
fun App(backStack: NavBackStack<NavKey>, isPaletteOpen: Boolean, onPaletteOpenChange: (Boolean) -> Unit) {
    KoinApplication(configuration = koinConfiguration { modules(appModules) }) {
        AppShellScreen(
            currentSection = Section.of(backStack.lastOrNull()),
            onSectionSelect = backStack::show,
            isPaletteOpen = isPaletteOpen,
            onPaletteOpenChange = onPaletteOpenChange,
            phone = { onShowMessage -> MirrorPanel(onShowMessage = onShowMessage) },
        ) { onShowMessage ->
            NavDisplay(
                backStack = backStack,
                entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                entryProvider = { route ->
                    NavEntry(route) {
                        when (route) {
                            DevicesRoute -> DevicesScreen(onShowMessage = onShowMessage, onSectionRequest = { backStack.show(it.section) })
                            AppsRoute -> AppsScreen(onShowMessage = onShowMessage)
                            FilesRoute -> FilesScreen(onShowMessage = onShowMessage)
                            LogcatRoute -> LogcatScreen(onShowMessage = onShowMessage)
                            ShellRoute -> ShellScreen()
                            ScreenRoute -> ScreenScreen(onShowMessage = onShowMessage)
                            SettingsRoute -> SettingsScreen()
                            else -> error("Unknown route: $route")
                        }
                    }
                },
            )
        }
    }
}

private val QuickAction.section: Section
    get() = when (this) {
        QuickAction.Apps, QuickAction.InstallApk -> Section.Apps
        QuickAction.Files -> Section.Files
        QuickAction.Logcat -> Section.Logcat
        QuickAction.Shell -> Section.Shell
        QuickAction.Screenshot -> Section.Screen
    }
