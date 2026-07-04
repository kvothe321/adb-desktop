package com.tlpcraft.adbdesktop.core.data.repository.mapper

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.CpuSnapshotDto
import com.tlpcraft.adbdesktop.core.domain.model.CpuInfo

private const val PERCENT = 100f
private const val KHZ_PER_MHZ = 1_000L

/** Usage is the busy share of the jiffies elapsed since [previous]. */
internal fun CpuSnapshotDto.toCpuInfo(previous: CpuSnapshotDto): CpuInfo {
    val elapsed = totalJiffies - previous.totalJiffies
    val busy = busyJiffies - previous.busyJiffies
    val usage = if (elapsed > 0) busy.toFloat() / elapsed * PERCENT else 0f
    return CpuInfo(
        usagePercent = usage.coerceIn(0f, PERCENT),
        coreCount = coreCount,
        maxFrequencyMHz = maxFrequencyKHz?.div(KHZ_PER_MHZ),
    )
}
