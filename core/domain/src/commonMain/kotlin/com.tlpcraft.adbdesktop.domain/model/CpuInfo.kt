package com.tlpcraft.adbdesktop.domain.model

/**
 * Represents a point-in-time snapshot of a connected device's CPU state.
 *
 * @property usagePercent Overall CPU usage as a percentage (0.0–100.0), computed from
 * the delta between two consecutive `/proc/stat` readings.
 * @property coreCount Number of logical CPU cores reported by the device (`nproc`).
 * @property maxFrequencyMHz Maximum clock frequency of cpu0, in MHz, read from
 * `/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq`.
 */
data class CpuInfo(
    val usagePercent: Float,
    val coreCount: Int,
    val maxFrequencyMHz: Long,
)
