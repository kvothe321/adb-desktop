package com.tlpcraft.adbdesktop.uikit.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
    Camera,
    Check,
    ChevronDown,
    ChevronUp,
    Close,
    Delete,
    Devices,
    Download,
    File,
    Folder,
    Info,
    List,
    Lock,
    Moon,
    Package,
    Pause,
    Play,
    Refresh,
    Search,
    Settings,
    Stop,
    Sun,
    Terminal,
    Upload,
    Warning,
    Wifi,
    ;

    internal val vector: ImageVector
        get() = when (this) {
            Add -> Icons.Filled.Add
            Camera -> CameraVector
            Check -> Icons.Filled.Check
            ChevronDown -> Icons.Filled.KeyboardArrowDown
            ChevronUp -> Icons.Filled.KeyboardArrowUp
            Close -> Icons.Filled.Close
            Delete -> Icons.Filled.Delete
            Devices -> DevicesVector
            Download -> DownloadVector
            File -> FileVector
            Folder -> FolderVector
            Info -> Icons.Filled.Info
            List -> Icons.AutoMirrored.Filled.List
            Lock -> Icons.Filled.Lock
            Moon -> MoonVector
            Package -> PackageVector
            Pause -> PauseVector
            Play -> Icons.Filled.PlayArrow
            Refresh -> Icons.Filled.Refresh
            Search -> Icons.Filled.Search
            Settings -> Icons.Filled.Settings
            Stop -> StopVector
            Sun -> SunVector
            Terminal -> TerminalVector
            Upload -> UploadVector
            Warning -> Icons.Filled.Warning
            Wifi -> WifiVector
        }
}
