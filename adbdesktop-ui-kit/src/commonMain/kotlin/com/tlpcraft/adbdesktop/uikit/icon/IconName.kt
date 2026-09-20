package com.tlpcraft.adbdesktop.uikit.icon

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
    Mirror,
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
            Add -> AddVector
            Camera -> CameraVector
            Check -> CheckVector
            ChevronDown -> ChevronDownVector
            ChevronUp -> ChevronUpVector
            Close -> CloseVector
            Delete -> DeleteVector
            Devices -> DevicesVector
            Download -> DownloadVector
            File -> FileVector
            Folder -> FolderVector
            Info -> InfoVector
            List -> ListVector
            Lock -> LockVector
            Mirror -> MirrorVector
            Moon -> MoonVector
            Package -> PackageVector
            Pause -> PauseVector
            Play -> PlayVector
            Refresh -> RefreshVector
            Search -> SearchVector
            Settings -> SettingsVector
            Stop -> StopVector
            Sun -> SunVector
            Terminal -> TerminalVector
            Upload -> UploadVector
            Warning -> WarningVector
            Wifi -> WifiVector
        }
}
