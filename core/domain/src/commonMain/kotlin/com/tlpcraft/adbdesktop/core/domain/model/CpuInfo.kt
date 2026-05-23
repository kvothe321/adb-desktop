package com.tlpcraft.adbdesktop.core.domain.model

/**
 * @property usagePercent overall usage between two consecutive samples, `0..100`.
 * @property maxFrequencyMHz highest clock frequency across cores, or `null` when the device hides it.
 */
data class CpuInfo(
    val usagePercent: Float,
    val coreCount: Int,
    val maxFrequencyMHz: Long?,
)
