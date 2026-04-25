package com.tlpcraft.adbdesktop.uikit.components.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val UiKitIcons.MonitorMobile: ImageVector
    get() {
        if (icon != null) return icon!!

        icon = ImageVector.Builder(
            name = "Icon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF171717)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(10f, 16.95f)
                horizontalLineTo(6.21f)
                curveTo(2.84f, 16.95f, 2f, 16.11f, 2f, 12.74f)
                verticalLineTo(6.74f)
                curveTo(2f, 3.37f, 2.84f, 2.53f, 6.21f, 2.53f)
                horizontalLineTo(16.74f)
                curveTo(20.11f, 2.53f, 20.95f, 3.37f, 20.95f, 6.74f)
            }
            path(
                stroke = SolidColor(Color(0xFF171717)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(10f, 21.47f)
                verticalLineTo(16.95f)
            }
            path(
                stroke = SolidColor(Color(0xFF171717)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(2f, 12.95f)
                horizontalLineTo(10f)
            }
            path(
                stroke = SolidColor(Color(0xFF171717)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(6.73999f, 21.47f)
                horizontalLineTo(9.99999f)
            }
            path(
                stroke = SolidColor(Color(0xFF171717)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
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
            }
            path(
                stroke = SolidColor(Color(0xFF171717)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(17.2445f, 18.25f)
                horizontalLineTo(17.2535f)
            }
        }.build()

        return icon!!
    }

private var icon: ImageVector? = null
