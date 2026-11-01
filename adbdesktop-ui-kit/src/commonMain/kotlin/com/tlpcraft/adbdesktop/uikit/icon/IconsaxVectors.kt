package com.tlpcraft.adbdesktop.uikit.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private const val BOX = 24f
private const val STROKE_WIDTH = 1.5f
private val INK = SolidColor(Color.Black)

private class IconStroke(val data: String, val width: Float, val isRoundCapped: Boolean)

private fun stroke(data: String, width: Float = STROKE_WIDTH, isRoundCapped: Boolean = true) = IconStroke(data, width, isRoundCapped)

/** Iconsax linear glyphs drawn in black and centred in a 24 box; [com.tlpcraft.adbdesktop.uikit.component.Icon] tints them. */
private fun iconsax(
    name: String,
    vararg strokes: IconStroke,
    width: Float = BOX,
    height: Float = BOX,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = BOX.dp,
    defaultHeight = BOX.dp,
    viewportWidth = BOX,
    viewportHeight = BOX,
).apply {
    addGroup(translationX = (BOX - width) / 2, translationY = (BOX - height) / 2)
    strokes.forEach { stroke ->
        addPath(
            pathData = addPathNodes(stroke.data),
            stroke = INK,
            strokeLineWidth = stroke.width,
            strokeLineCap = if (stroke.isRoundCapped) StrokeCap.Round else StrokeCap.Butt,
            strokeLineJoin = if (stroke.isRoundCapped) StrokeJoin.Round else StrokeJoin.Miter,
        )
    }
    clearGroup()
}.build()

internal val AddVector: ImageVector by lazy {
    iconsax(
        "Add",
        stroke("M6 12H18"),
        stroke("M12 18V6"),
    )
}

internal val BackVector: ImageVector by lazy {
    iconsax(
        "Back",
        stroke("M9.57 5.92969L3.5 11.9997L9.57 18.0697"),
        stroke("M20.4999 12H3.66992"),
    )
}

internal val CameraVector: ImageVector by lazy {
    iconsax(
        "Camera",
        stroke(
            "M6.76005 22H17.24C20 22 21.1 20.31 21.23 18.25L21.75 9.99C21.89 7.83 20.17 6 18 6C17.39 6 16.83 5.65 16.55 5.11L15.83 3.66C15.37 2" +
                ".75 14.17 2 13.15 2H10.86C9.83005 2 8.63005 2.75 8.17005 3.66L7.45005 5.11C7.17005 5.65 6.61005 6 6.00005 6C3.83005 6 2.11005 7.83" +
                " 2.25005 9.99L2.77005 18.25C2.89005 20.31 4.00005 22 6.76005 22Z",
        ),
        stroke("M10.5 8H13.5"),
        stroke("M12 18C13.79 18 15.25 16.54 15.25 14.75C15.25 12.96 13.79 11.5 12 11.5C10.21 11.5 8.75 12.96 8.75 14.75C8.75 16.54 10.21 18 12 18Z"),
    )
}

internal val CategoryVector: ImageVector by lazy {
    iconsax(
        "Category",
        stroke("M5 10H7C9 10 10 9 10 7V5C10 3 9 2 7 2H5C3 2 2 3 2 5V7C2 9 3 10 5 10Z"),
        stroke("M17 10H19C21 10 22 9 22 7V5C22 3 21 2 19 2H17C15 2 14 3 14 5V7C14 9 15 10 17 10Z"),
        stroke("M17 22H19C21 22 22 21 22 19V17C22 15 21 14 19 14H17C15 14 14 15 14 17V19C14 21 15 22 17 22Z"),
        stroke("M5 22H7C9 22 10 21 10 19V17C10 15 9 14 7 14H5C3 14 2 15 2 17V19C2 21 3 22 5 22Z"),
    )
}

internal val CheckVector: ImageVector by lazy {
    iconsax(
        "Check",
        stroke("M12 22C17.5 22 22 17.5 22 12C22 6.5 17.5 2 12 2C6.5 2 2 6.5 2 12C2 17.5 6.5 22 12 22Z"),
        stroke("M7.75 11.9999L10.58 14.8299L16.25 9.16992"),
    )
}

internal val ChevronDownVector: ImageVector by lazy {
    iconsax(
        "ChevronDown",
        stroke("M19.9201 8.9502L13.4001 15.4702C12.6301 16.2402 11.3701 16.2402 10.6001 15.4702L4.08008 8.9502"),
    )
}

internal val ChevronUpVector: ImageVector by lazy {
    iconsax(
        "ChevronUp",
        stroke("M19.9201 15.0496L13.4001 8.52965C12.6301 7.75965 11.3701 7.75965 10.6001 8.52965L4.08008 15.0496"),
    )
}

