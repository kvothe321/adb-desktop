package com.tlpcraft.adbdesktop.core.domain.model

/**
 * How an app presents itself: its name in the device's language and its launcher icon as PNG bytes, each `null` when it
 * has none. A plain class on purpose: arrays compare by identity in data classes.
 */
class AppAppearance(val label: String?, val icon: ByteArray?)
