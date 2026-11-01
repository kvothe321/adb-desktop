package com.tlpcraft.adbdesktop.navigation

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

val isMacOs: Boolean = System.getProperty("os.name").startsWith("Mac", ignoreCase = true)

private val digitKeys = listOf(Key.One, Key.Two, Key.Three, Key.Four, Key.Five, Key.Six, Key.Seven)

/** The section bound to this key press, such as Ctrl+2 (Cmd+2 on macOS), or `null`. */
fun KeyEvent.sectionShortcut(): Section? {
    val modifierPressed = if (isMacOs) isMetaPressed else isCtrlPressed
    if (type != KeyEventType.KeyDown || !modifierPressed) return null
    return digitKeys.indexOf(key).takeIf { it >= 0 }?.let { index -> Section.forShortcut(index + 1) }
}

/** Whether this key press opens the command palette: Ctrl+K, or Cmd+K on macOS. */
fun KeyEvent.isPaletteShortcut(): Boolean {
    val modifierPressed = if (isMacOs) isMetaPressed else isCtrlPressed
    return type == KeyEventType.KeyDown && modifierPressed && key == Key.K
}
