package com.tlpcraft.adbdesktop.core.data.source.adb.parser

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.CpuSnapshotDto

/**
 * Parses the output of [SCRIPT]: the aggregate `/proc/stat` line, `nproc`, then every core's
 * `cpuinfo_max_freq` in kHz, with sections separated by [SECTION_SEPARATOR].
 */
internal object CpuSnapshotParser {
    const val SECTION_SEPARATOR = "---"
    const val SCRIPT = "grep '^cpu ' /proc/stat; echo $SECTION_SEPARATOR; nproc; echo $SECTION_SEPARATOR; " +
        "cat /sys/devices/system/cpu/cpu*/cpufreq/cpuinfo_max_freq 2>/dev/null"

    private const val STAT_COLUMNS = 8
    private const val IDLE_COLUMN = 3
    private const val IOWAIT_COLUMN = 4

    fun parse(output: String): CpuSnapshotDto {
        val sections = output.split(SECTION_SEPARATOR).map(String::trim)
        val jiffies = parseJiffies(sections.getOrNull(0).orEmpty())
        val idle = jiffies.getOrElse(IDLE_COLUMN) { 0 } + jiffies.getOrElse(IOWAIT_COLUMN) { 0 }
        val total = jiffies.sum()

        return CpuSnapshotDto(
            busyJiffies = total - idle,
            totalJiffies = total,
            coreCount = sections.getOrNull(1)?.toIntOrNull() ?: 1,
            maxFrequencyKHz = parseMaxFrequency(sections.getOrNull(2).orEmpty()),
        )
    }

    /** `cpu  user nice system idle iowait irq softirq steal ...`; guest columns are already counted in user. */
    private fun parseJiffies(line: String): List<Long> = line.split(Regex("\\s+"))
        .drop(1)
        .take(STAT_COLUMNS)
        .mapNotNull(String::toLongOrNull)

    private fun parseMaxFrequency(section: String): Long? = section.lineSequence()
        .mapNotNull { it.trim().toLongOrNull() }
        .filter { it > 0 }
        .maxOrNull()
}
