package com.tlpcraft.adbdesktop.core.domain.service

/** The clipboard of the computer running the app. */
interface HostClipboard {
    suspend fun copyPng(png: ByteArray)

    /** The current text, or `null` when the clipboard holds none. */
    suspend fun readText(): String?
}
