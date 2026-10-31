package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ControlSize(internal val height: Dp, internal val horizontalPadding: Dp) {
    Small(height = 26.dp, horizontalPadding = 8.dp),
    Medium(height = 32.dp, horizontalPadding = 12.dp),
}
