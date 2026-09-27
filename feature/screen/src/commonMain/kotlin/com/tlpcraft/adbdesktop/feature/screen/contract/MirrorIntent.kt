package com.tlpcraft.adbdesktop.feature.screen.contract

import com.tlpcraft.adbdesktop.core.domain.model.MirrorInput

sealed interface MirrorIntent {
    data object MirrorClicked : MirrorIntent

    data object PinClicked : MirrorIntent

    data class VisibilityChanged(val isVisible: Boolean) : MirrorIntent

    /** Mouse, wheel or keyboard input on the mirrored screen. */
    data class InputGiven(val input: MirrorInput) : MirrorIntent

    data class DeviceButtonClicked(val button: DeviceButton) : MirrorIntent

    /** Paste the computer's clipboard on the device. */
    data object PasteRequested : MirrorIntent
}
