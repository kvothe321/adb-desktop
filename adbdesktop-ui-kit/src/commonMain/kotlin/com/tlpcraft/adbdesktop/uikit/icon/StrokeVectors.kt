package com.tlpcraft.adbdesktop.uikit.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private const val VIEWPORT = 24f
private const val STROKE_WIDTH = 1.5f

/** Outline glyphs drawn in black; [com.tlpcraft.adbdesktop.uikit.component.Icon] tints them. */
private fun strokeIcon(name: String, vararg paths: PathBuilder.() -> Unit): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = VIEWPORT.dp,
    defaultHeight = VIEWPORT.dp,
    viewportWidth = VIEWPORT,
    viewportHeight = VIEWPORT,
).apply {
    paths.forEach { pathData ->
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE_WIDTH,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = pathData,
        )
    }
}.build()

internal val PackageVector: ImageVector by lazy {
    strokeIcon(
        "Package",
        {
            moveTo(3.17f, 7.44f)
            lineTo(12f, 12.55f)
            lineTo(20.77f, 7.47f)
        },
        {
            moveTo(12f, 21.61f)
            verticalLineTo(12.54f)
        },
        {
            moveTo(9.93f, 2.48f)
            lineTo(4.59f, 5.44f)
            curveTo(3.38f, 6.11f, 2.39f, 7.79f, 2.39f, 9.17f)
            verticalLineTo(14.82f)
            curveTo(2.39f, 16.2f, 3.38f, 17.88f, 4.59f, 18.55f)
            lineTo(9.93f, 21.52f)
            curveTo(11.07f, 22.15f, 12.94f, 22.15f, 14.08f, 21.52f)
            lineTo(19.42f, 18.55f)
            curveTo(20.63f, 17.88f, 21.62f, 16.2f, 21.62f, 14.82f)
            verticalLineTo(9.17f)
            curveTo(21.62f, 7.79f, 20.63f, 6.11f, 19.42f, 5.44f)
            lineTo(14.08f, 2.47f)
            curveTo(12.93f, 1.84f, 11.07f, 1.84f, 9.93f, 2.48f)
            close()
        },
    )
}

internal val FolderVector: ImageVector by lazy {
    strokeIcon("Folder", {
        moveTo(22f, 11f)
        verticalLineTo(17f)
        curveTo(22f, 21f, 21f, 22f, 17f, 22f)
        horizontalLineTo(7f)
        curveTo(3f, 22f, 2f, 21f, 2f, 17f)
        verticalLineTo(7f)
        curveTo(2f, 3f, 3f, 2f, 7f, 2f)
        horizontalLineTo(8.5f)
        curveTo(10f, 2f, 10.33f, 2.44f, 10.9f, 3.2f)
        lineTo(12.4f, 5.2f)
        curveTo(12.78f, 5.7f, 13f, 6f, 14f, 6f)
        horizontalLineTo(17f)
        curveTo(21f, 6f, 22f, 7f, 22f, 11f)
        close()
    })
}

internal val DevicesVector: ImageVector by lazy {
    strokeIcon(
        "Devices",
        {
            moveTo(10f, 16.95f)
            horizontalLineTo(6.21f)
            curveTo(2.84f, 16.95f, 2f, 16.11f, 2f, 12.74f)
            verticalLineTo(6.74f)
            curveTo(2f, 3.37f, 2.84f, 2.53f, 6.21f, 2.53f)
            horizontalLineTo(16.74f)
            curveTo(20.11f, 2.53f, 20.95f, 3.37f, 20.95f, 6.74f)
        },
        {
            moveTo(10f, 21.47f)
            verticalLineTo(16.95f)
        },
        {
            moveTo(2f, 12.95f)
            horizontalLineTo(10f)
        },
        {
            moveTo(6.74f, 21.47f)
            horizontalLineTo(10f)
        },
        {
            moveTo(22f, 12.8f)
            verticalLineTo(18.51f)
            curveTo(22f, 20.88f, 21.41f, 21.47f, 19.04f, 21.47f)
            horizontalLineTo(15.49f)
            curveTo(13.12f, 21.47f, 12.53f, 20.88f, 12.53f, 18.51f)
            verticalLineTo(12.8f)
            curveTo(12.53f, 10.43f, 13.12f, 9.84f, 15.49f, 9.84f)
            horizontalLineTo(19.04f)
            curveTo(21.41f, 9.84f, 22f, 10.43f, 22f, 12.8f)
            close()
        },
        {
            moveTo(17.24f, 18.25f)
            horizontalLineTo(17.25f)
        },
    )
}

internal val SunVector: ImageVector by lazy {
    strokeIcon(
        "Sun",
        {
            moveTo(12f, 7f)
            arcTo(5f, 5f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 12f, y1 = 17f)
            arcTo(5f, 5f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 12f, y1 = 7f)
            close()
        },
        {
            moveTo(12f, 1.5f)
            verticalLineTo(3.5f)
            moveTo(12f, 20.5f)
            verticalLineTo(22.5f)
            moveTo(1.5f, 12f)
            horizontalLineTo(3.5f)
            moveTo(20.5f, 12f)
            horizontalLineTo(22.5f)
            moveTo(4.6f, 4.6f)
            lineTo(6f, 6f)
            moveTo(18f, 18f)
            lineTo(19.4f, 19.4f)
            moveTo(4.6f, 19.4f)
            lineTo(6f, 18f)
            moveTo(18f, 6f)
            lineTo(19.4f, 4.6f)
        },
    )
}

internal val MoonVector: ImageVector by lazy {
    strokeIcon("Moon", {
        moveTo(20.5f, 14.5f)
        arcTo(8.5f, 8.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 9.5f, y1 = 3.5f)
        arcTo(7f, 7f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 20.5f, y1 = 14.5f)
        close()
    })
}

internal val WifiVector: ImageVector by lazy {
    strokeIcon(
        "Wifi",
        {
            moveTo(2f, 8.8f)
            arcTo(14.5f, 14.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 22f, y1 = 8.8f)
        },
        {
            moveTo(5.2f, 12.3f)
            arcTo(9.8f, 9.8f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 18.8f, y1 = 12.3f)
        },
        {
            moveTo(8.5f, 15.8f)
            arcTo(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, x1 = 15.5f, y1 = 15.8f)
        },
        {
            moveTo(12f, 19.5f)
            horizontalLineTo(12.01f)
        },
    )
}

internal val TerminalVector: ImageVector by lazy {
    strokeIcon(
        "Terminal",
        {
            moveTo(4f, 4f)
            horizontalLineTo(20f)
            curveTo(21.1f, 4f, 22f, 4.9f, 22f, 6f)
            verticalLineTo(18f)
            curveTo(22f, 19.1f, 21.1f, 20f, 20f, 20f)
            horizontalLineTo(4f)
            curveTo(2.9f, 20f, 2f, 19.1f, 2f, 18f)
            verticalLineTo(6f)
            curveTo(2f, 4.9f, 2.9f, 4f, 4f, 4f)
            close()
        },
        {
            moveTo(6f, 9f)
            lineTo(9f, 12f)
            lineTo(6f, 15f)
        },
        {
            moveTo(11.5f, 15f)
            horizontalLineTo(17f)
        },
    )
}
