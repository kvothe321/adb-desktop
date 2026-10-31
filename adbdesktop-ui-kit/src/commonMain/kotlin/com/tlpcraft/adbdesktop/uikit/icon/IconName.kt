package com.tlpcraft.adbdesktop.uikit.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

/** Every glyph the design system offers, named after what it depicts. */
enum class IconName {
    Add,
    Check,
    ChevronDown,
    Close,
    Delete,
    Devices,
    Folder,
    Info,
    Lock,
    Moon,
    Package,
    Play,
    Refresh,
    Search,
    Settings,
    Sun,
    Terminal,
    Warning,
    Wifi,
    ;

    internal val vector: ImageVector
        get() = when (this) {
            Add -> Icons.Filled.Add
            Check -> Icons.Filled.Check
            ChevronDown -> Icons.Filled.KeyboardArrowDown
            Close -> Icons.Filled.Close
            Delete -> Icons.Filled.Delete
            Devices -> DevicesVector
            Folder -> FolderVector
            Info -> Icons.Filled.Info
            Lock -> Icons.Filled.Lock
            Moon -> MoonVector
            Package -> PackageVector
            Play -> Icons.Filled.PlayArrow
            Refresh -> Icons.Filled.Refresh
            Search -> Icons.Filled.Search
            Settings -> Icons.Filled.Settings
            Sun -> SunVector
            Terminal -> TerminalVector
            Warning -> Icons.Filled.Warning
            Wifi -> WifiVector
        }
}
