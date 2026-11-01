package com.tlpcraft.adbdesktop.uikit.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
internal data class AppShapes(
    val small: Shape = RoundedCornerShape(6.dp),
    val medium: Shape = RoundedCornerShape(10.dp),
    val large: Shape = RoundedCornerShape(18.dp),
    val pill: Shape = CircleShape,
)

internal val LocalAppShapes = staticCompositionLocalOf { AppShapes() }
