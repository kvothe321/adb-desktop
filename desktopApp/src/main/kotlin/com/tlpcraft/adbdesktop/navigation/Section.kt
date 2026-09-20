package com.tlpcraft.adbdesktop.navigation

import androidx.navigation3.runtime.NavKey
import com.tlpcraft.adbdesktop.resources.Res
import com.tlpcraft.adbdesktop.resources.section_apps
import com.tlpcraft.adbdesktop.resources.section_devices
import com.tlpcraft.adbdesktop.resources.section_files
import com.tlpcraft.adbdesktop.resources.section_logcat
import com.tlpcraft.adbdesktop.resources.section_screen
import com.tlpcraft.adbdesktop.resources.section_settings
import com.tlpcraft.adbdesktop.resources.section_shell
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import org.jetbrains.compose.resources.StringResource

/** Top-level destinations, in sidebar order. Each is reached with Ctrl (Cmd on macOS) plus [shortcutDigit]. */
enum class Section(val route: NavKey, val label: StringResource, val icon: IconName) {
    Devices(DevicesRoute, Res.string.section_devices, IconName.Devices),
    Apps(AppsRoute, Res.string.section_apps, IconName.Package),
    Files(FilesRoute, Res.string.section_files, IconName.Folder),
    Logcat(LogcatRoute, Res.string.section_logcat, IconName.List),
    Shell(ShellRoute, Res.string.section_shell, IconName.Terminal),
    Screen(ScreenRoute, Res.string.section_screen, IconName.Camera),
    Settings(SettingsRoute, Res.string.section_settings, IconName.Settings),
    ;

    val shortcutDigit: Int
        get() = ordinal + 1

    companion object {
        fun of(route: NavKey?): Section = entries.firstOrNull { it.route == route } ?: Devices

        fun forShortcut(digit: Int): Section? = entries.getOrNull(digit - 1)
    }
}
