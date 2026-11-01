package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LifecycleStartEffect

/** Reports whether the calling screen can be seen: it stops being visible when it is left or the window is minimized. */
@Composable
fun VisibilityEffect(onVisibilityChange: (Boolean) -> Unit) {
    val currentOnVisibilityChange by rememberUpdatedState(onVisibilityChange)

    LifecycleStartEffect(Unit) {
        currentOnVisibilityChange(true)
        onStopOrDispose { currentOnVisibilityChange(false) }
    }
}
