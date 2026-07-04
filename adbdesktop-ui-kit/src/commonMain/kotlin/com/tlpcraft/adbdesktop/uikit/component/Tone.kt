package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme

/** Semantic color role shared by components that communicate a status. */
enum class Tone {
    Neutral,
    Accent,
    Info,
    Success,
    Warning,
    Error,
}

internal val Tone.color: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        Tone.Neutral -> AppTheme.colors.textSecondary
        Tone.Accent -> AppTheme.colors.accent
        Tone.Info -> AppTheme.colors.info
        Tone.Success -> AppTheme.colors.success
        Tone.Warning -> AppTheme.colors.warning
        Tone.Error -> AppTheme.colors.error
    }
