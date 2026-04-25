package com.tlpcraft.adbdesktop.uikit.components.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val UiKitIcons.Box: ImageVector
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
                moveTo(3.16992f, 7.43994f)
                lineTo(11.9999f, 12.5499f)
                lineTo(20.7699f, 7.46994f)
            }
            path(
                stroke = SolidColor(Color(0xFF171717)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(12f, 21.61f)
                verticalLineTo(12.54f)
            }
            path(
                stroke = SolidColor(Color(0xFF171717)),
                strokeLineWidth = 1.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(9.93014f, 2.48004f)
                lineTo(4.59014f, 5.44004f)
                curveTo(3.38014f, 6.11004f, 2.39014f, 7.79004f, 2.39014f, 9.17004f)
                verticalLineTo(14.82f)
                curveTo(2.39014f, 16.2f, 3.38014f, 17.88f, 4.59014f, 18.55f)
                lineTo(9.93014f, 21.52f)
                curveTo(11.0701f, 22.15f, 12.9401f, 22.15f, 14.0801f, 21.52f)
                lineTo(19.4201f, 18.55f)
                curveTo(20.6301f, 17.88f, 21.6201f, 16.2f, 21.6201f, 14.82f)
                verticalLineTo(9.17004f)
                curveTo(21.6201f, 7.79004f, 20.6301f, 6.11004f, 19.4201f, 5.44004f)
                lineTo(14.0801f, 2.47004f)
                curveTo(12.9301f, 1.84004f, 11.0701f, 1.84004f, 9.93014f, 2.48004f)
                close()
            }
        }.build()

        return icon!!
    }

private var icon: ImageVector? = null
