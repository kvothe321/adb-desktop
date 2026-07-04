package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

/** Queue of short, transient messages shown at the bottom of a [Scaffold]. */
@Stable
class MessageHostState {
    internal val snackbarHostState = SnackbarHostState()

    /** Suspends until the message is dismissed or times out. */
    suspend fun show(message: String) {
        snackbarHostState.showSnackbar(message)
    }
}

@Composable
fun rememberMessageHostState(): MessageHostState = remember { MessageHostState() }
