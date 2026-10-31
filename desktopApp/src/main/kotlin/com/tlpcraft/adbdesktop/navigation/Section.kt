package com.tlpcraft.adbdesktop.navigation

import androidx.navigation3.runtime.NavKey
import com.tlpcraft.adbdesktop.resources.Res
import com.tlpcraft.adbdesktop.resources.section_apps
import com.tlpcraft.adbdesktop.resources.section_devices
import com.tlpcraft.adbdesktop.resources.section_files
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import org.jetbrains.compose.resources.StringResource

/** Top-level destinations, in sidebar order. [shortcutDigit] pairs with Ctrl (Cmd on macOS). */
enum class Section(val route: NavKey, val label: StringResource, val icon: IconName, val shortcutDigit: Int) {
    Devices(DevicesRoute, Res.string.section_devices, IconName.Devices, shortcutDigit = 1),
    Apps(AppsRoute, Res.string.section_apps, IconName.Package, shortcutDigit = 2),
    Files(FilesRoute, Res.string.section_files, IconName.Folder, shortcutDigit = 3),
    ;

    companion object {
        fun of(route: NavKey?): Section = entries.firstOrNull { it.route == route } ?: Devices

        fun forShortcut(digit: Int): Section? = entries.firstOrNull { it.shortcutDigit == digit }
    }
}
