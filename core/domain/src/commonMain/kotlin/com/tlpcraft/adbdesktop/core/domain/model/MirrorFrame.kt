package com.tlpcraft.adbdesktop.core.domain.model

/** One decoded screen image as BGRA bytes, top row first. A plain class on purpose: arrays compare by identity in data classes. */
class MirrorFrame(val width: Int, val height: Int, val pixels: ByteArray)
