package com.tlpcraft.adbdesktop.core.domain.model

/** Input for a mirrored device, applied as if it happened on the device itself. */
sealed interface MirrorInput {
    data class Touch(val action: TouchAction, val point: MirrorPoint) : MirrorInput

    /** [horizontal] is positive to the right and [vertical] positive upwards, in wheel notches. */
    data class Scroll(val point: MirrorPoint, val horizontal: Float, val vertical: Float) : MirrorInput

    data class Key(val action: KeyAction, val key: DeviceKey, val modifiers: Set<KeyModifier> = emptySet()) : MirrorInput

    /** Typed characters, entered as text so the device keyboard layout does not matter. */
    data class Text(val text: String) : MirrorInput

    /** Puts [text] on the device clipboard and pastes it where the device has focus. */
    data class Paste(val text: String) : MirrorInput

    /** Back, or turns the screen on when it is off. */
    data object BackOrScreenOn : MirrorInput

    data object Rotate : MirrorInput
}

/** A position in a frame of [frameWidth] by [frameHeight], the frame the user saw when acting on it. */
data class MirrorPoint(val x: Int, val y: Int, val frameWidth: Int, val frameHeight: Int)

enum class TouchAction {
    Down,
    Move,
    Up,
}

enum class KeyAction {
    Down,
    Up,
}

enum class KeyModifier {
    Shift,
    Ctrl,
    Alt,
    Meta,
}

/** Keys a mirrored device understands. */
enum class DeviceKey {
    Home,
    Back,
    AppSwitch,
    Power,
    VolumeUp,
    VolumeDown,
    Enter,
    Backspace,
    ForwardDelete,
    Tab,
    Escape,
    Space,
    Up,
    Down,
    Left,
    Right,
    MoveHome,
    MoveEnd,
    PageUp,
    PageDown,
    Insert,
    A,
    B,
    C,
    D,
    E,
    F,
    G,
    H,
    I,
    J,
    K,
    L,
    M,
    N,
    O,
    P,
    Q,
    R,
    S,
    T,
    U,
    V,
    W,
    X,
    Y,
    Z,
    Digit0,
    Digit1,
    Digit2,
    Digit3,
    Digit4,
    Digit5,
    Digit6,
    Digit7,
    Digit8,
    Digit9,
}
