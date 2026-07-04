package com.tlpcraft.adbdesktop.core.data.source.adb.parser

/** adb reports most wireless results on stdout with exit code 0, so success is read from the text. */
internal object WirelessOutputParser {
    private val connectSuccessPrefixes = listOf("connected to", "already connected to")
    private const val DISCONNECT_SUCCESS_PREFIX = "disconnected"
    private const val PAIR_SUCCESS_PREFIX = "Successfully paired"

    fun isConnectSuccess(output: String): Boolean = connectSuccessPrefixes.any { output.trim().startsWith(it) }

    fun isDisconnectSuccess(output: String): Boolean = output.trim().startsWith(DISCONNECT_SUCCESS_PREFIX)

    fun isPairSuccess(output: String): Boolean = output.trim().startsWith(PAIR_SUCCESS_PREFIX)

    /** Extracts the human readable reason from lines such as `failed to connect to 'x': Connection refused`. */
    fun failureReason(output: String): String {
        val line = output.trim().lineSequence().lastOrNull { it.isNotBlank() }.orEmpty()
        return line.removePrefix("error:").removePrefix("Failed:").substringAfterLast("': ").trim()
    }
}
