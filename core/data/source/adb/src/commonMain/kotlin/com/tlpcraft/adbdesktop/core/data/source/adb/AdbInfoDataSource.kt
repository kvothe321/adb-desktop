package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.AdbInfoDto
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.AdbVersionParser

interface AdbInfoDataSource {
    suspend fun readInfo(): AdbInfoDto
}

class AdbInfoDataSourceImpl(private val client: AdbClient) : AdbInfoDataSource {

    override suspend fun readInfo(): AdbInfoDto = AdbInfoDto(
        path = client.executablePath(),
        version = AdbVersionParser.parse(client.run(listOf("version"))),
    )
}
