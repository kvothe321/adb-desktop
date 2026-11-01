package com.tlpcraft.adbdesktop.uikit.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** How much room the window gives, so layouts can adapt. Read it through [AppTheme.windowWidth]. */
enum class WindowWidth {
    /** The sidebar folds into a rail of icons. */
    Compact,
    Medium,
    Expanded,
    ;

    internal companion object {
        private val MEDIUM_FROM = 900.dp
        private val EXPANDED_FROM = 1240.dp

        fun of(width: Dp): WindowWidth = when {
            width < MEDIUM_FROM -> Compact
            width < EXPANDED_FROM -> Medium
            else -> Expanded
        }
    }
}

internal val LocalWindowWidth = compositionLocalOf { WindowWidth.Expanded }
