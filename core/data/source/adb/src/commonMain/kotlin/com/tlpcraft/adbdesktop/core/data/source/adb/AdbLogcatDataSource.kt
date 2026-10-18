package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.LogEntryDto
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.LineBuffer
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.LogcatParser
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.shellQuoted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

interface AdbLogcatDataSource {
    /**
     * Emits batches of entries as they arrive, starting with the most recent history, or with the entries from the
     * timestamp [since] on, in the `MM-DD hh:mm:ss.mmm` form entries carry.
     */
    fun streamLogs(serial: String, since: String? = null): Flow<List<LogEntryDto>>

    suspend fun clearLogs(serial: String)

    /** Processes currently running for [packageName]; empty when it is not running. */
    suspend fun readProcessIds(serial: String, packageName: String): Set<Int>
}

class AdbLogcatDataSourceImpl(private val client: AdbClient) : AdbLogcatDataSource {

    /** adb quotes logcat's arguments itself, so the timestamp goes in as it is. */
    override fun streamLogs(serial: String, since: String?): Flow<List<LogEntryDto>> = flow {
        val lines = LineBuffer()
        client.stream("-s", serial, "logcat", "-v", "threadtime", "-T", since ?: HISTORY_LINES)
            .map { chunk -> lines.feed(chunk).mapNotNull(LogcatParser::parse) }
            .filter { it.isNotEmpty() }
            .collect { emit(it) }
    }

    override suspend fun clearLogs(serial: String) {
        client.run(listOf("-s", serial, "logcat", "-c"))
    }

    override suspend fun readProcessIds(serial: String, packageName: String): Set<Int> =
        client.shellResult(serial, listOf("pidof", packageName.shellQuoted())).stdout.trim().split(' ').mapNotNull(String::toIntOrNull).toSet()

    private companion object {
        const val HISTORY_LINES = "1000"
    }
}
