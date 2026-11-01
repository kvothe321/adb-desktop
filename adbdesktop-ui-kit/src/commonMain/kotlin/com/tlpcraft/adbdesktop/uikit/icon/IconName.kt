package com.tlpcraft.adbdesktop.uikit.icon

import androidx.compose.ui.graphics.vector.ImageVector

/** Every glyph the design system offers, named after what it depicts. */
enum class IconName {
    Add,
    Back,
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
    Home,
    Info,
    List,
    Lock,
    Mirror,
    Moon,
    Package,
    Pause,
    Pin,
    Play,
    Recents,
    Refresh,
    Rotate,
    Search,
    Settings,
    Stop,
    Sun,
    Terminal,
    Upload,
    VolumeDown,
    VolumeUp,
    Warning,
    Wifi,
    ;

    internal val vector: ImageVector
        get() = when (this) {
            Add -> AddVector
            Back -> BackVector
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
            Home -> HomeVector
            Info -> InfoVector
            List -> ListVector
            Lock -> LockVector
            Mirror -> MirrorVector
            Moon -> MoonVector
            Package -> PackageVector
            Pause -> PauseVector
            Pin -> PinVector
            Play -> PlayVector
            Recents -> RecentsVector
            Refresh -> RefreshVector
            Rotate -> RotateVector
            Search -> SearchVector
            Settings -> SettingsVector
            Stop -> StopVector
            Sun -> SunVector
            Terminal -> TerminalVector
            Upload -> UploadVector
            VolumeDown -> VolumeDownVector
            VolumeUp -> VolumeUpVector
            Warning -> WarningVector
            Wifi -> WifiVector
        }
}