internal val CloseVector: ImageVector by lazy {
    iconsax(
        "Close",
        stroke("M12 22C17.5 22 22 17.5 22 12C22 6.5 17.5 2 12 2C6.5 2 2 6.5 2 12C2 17.5 6.5 22 12 22Z"),
        stroke("M9.17004 14.8299L14.83 9.16992"),
        stroke("M14.83 14.8299L9.17004 9.16992"),
    )
}

internal val DeleteVector: ImageVector by lazy {
    iconsax(
        "Delete",
        stroke("M21 5.98047C17.67 5.65047 14.32 5.48047 10.98 5.48047C9 5.48047 7.02 5.58047 5.04 5.78047L3 5.98047"),
        stroke("M8.5 4.97L8.72 3.66C8.88 2.71 9 2 10.69 2H13.31C15 2 15.13 2.75 15.28 3.67L15.5 4.97"),
        stroke(
            "M18.85 9.14062L18.2 19.2106C18.09 20.7806 18 22.0006 15.21 22.0006H8.79002C6.00002 22.0006 5.91002 20.7806 5.80002 19.2106L5.15002" +
                " 9.14062",
        ),
        stroke("M10.33 16.5H13.66"),
        stroke("M9.5 12.5H14.5"),
    )
}

internal val DevicesVector: ImageVector by lazy {
    iconsax(
        "Devices",
        stroke("M16 5V15C16 19 15 20 11 20H5C1 20 0 19 0 15V5C0 1 1 0 5 0H11C15 0 16 1 16 5Z"),
        stroke("M10 3.5H6"),
        stroke(
            "M7.99995 17.1C8.85599 17.1 9.54995 16.406 9.54995 15.55C9.54995 14.694 8.85599 14 7.99995 14C7.14391 14 6.44995 14.694 6.44995 15." +
                "55C6.44995 16.406 7.14391 17.1 7.99995 17.1Z",
        ),
        width = 16f,
        height = 20f,
    )
}

internal val DownloadVector: ImageVector by lazy {
    iconsax(
        "Download",
        stroke(
            "M16.44 8.90039C20.04 9.21039 21.51 11.0604 21.51 15.1104V15.2404C21.51 19.7104 19.72 21.5004 15.25 21.5004H8.73998C4.26998 21.5004" +
                " 2.47998 19.7104 2.47998 15.2404V15.1104C2.47998 11.0904 3.92998 9.24039 7.46998 8.91039",
        ),
        stroke("M12 2V14.88"),
        stroke("M15.3499 12.6504L11.9999 16.0004L8.6499 12.6504"),
    )
}

internal val FileVector: ImageVector by lazy {
    iconsax(
        "File",
        stroke("M22 10V15C22 20 20 22 15 22H9C4 22 2 20 2 15V9C2 4 4 2 9 2H14"),
        stroke("M22 10H18C15 10 14 9 14 6V2L22 10Z"),
    )
}

internal val FolderVector: ImageVector by lazy {
    iconsax(
        "Folder",
        stroke(
            "M22 11V17C22 21 21 22 17 22H7C3 22 2 21 2 17V7C2 3 3 2 7 2H8.5C10 2 10.33 2.44 10.9 3.2L12.4 5.2C12.78 5.7 13 6 14 6H17C21 6 22 7 " +
                "22 11Z",
            isRoundCapped = false,
        ),
    )
}

internal val HomeVector: ImageVector by lazy {
    iconsax(
        "Home",
        stroke(
            "M9.02 2.84016L3.63 7.04016C2.73 7.74016 2 9.23016 2 10.3602V17.7702C2 20.0902 3.89 21.9902 6.21 21.9902H17.79C20.11 21.9902 22 20." +
                "0902 22 17.7802V10.5002C22 9.29016 21.19 7.74016 20.2 7.05016L14.02 2.72016C12.62 1.74016 10.37 1.79016 9.02 2.84016Z",
        ),
        stroke("M12 17.9902V14.9902"),
    )
}

internal val InfoVector: ImageVector by lazy {
    iconsax(
        "Info",
        stroke("M12 22C17.5 22 22 17.5 22 12C22 6.5 17.5 2 12 2C6.5 2 2 6.5 2 12C2 17.5 6.5 22 12 22Z"),
        stroke("M12 8V13"),
        stroke("M11.9945 16H12.0035", width = 2f),
    )
}

