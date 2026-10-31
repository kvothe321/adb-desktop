package com.tlpcraft.adbdesktop.core.data.source.adb.fake

import com.tlpcraft.adbdesktop.core.data.source.adb.binary.AdbEnvironment

class FakeAdbEnvironment(
    private val variables: Map<String, String> = emptyMap(),
    private val executables: Set<String> = emptySet(),
    override val executableName: String = "adb",
    override val pathSeparator: String = ":",
    override val fileSeparator: String = "/",
) : AdbEnvironment {
    override fun variable(name: String): String? = variables[name]

    override fun isExecutableFile(path: String): Boolean = path in executables
}
