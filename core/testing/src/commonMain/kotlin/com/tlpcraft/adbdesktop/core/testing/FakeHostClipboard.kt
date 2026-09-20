package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.domain.service.HostClipboard

class FakeHostClipboard : HostClipboard {
    var copied: ByteArray? = null
    var text: String? = null

    override suspend fun copyPng(png: ByteArray) {
        copied = png
    }

    override suspend fun readText(): String? = text
}