internal val DocumentTextVector: ImageVector by lazy {
    iconsax(
        "DocumentText",
        stroke("M21 7V17C21 20 19.5 22 16 22H8C4.5 22 3 20 3 17V7C3 4 4.5 2 8 2H16C19.5 2 21 4 21 7Z"),
        stroke("M14.5 4.5V6.5C14.5 7.6 15.4 8.5 16.5 8.5H18.5"),
        stroke("M8 13H12"),
        stroke("M8 17H16"),
    )
}

internal val LockVector: ImageVector by lazy {
    iconsax(
        "Lock",
        stroke("M6 10V8C6 4.69 7 2 12 2C17 2 18 4.69 18 8V10"),
        stroke("M17 22H7C3 22 2 21 2 17V15C2 11 3 10 7 10H17C21 10 22 11 22 15V17C22 21 21 22 17 22Z"),
        stroke("M15.9965 16H16.0054", width = 2f),
        stroke("M11.9955 16H12.0045", width = 2f),
        stroke("M7.99451 16H8.00349", width = 2f),
    )
}

internal val MirrorVector: ImageVector by lazy {
    iconsax(
        "Mirror",
        stroke("M2 9V8C2 5 4 3 7 3H17C20 3 22 5 22 8V16C22 19 20 21 17 21H16"),
        stroke("M3.68994 11.7109C8.30994 12.3009 11.6999 15.701 12.2999 20.321"),
        stroke("M2.62 15.0703C6.01 15.5003 8.5 18.0003 8.94 21.3903"),
        stroke("M1.97998 18.8613C3.66998 19.0813 4.91998 20.3213 5.13998 22.0213"),
    )
}

internal val MonitorVector: ImageVector by lazy {
    iconsax(
        "Monitor",
        stroke(
            "M6.44 2H17.55C21.11 2 22 2.89 22 6.44V12.77C22 16.33 21.11 17.21 17.56 17.21H6.44C2.89 17.22 2 16.33 2 12.78V6.44C2 2.89 2.89 2 6." +
                "44 2Z",
        ),
        stroke("M12 17.2207V22.0007"),
        stroke("M2 13H22"),
        stroke("M7.5 22H16.5"),
    )
}

internal val MoonVector: ImageVector by lazy {
    iconsax(
        "Moon",
        stroke(
            "M2.02997 12.42C2.38997 17.57 6.75997 21.76 11.99 21.99C15.68 22.15 18.98 20.43 20.96 17.72C21.78 16.61 21.34 15.87 19.97 16.12C19." +
                "3 16.24 18.61 16.29 17.89 16.26C13 16.06 8.99997 11.97 8.97997 7.13996C8.96997 5.83996 9.23997 4.60996 9.72997 3.48996C10.27 2.249" +
                "96 9.61997 1.65996 8.36997 2.18996C4.40997 3.85996 1.69997 7.84996 2.02997 12.42Z",
        ),
    )
}

internal val PackageVector: ImageVector by lazy {
    iconsax(
        "Package",
        stroke("M3.17004 7.43945L12 12.5495L20.77 7.46945"),
        stroke("M12 21.6091V12.5391"),
        stroke(
            "M9.93001 2.4793L4.59001 5.4393C3.38001 6.1093 2.39001 7.7893 2.39001 9.1693V14.8193C2.39001 16.1993 3.38001 17.8793 4.59001 18.549" +
                "3L9.93001 21.5193C11.07 22.1493 12.94 22.1493 14.08 21.5193L19.42 18.5493C20.63 17.8793 21.62 16.1993 21.62 14.8193V9.1693C21.62 7" +
                ".7893 20.63 6.1093 19.42 5.4393L14.08 2.4693C12.93 1.8393 11.07 1.8393 9.93001 2.4793Z",
        ),
    )
}

internal val PauseVector: ImageVector by lazy {
    iconsax(
        "Pause",
        stroke(
            "M10.65 19.11V4.89C10.65 3.54 10.08 3 8.64 3H5.01C3.57 3 3 3.54 3 4.89V19.11C3 20.46 3.57 21 5.01 21H8.64C10.08 21 10.65 20.46 10.6" +
                "5 19.11Z",
        ),
        stroke(
            "M21 19.11V4.89C21 3.54 20.43 3 18.99 3H15.36C13.93 3 13.35 3.54 13.35 4.89V19.11C13.35 20.46 13.92 21 15.36 21H18.99C20.43 21 21 2" +
                "0.46 21 19.11Z",
        ),
    )
}

internal val PlayVector: ImageVector by lazy {
    iconsax(
        "Play",
        stroke(
            "M4 12.0004V8.44038C4 4.02038 7.13 2.21038 10.96 4.42038L14.05 6.20038L17.14 7.98038C20.97 10.1904 20.97 13.8104 17.14 16.0204L14.0" +
                "5 17.8004L10.96 19.5804C7.13 21.7904 4 19.9804 4 15.5604V12.0004Z",
        ),
    )
}

