package com.tlpcraft.adbdesktop.core.domain.model

data class RemoteFile(
    val name: String,
    val path: String,
    val type: RemoteFileType,
    val sizeBytes: Long?,
    val modified: String?,
    val permissions: String,
    val linkTarget: String? = null,
)

enum class RemoteFileType {
    DIRECTORY,
    LINK,
    FILE,
    OTHER,
    ;

    val canOpen: Boolean
        get() = this == DIRECTORY || this == LINK
}

/** Absolute device paths use `/` on every host. */
object RemotePath {
    const val ROOT = "/"
    private const val SEPARATOR = '/'

    fun child(directory: String, name: String): String = directory.trimEnd(SEPARATOR) + SEPARATOR + name

    fun parent(path: String): String = path.trimEnd(SEPARATOR).substringBeforeLast(SEPARATOR, missingDelimiterValue = "").ifEmpty { ROOT }
}
