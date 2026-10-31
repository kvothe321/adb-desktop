package com.tlpcraft.adbdesktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.tlpcraft.adbdesktop.di.appModules
import com.tlpcraft.adbdesktop.feature.apps.AppsScreen
import com.tlpcraft.adbdesktop.feature.devices.DevicesScreen
import com.tlpcraft.adbdesktop.feature.files.FilesScreen
import com.tlpcraft.adbdesktop.navigation.AppsRoute
import com.tlpcraft.adbdesktop.navigation.DevicesRoute
import com.tlpcraft.adbdesktop.navigation.FilesRoute
import com.tlpcraft.adbdesktop.navigation.Section
import com.tlpcraft.adbdesktop.navigation.show
import com.tlpcraft.adbdesktop.shell.AppShellScreen
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

/** Follows the system theme until the user picks one with the sidebar toggle. */
@Composable
fun App(backStack: NavBackStack<NavKey>) {
    KoinApplication(configuration = koinConfiguration { modules(appModules) }) {
        val systemDarkTheme = isSystemInDarkTheme()
        var chosenDarkTheme by rememberSaveable { mutableStateOf<Boolean?>(null) }
        val isDarkTheme = chosenDarkTheme ?: systemDarkTheme

        AppTheme(darkTheme = isDarkTheme) {
            AppShellScreen(
                currentSection = Section.of(backStack.lastOrNull()),
                onSectionSelect = backStack::show,
                isDarkTheme = isDarkTheme,
                onThemeToggle = { chosenDarkTheme = !isDarkTheme },
            ) { onShowMessage ->
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider = { route ->
                        NavEntry(route) {
                            when (route) {
                                DevicesRoute -> DevicesScreen(onShowMessage = onShowMessage)
                                AppsRoute -> AppsScreen(onShowMessage = onShowMessage)
                                FilesRoute -> FilesScreen()
                                else -> error("Unknown route: $route")
                            }
                        }
                    },
                )
            }
        }
    }
}
