package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.domain.service.HostFiles

class FakeHostFiles : HostFiles {
    var pickedFiles: List<String> = emptyList()
    var pickedDirectory: String? = null
    var saveLocation: String? = null
    val written = mutableMapOf<String, ByteArray>()

    override suspend fun pickFiles(extension: String?): List<String> = pickedFiles

    override suspend fun pickDirectory(): String? = pickedDirectory

    override suspend fun pickSaveLocation(suggestedName: String): String? = saveLocation

    override suspend fun write(path: String, content: ByteArray) {
        written[path] = content
    }
}
