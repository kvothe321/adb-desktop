package com.tlpcraft.adbdesktop.core.data.source.adb.dto

/** One decoded screen image as BGRA bytes, top row first. */
class MirrorFrameDto(val width: Int, val height: Int, val pixels: ByteArray)
