package com.tlpcraft.adbdesktop.presentation.component

/** Callbacks for the action buttons in the details panel. */
data class AppActions(
    val onOpen: () -> Unit = {},
    val onForceStop: () -> Unit = {},
    val onClearCache: () -> Unit = {},
    val onClearData: () -> Unit = {},
    val onUninstall: () -> Unit = {},
)
