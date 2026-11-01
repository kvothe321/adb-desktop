package com.tlpcraft.adbdesktop.core.data.source.adb.binary

/** Host operating system facts needed to locate the adb executable. */
interface AdbEnvironment {
    val pathSeparator: String
    val fileSeparator: String
    val executableName: String

    fun variable(name: String): String?

    fun isExecutableFile(path: String): Boolean

    /** Size of a regular file, or `null` for folders and missing paths. */
    fun fileSize(path: String): Long?

    fun deleteFile(path: String)
}
