package com.tlpcraft.adbdesktop.core.domain.model

/** [totalBytes] is `null` for folders, whose final size is not known up front. */
data class TransferProgress(val name: String, val transferredBytes: Long, val totalBytes: Long?) {
    val fraction: Float?
        get() = totalBytes?.takeIf { it > 0 }?.let { (transferredBytes.toFloat() / it).coerceIn(0f, 1f) }
}
