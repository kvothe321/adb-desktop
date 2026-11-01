package com.tlpcraft.adbdesktop.core.data.repository.mapper

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MirrorControlDto
import com.tlpcraft.adbdesktop.core.domain.model.DeviceKey
import com.tlpcraft.adbdesktop.core.domain.model.KeyAction
import com.tlpcraft.adbdesktop.core.domain.model.KeyModifier
import com.tlpcraft.adbdesktop.core.domain.model.MirrorInput
import com.tlpcraft.adbdesktop.core.domain.model.TouchAction

/** One input can take several messages: a back press is a down and an up. */
internal fun MirrorInput.toControlMessages(): List<MirrorControlDto> = when (this) {
    is MirrorInput.Touch -> listOf(MirrorControlDto.Touch(action.motionAction(), point.x, point.y, point.frameWidth, point.frameHeight))
    is MirrorInput.Scroll -> listOf(MirrorControlDto.Scroll(point.x, point.y, point.frameWidth, point.frameHeight, horizontal, vertical))
    is MirrorInput.Key -> listOf(MirrorControlDto.Key(action.keyAction(), key.keyCode(), modifiers.metaState()))
    is MirrorInput.Text -> listOf(MirrorControlDto.Text(text))
    is MirrorInput.Paste -> listOf(MirrorControlDto.SetClipboard(text, paste = true))
    MirrorInput.BackOrScreenOn -> listOf(MirrorControlDto.BackOrScreenOn(KEY_ACTION_DOWN), MirrorControlDto.BackOrScreenOn(KEY_ACTION_UP))
    MirrorInput.Rotate -> listOf(MirrorControlDto.RotateDevice)
}

private const val KEY_ACTION_DOWN = 0
private const val KEY_ACTION_UP = 1

private fun TouchAction.motionAction(): Int = when (this) {
    TouchAction.Down -> 0
    TouchAction.Up -> 1
    TouchAction.Move -> 2
}

private fun KeyAction.keyAction(): Int = when (this) {
    KeyAction.Down -> KEY_ACTION_DOWN
    KeyAction.Up -> KEY_ACTION_UP
}

/** Android `KeyEvent` meta flags, with the left key's flag as a real keyboard sets it. */
private fun Set<KeyModifier>.metaState(): Int = fold(0) { state, modifier ->
    state or when (modifier) {
        KeyModifier.Shift -> META_SHIFT_ON or META_SHIFT_LEFT_ON
        KeyModifier.Alt -> META_ALT_ON or META_ALT_LEFT_ON
        KeyModifier.Ctrl -> META_CTRL_ON or META_CTRL_LEFT_ON
        KeyModifier.Meta -> META_META_ON or META_META_LEFT_ON
    }
}

private fun DeviceKey.keyCode(): Int = when (this) {
    DeviceKey.Home -> KEYCODE_HOME
    DeviceKey.Back -> KEYCODE_BACK
    DeviceKey.AppSwitch -> KEYCODE_APP_SWITCH
    DeviceKey.Power -> KEYCODE_POWER
    DeviceKey.VolumeUp -> KEYCODE_VOLUME_UP
    DeviceKey.VolumeDown -> KEYCODE_VOLUME_DOWN
    DeviceKey.Enter -> KEYCODE_ENTER
    DeviceKey.Backspace -> KEYCODE_DEL
    DeviceKey.ForwardDelete -> KEYCODE_FORWARD_DEL
    DeviceKey.Tab -> KEYCODE_TAB
    DeviceKey.Escape -> KEYCODE_ESCAPE
    DeviceKey.Space -> KEYCODE_SPACE
    DeviceKey.Up -> KEYCODE_DPAD_UP
    DeviceKey.Down -> KEYCODE_DPAD_DOWN
    DeviceKey.Left -> KEYCODE_DPAD_LEFT
    DeviceKey.Right -> KEYCODE_DPAD_RIGHT
    DeviceKey.MoveHome -> KEYCODE_MOVE_HOME
    DeviceKey.MoveEnd -> KEYCODE_MOVE_END
    DeviceKey.PageUp -> KEYCODE_PAGE_UP
    DeviceKey.PageDown -> KEYCODE_PAGE_DOWN
    DeviceKey.Insert -> KEYCODE_INSERT
    in DeviceKey.A..DeviceKey.Z -> KEYCODE_A + (ordinal - DeviceKey.A.ordinal)
    else -> KEYCODE_0 + (ordinal - DeviceKey.Digit0.ordinal)
}

private const val META_SHIFT_ON = 0x1
private const val META_SHIFT_LEFT_ON = 0x40
private const val META_ALT_ON = 0x2
private const val META_ALT_LEFT_ON = 0x10
private const val META_CTRL_ON = 0x1000
private const val META_CTRL_LEFT_ON = 0x2000
private const val META_META_ON = 0x10000
private const val META_META_LEFT_ON = 0x20000

private const val KEYCODE_HOME = 3
private const val KEYCODE_BACK = 4
private const val KEYCODE_0 = 7
private const val KEYCODE_DPAD_UP = 19
private const val KEYCODE_DPAD_DOWN = 20
private const val KEYCODE_DPAD_LEFT = 21
private const val KEYCODE_DPAD_RIGHT = 22
private const val KEYCODE_VOLUME_UP = 24
private const val KEYCODE_VOLUME_DOWN = 25
private const val KEYCODE_POWER = 26
private const val KEYCODE_A = 29
private const val KEYCODE_TAB = 61
private const val KEYCODE_SPACE = 62
private const val KEYCODE_ENTER = 66
private const val KEYCODE_DEL = 67
private const val KEYCODE_PAGE_UP = 92
private const val KEYCODE_PAGE_DOWN = 93
private const val KEYCODE_ESCAPE = 111
private const val KEYCODE_FORWARD_DEL = 112
private const val KEYCODE_MOVE_HOME = 122
private const val KEYCODE_MOVE_END = 123
private const val KEYCODE_INSERT = 124
private const val KEYCODE_APP_SWITCH = 187
