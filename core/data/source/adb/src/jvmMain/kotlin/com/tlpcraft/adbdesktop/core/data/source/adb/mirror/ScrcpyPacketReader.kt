package com.tlpcraft.adbdesktop.core.data.source.adb.mirror

import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream

/**
 * Reads scrcpy's video stream, where each packet has an 8 byte flags and timestamp field and a 4 byte size, big endian.
 * Parameter sets arrive as their own config packets and are put in front of the frame that follows, as the decoder wants them.
 */
internal class ScrcpyPacketReader(stream: InputStream) {
    private val input = DataInputStream(stream)
    private var pendingConfig = ByteArray(0)

    /** The next frame's bytes, or `null` when the stream ends between packets. */
    fun nextAccessUnit(): ByteArray? {
        while (true) {
            val header = try {
                input.readLong()
            } catch (_: EOFException) {
                return null
            }
            val size = input.readInt()
            if (size !in 0..MAX_PACKET_SIZE) throw IOException("Invalid video packet of $size bytes")
            val data = ByteArray(size).also(input::readFully)
            if (header and CONFIG_FLAG != 0L) {
                pendingConfig = data
            } else {
                return (pendingConfig + data).also { pendingConfig = ByteArray(0) }
            }
        }
    }

    private companion object {
        const val CONFIG_FLAG = 1L shl 62
        const val MAX_PACKET_SIZE = 32 * 1024 * 1024
    }
}
