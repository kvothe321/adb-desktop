package com.tlpcraft.adbdesktop.core.data.source.adb.binary

import java.io.File

class JvmAdbEnvironment : AdbEnvironment {
    private val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)

    override val pathSeparator: String = File.pathSeparator
    override val fileSeparator: String = File.separator
    override val executableName: String = if (isWindows) "adb.exe" else "adb"

    override fun variable(name: String): String? = System.getenv(name)?.takeIf(String::isNotBlank)

    override fun isExecutableFile(path: String): Boolean = File(path).let { it.isFile && it.canExecute() }

    override fun fileSize(path: String): Long? = File(path).takeIf(File::isFile)?.length()

    override fun deleteFile(path: String) {
        File(path).delete()
    }
}
