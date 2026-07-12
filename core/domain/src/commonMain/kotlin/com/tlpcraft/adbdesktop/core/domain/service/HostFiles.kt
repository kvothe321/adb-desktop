package com.tlpcraft.adbdesktop.core.domain.service

/** File dialogs and file writes on the computer running the app. Picks return `null` or an empty list when cancelled. */
interface HostFiles {
    suspend fun pickFiles(extension: String? = null): List<String>

    suspend fun pickDirectory(): String?

    suspend fun pickSaveLocation(suggestedName: String): String?

    suspend fun write(path: String, content: ByteArray)
}
