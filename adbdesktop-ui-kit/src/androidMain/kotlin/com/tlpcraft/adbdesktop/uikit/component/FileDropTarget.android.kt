package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** The Android target only exists for IDE previews, where there is nothing to drop. */
@Composable
internal actual fun rememberFileDropTarget(onHoverChange: (Boolean) -> Unit, onFilesDrop: (List<String>) -> Unit): Modifier = Modifier