internal val RecentsVector: ImageVector by lazy {
    iconsax(
        "Recents",
        stroke("M16 12.9V17.1C16 20.6 14.6 22 11.1 22H6.9C3.4 22 2 20.6 2 17.1V12.9C2 9.4 3.4 8 6.9 8H11.1C14.6 8 16 9.4 16 12.9Z"),
        stroke("M22 6.9V11.1C22 14.6 20.6 16 17.1 16H16V12.9C16 9.4 14.6 8 11.1 8H8V6.9C8 3.4 9.4 2 12.9 2H17.1C20.6 2 22 3.4 22 6.9Z"),
    )
}

internal val RefreshVector: ImageVector by lazy {
    iconsax(
        "Refresh",
        stroke(
            "M22 12C22 17.52 17.52 22 12 22C6.48 22 3.11 16.44 3.11 16.44M3.11 16.44H7.63M3.11 16.44V21.44M2 12C2 6.48 6.44 2 12 2C18.67 2 22 7" +
                ".56 22 7.56M22 7.56V2.56M22 7.56H17.56",
        ),
    )
}

internal val RotateVector: ImageVector by lazy {
    iconsax(
        "Rotate",
        stroke(
            "M14.8901 5.08039C14.0201 4.82039 13.0601 4.65039 12.0001 4.65039C7.21008 4.65039 3.33008 8.53039 3.33008 13.3204C3.33008 18.1204 7" +
                ".21008 22.0004 12.0001 22.0004C16.7901 22.0004 20.6701 18.1204 20.6701 13.3304C20.6701 11.5504 20.1301 9.89039 19.2101 8.51039",
        ),
        stroke("M16.13 5.32L13.24 2"),
        stroke("M16.13 5.32031L12.76 7.78031"),
    )
}

internal val SearchVector: ImageVector by lazy {
    iconsax(
        "Search",
        stroke("M11.5 21C16.7467 21 21 16.7467 21 11.5C21 6.25329 16.7467 2 11.5 2C6.25329 2 2 6.25329 2 11.5C2 16.7467 6.25329 21 11.5 21Z"),
        stroke("M22 22L20 20"),
    )
}

internal val SettingsVector: ImageVector by lazy {
    iconsax(
        "Settings",
        stroke("M12 15C13.6569 15 15 13.6569 15 12C15 10.3431 13.6569 9 12 9C10.3431 9 9 10.3431 9 12C9 13.6569 10.3431 15 12 15Z"),
        stroke(
            "M2 12.8794V11.1194C2 10.0794 2.85 9.21945 3.9 9.21945C5.71 9.21945 6.45 7.93945 5.54 6.36945C5.02 5.46945 5.33 4.29945 6.24 3.7794" +
                "5L7.97 2.78945C8.76 2.31945 9.78 2.59945 10.25 3.38945L10.36 3.57945C11.26 5.14945 12.74 5.14945 13.65 3.57945L13.76 3.38945C14.23" +
                " 2.59945 15.25 2.31945 16.04 2.78945L17.77 3.77945C18.68 4.29945 18.99 5.46945 18.47 6.36945C17.56 7.93945 18.3 9.21945 20.11 9.21" +
                "945C21.15 9.21945 22.01 10.0694 22.01 11.1194V12.8794C22.01 13.9194 21.16 14.7794 20.11 14.7794C18.3 14.7794 17.56 16.0594 18.47 1" +
                "7.6294C18.99 18.5394 18.68 19.6994 17.77 20.2194L16.04 21.2094C15.25 21.6794 14.23 21.3994 13.76 20.6094L13.65 20.4194C12.75 18.84" +
                "94 11.27 18.8494 10.36 20.4194L10.25 20.6094C9.78 21.3994 8.76 21.6794 7.97 21.2094L6.24 20.2194C5.33 19.6994 5.02 18.5294 5.54 17" +
                ".6294C6.45 16.0594 5.71 14.7794 3.9 14.7794C2.85 14.7794 2 13.9194 2 12.8794Z",
        ),
    )
}

internal val StopVector: ImageVector by lazy {
    iconsax(
        "Stop",
        stroke("M9.3 21H14.7C19.2 21 21 19.2 21 14.7V9.3C21 4.8 19.2 3 14.7 3H9.3C4.8 3 3 4.8 3 9.3V14.7C3 19.2 4.8 21 9.3 21Z"),
    )
}

