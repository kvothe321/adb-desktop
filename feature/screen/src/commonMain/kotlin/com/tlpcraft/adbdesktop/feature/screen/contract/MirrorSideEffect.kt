package com.tlpcraft.adbdesktop.feature.screen.contract

sealed interface MirrorSideEffect {
    /** [details] is the message from the device, shown as it is. */
    data class Failed(val details: String) : MirrorSideEffect
}
