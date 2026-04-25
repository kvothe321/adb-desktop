package com.tlpcraft.adbdesktop.core.data.datasource

import com.tlpcraft.adbdesktop.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.domain.service.DispatcherProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Desktop implementation of [CpuDataSource].
 *
 * Collects CPU stats from a connected Android device via three ADB shell commands:
 *
 * 1. **Usage** — two consecutive reads of `/proc/stat` separated by a 500 ms window.
 *    The delta between busy and total jiffies gives an accurate utilisation percentage
 *    without relying on the output format of `top`, which varies across Android versions.
 *
 * 2. **Core count** — `nproc` returns the number of available logical processors.
 *
 * 3. **Max frequency** — `/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq`
 *    exposes the hardware ceiling of cpu0 in kHz; divided by 1000 to produce MHz.
 *
 * All blocking [ProcessBuilder] I/O is confined to [DispatcherProvider.io].
 */
class CpuDataSourceImpl(
    private val dispatcherProvider: DispatcherProvider
) : CpuDataSource {

    override suspend fun getCpuStats(deviceSerial: String): CpuInfo = withContext(dispatcherProvider.io) {
        // ── CPU usage via /proc/stat delta ─────────────────────────────────────
        val snapshot1 = readProcStatSnapshot(deviceSerial)
        delay(STAT_SAMPLE_WINDOW_MS)
        val snapshot2 = readProcStatSnapshot(deviceSerial)

        val deltaBusy = snapshot2.busy - snapshot1.busy
        val deltaTotal = snapshot2.total - snapshot1.total
        val usagePercent = if (deltaTotal > 0) (deltaBusy.toFloat() / deltaTotal) * 100f else 0f

        // ── Core count ─────────────────────────────────────────────────────────
        val coreCount = runAdbShell(deviceSerial, "nproc").toIntOrNull() ?: 1

        // ── Max clock frequency ────────────────────────────────────────────────
        val maxFreqMHz =
            (
                runAdbShell(deviceSerial, "cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
                    .toLongOrNull() ?: 0L
            ) / 1_000L

        CpuInfo(
            usagePercent = usagePercent,
            coreCount = coreCount,
            maxFrequencyMHz = maxFreqMHz
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    /**
     * Reads the first `cpu` aggregate line from `/proc/stat` and returns it as a
     * [CpuStatSnapshot].  The line format is:
     * `cpu  <user> <nice> <system> <idle> <iowait> <irq> <softirq> <steal> ...`
     */
    private fun readProcStatSnapshot(deviceSerial: String): CpuStatSnapshot {
        val line = runAdbShell(deviceSerial, "cat /proc/stat")
            .lineSequence()
            .first { it.startsWith("cpu ") }
        val parts = line.trim().split("\\s+".toRegex())
        return CpuStatSnapshot(
            user = parts[1].toLong(),
            nice = parts[2].toLong(),
            system = parts[3].toLong(),
            idle = parts[4].toLong(),
            iowait = parts[5].toLong(),
            irq = parts[6].toLong(),
            softirq = parts[7].toLong(),
            steal = parts[8].toLong()
        )
    }

    /**
     * Runs a single `adb -s <serial> shell <command>` and returns the trimmed stdout.
     * This is a blocking call — it must only be invoked from [DispatcherProvider.io].
     */
    private fun runAdbShell(deviceSerial: String, command: String): String {
        val process = ProcessBuilder("adb", "-s", deviceSerial, "shell", command)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return output.trim()
    }

    private data class CpuStatSnapshot(
        val user: Long,
        val nice: Long,
        val system: Long,
        val idle: Long,
        val iowait: Long,
        val irq: Long,
        val softirq: Long,
        val steal: Long
    ) {
        /** Jiffies spent doing actual work (everything except idle and iowait). */
        val busy: Long get() = user + nice + system + irq + softirq + steal

        /** Total jiffies elapsed since boot. */
        val total: Long get() = busy + idle + iowait
    }

    private companion object {
        /** Gap between the two `/proc/stat` samples used to compute CPU delta. */
        const val STAT_SAMPLE_WINDOW_MS = 500L
    }
}
