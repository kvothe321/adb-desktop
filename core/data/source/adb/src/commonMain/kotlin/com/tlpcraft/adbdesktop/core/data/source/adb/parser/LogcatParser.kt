package com.tlpcraft.adbdesktop.core.data.source.adb.parser

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.LogEntryDto

/** Parses `logcat -v threadtime` lines; buffer markers such as `--------- beginning of main` yield `null`. */
internal object LogcatParser {
    private const val TIMESTAMP = 1
    private const val PROCESS_ID = 2
    private const val THREAD_ID = 3
    private const val LEVEL = 4
    private const val TAG = 5
    private const val MESSAGE = 6

    private val entryLine = Regex("""^(\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+(\d+)\s+([VDIWEFS])\s(.*?)\s*:(?: (.*))?$""")

    fun parse(line: String): LogEntryDto? = entryLine.find(line)?.groupValues?.let { columns ->
        LogEntryDto(
            timestamp = columns[TIMESTAMP],
            processId = columns[PROCESS_ID].toInt(),
            threadId = columns[THREAD_ID].toInt(),
            level = columns[LEVEL].single(),
            tag = columns[TAG],
            message = columns[MESSAGE],
        )
    }
}
