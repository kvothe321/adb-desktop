package com.tlpcraft.adbdesktop.core.data.source.adb.dto

/** Cumulative CPU jiffies since boot plus static CPU facts, read in a single shell round trip. */
data class CpuSnapshotDto(
    val busyJiffies: Long,
    val totalJiffies: Long,
    val coreCount: Int,
    val maxFrequencyKHz: Long?,
)
