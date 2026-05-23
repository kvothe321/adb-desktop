package com.tlpcraft.adbdesktop.core.data.source.adb

import com.tlpcraft.adbdesktop.core.data.source.adb.client.AdbClient
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.DeviceDto
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.DeviceListParser
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.TrackDevicesDecoder
import com.tlpcraft.adbdesktop.core.data.source.adb.parser.WirelessOutputParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

class AdbDeviceDataSourceImpl(private val client: AdbClient) : AdbDeviceDataSource {

    override fun trackDevices(): Flow<List<DeviceDto>> = flow {
        val decoder = TrackDevicesDecoder()
        client.stream("track-devices", "-l").collect { chunk ->
            decoder.decode(chunk).forEach { listing -> emit(DeviceListParser.parse(listing)) }
        }
    }.distinctUntilChanged()

    override suspend fun connect(address: String) {
        val output = client.execute("connect", address).let { it.stdout + it.stderr }
        if (!WirelessOutputParser.isConnectSuccess(output)) {
            throw AdbException.ConnectFailed(address, WirelessOutputParser.failureReason(output))
        }
    }

    override suspend fun disconnect(serial: String) {
        val output = client.execute("disconnect", serial).let { it.stdout + it.stderr }
        if (!WirelessOutputParser.isDisconnectSuccess(output)) throw AdbException.DeviceNotFound(serial)
    }

    override suspend fun pair(address: String, pairingCode: String) {
        val output = client.execute("pair", address, pairingCode).let { it.stdout + it.stderr }
        if (!WirelessOutputParser.isPairSuccess(output)) throw AdbException.PairFailed(WirelessOutputParser.failureReason(output))
    }
}
