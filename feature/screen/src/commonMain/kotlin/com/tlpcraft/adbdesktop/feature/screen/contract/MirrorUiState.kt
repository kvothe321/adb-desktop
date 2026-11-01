package com.tlpcraft.adbdesktop.feature.screen.contract

import com.tlpcraft.adbdesktop.core.domain.model.MirrorFrame

data class MirrorUiState(
    val serial: String? = null,
    val isOn: Boolean = false,
    val mirror: MirrorState = MirrorState.Off,
)

sealed interface MirrorState {
    data object Off : MirrorState

    data object Starting : MirrorState

    /** A plain class on purpose: every frame is a new image, so comparing the pixels would only cost time. */
    class Live(val frame: MirrorFrame) : MirrorState
}

/** The device's own buttons, pressed from the mirror's toolbar. */
enum class DeviceButton {
    Back,
    Home,
    Recents,
    VolumeDown,
    VolumeUp,
    Power,
    Rotate,
}
