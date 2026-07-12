package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AdbShellDataSource {
    /** Runs one shell on the device: each [commands] value is a line of input, the result is its merged output. */
    fun openSession(serial: String, commands: Flow<String>): Flow<String>
}

class AdbShellDataSourceImpl(private val client: AdbClient) : AdbShellDataSource {

    override fun openSession(serial: String, commands: Flow<String>): Flow<String> = client.interact(commands.map { "$it\n" }, "-s", serial, "shell")
}
