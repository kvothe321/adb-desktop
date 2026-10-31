package com.tlpcraft.adbdesktop.core.data.source.adb.parser

/**
 * Splits the `adb track-devices` stream into device listings. The stream is a sequence of frames,
 * each a 4-digit hex payload length followed by the payload. Chunks may cut frames anywhere.
 */
internal class TrackDevicesDecoder {
    private val buffer = StringBuilder()

    fun decode(chunk: String): List<String> {
        buffer.append(chunk)
        val payloads = mutableListOf<String>()
        while (true) {
            val payload = takeFrame() ?: break
            payloads += payload
        }
        return payloads
    }

    private fun takeFrame(): String? {
        if (buffer.length < LENGTH_DIGITS) return null
        val payloadLength = buffer.substring(0, LENGTH_DIGITS).toIntOrNull(radix = 16)
            ?: throw IllegalStateException("Malformed track-devices frame header: ${buffer.take(LENGTH_DIGITS)}")
        val frameLength = LENGTH_DIGITS + payloadLength
        if (buffer.length < frameLength) return null

        val payload = buffer.substring(LENGTH_DIGITS, frameLength)
        buffer.deleteRange(0, frameLength)
        return payload
    }

    private companion object {
        const val LENGTH_DIGITS = 4
    }
}