internal val SunVector: ImageVector by lazy {
    iconsax(
        "Sun",
        stroke(
            "M12 18.5C15.5899 18.5 18.5 15.5899 18.5 12C18.5 8.41015 15.5899 5.5 12 5.5C8.41015 5.5 5.5 8.41015 5.5 12C5.5 15.5899 8.41015 18.5" +
                " 12 18.5Z",
        ),
        stroke(
            "M19.14 19.14L19.01 19.01M19.01 4.99L19.14 4.86M4.86 19.14L4.99 19.01M12 2.08V2M12 22V21.92M2.08 12H2M22 12H21.92M4.99 4.99L4.86 4." +
                "86",
            width = 2f,
        ),
    )
}

internal val TerminalVector: ImageVector by lazy {
    iconsax(
        "Terminal",
        stroke(
            "M6.89001 9C7.87001 9.49 8.71001 10.23 9.32001 11.15C9.67001 11.67 9.67001 12.34 9.32001 12.86C8.71001 13.77 7.87001 14.51 6.89001 " +
                "15",
        ),
        stroke("M13 15H17"),
        stroke("M9 22H15C20 22 22 20 22 15V9C22 4 20 2 15 2H9C4 2 2 4 2 9V15C2 20 4 22 9 22Z"),
    )
}

internal val UploadVector: ImageVector by lazy {
    iconsax(
        "Upload",
        stroke(
            "M16.44 8.90039C20.04 9.21039 21.51 11.0604 21.51 15.1104V15.2404C21.51 19.7104 19.72 21.5004 15.25 21.5004H8.73998C4.26998 21.5004" +
                " 2.47998 19.7104 2.47998 15.2404V15.1104C2.47998 11.0904 3.92998 9.24039 7.46998 8.91039",
        ),
        stroke("M12 15.0001V3.62012"),
        stroke("M15.3499 5.85L11.9999 2.5L8.6499 5.85"),
    )
}

internal val VolumeDownVector: ImageVector by lazy {
    iconsax(
        "VolumeDown",
        stroke(
            "M2 9.96021V14.0402C2 16.0802 3.02 17.1002 5.06 17.1002H6.52C6.9 17.1002 7.28 17.2102 7.6 17.4102L10.58 19.2702C13.16 20.8802 15.26" +
                " 19.7102 15.26 16.6702V7.32021C15.26 4.28021 13.15 3.11021 10.58 4.72021L7.6 6.59021C7.27 6.79021 6.9 6.90021 6.52 6.90021H5.06C3." +
                "02 6.90021 2 7.92021 2 9.96021Z",
            isRoundCapped = false,
        ),
        stroke("M18 12H22"),
    )
}

internal val VolumeUpVector: ImageVector by lazy {
    iconsax(
        "VolumeUp",
        stroke(
            "M2 9.99979V13.9998C2 15.9998 3 16.9998 5 16.9998H6.43C6.8 16.9998 7.17 17.1098 7.49 17.2998L10.41 19.1298C12.93 20.7098 15 19.5598" +
                " 15 16.5898V7.40979C15 4.42979 12.93 3.28979 10.41 4.86979L7.49 6.69979C7.17 6.88979 6.8 6.99979 6.43 6.99979H5C3 6.99979 2 7.9997" +
                "9 2 9.99979Z",
            isRoundCapped = false,
        ),
        stroke("M18 8C19.78 10.37 19.78 13.63 18 16"),
        stroke("M19.83 5.5C22.72 9.35 22.72 14.65 19.83 18.5"),
    )
}

internal val WarningVector: ImageVector by lazy {
    iconsax(
        "Warning",
        stroke("M12 9V14"),
        stroke(
            "M12.0001 21.4093H5.94005C2.47005 21.4093 1.02005 18.9293 2.70005 15.8993L5.82006 10.2793L8.76006 4.9993C10.5401 1.7893 13.4601 1.7" +
                "893 15.2401 4.9993L18.1801 10.2893L21.3001 15.9093C22.9801 18.9393 21.5201 21.4193 18.0601 21.4193H12.0001V21.4093Z",
        ),
        stroke("M11.9945 17H12.0035", width = 2f),
    )
}

internal val WifiVector: ImageVector by lazy {
    iconsax(
        "Wifi",
        stroke("M4.91003 11.8396C9.21003 8.51961 14.8 8.51961 19.1 11.8396"),
        stroke("M2 8.35961C8.06 3.67961 15.94 3.67961 22 8.35961"),
        stroke("M6.79004 15.4902C9.94004 13.0502 14.05 13.0502 17.2 15.4902"),
        stroke("M9.40002 19.1494C10.98 17.9294 13.03 17.9294 14.61 19.1494"),
    )
}
