package com.tlpcraft.adbdesktop.core.data.source.adb.parser

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.RemoteFileDto

/**
 * Parses `ls -la`. Regular rows are `perms links owner group size date time name`; device nodes show `major, minor`
 * as their size and entries the shell may not stat print `?` in every column.
 */
internal object FileListParser {
    private val entryLine = Regex("""^([-dlcbps][-rwxsStT]{9}\S?)\s+\d+\s+\S+\s+\S+\s+(\d+(?:,\s*\d+)?)\s+(\d{4}-\d{2}-\d{2} \d{2}:\d{2})\s(.+)$""")
    private val unreadableLine = Regex("""^([-dlcbps]\?{9}\S?)(?:\s+\?)+\s+(.+)$""")
    private val currentAndParent = setOf(".", "..")
    private const val LINK_ARROW = " -> "
    private const val PERMISSIONS = 1
    private const val SIZE = 2
    private const val MODIFIED = 3
    private const val NAME = 4

    fun parse(output: String): List<RemoteFileDto> = output.lineSequence()
        .mapNotNull { parseLine(it.trimEnd()) }
        .filterNot { it.name in currentAndParent }
        .toList()

    private fun parseLine(line: String): RemoteFileDto? = entryLine.find(line)?.let(::toEntry) ?: unreadableLine.find(line)?.let(::toUnreadableEntry)

    private fun toEntry(match: MatchResult): RemoteFileDto {
        val columns = match.groupValues
        val permissions = columns[PERMISSIONS]
        val nameColumn = columns[NAME]
        val isLink = permissions.startsWith('l')
        return RemoteFileDto(
            name = if (isLink) nameColumn.substringBefore(LINK_ARROW) else nameColumn,
            permissions = permissions,
            sizeBytes = columns[SIZE].toLongOrNull(),
            modified = columns[MODIFIED],
            linkTarget = if (isLink) nameColumn.substringAfter(LINK_ARROW, missingDelimiterValue = "").ifEmpty { null } else null,
        )
    }

    private fun toUnreadableEntry(match: MatchResult): RemoteFileDto {
        val (permissions, name) = match.destructured
        return RemoteFileDto(name = name, permissions = permissions, sizeBytes = null, modified = null)
    }
}
