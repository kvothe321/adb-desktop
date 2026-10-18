package com.tlpcraft.adbdesktop.core.domain.model

/** An app's launcher icon as PNG bytes. A plain class on purpose: arrays compare by identity in data classes. */
class AppIcon(val png: ByteArray)
