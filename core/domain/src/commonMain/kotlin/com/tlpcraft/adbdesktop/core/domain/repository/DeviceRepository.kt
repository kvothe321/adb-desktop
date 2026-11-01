package com.tlpcraft.adbdesktop.core.domain.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.Device
import com.tlpcraft.adbdesktop.core.domain.model.WirelessEndpoint
import com.tlpcraft.adbdesktop.core.domain.model.WirelessService
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    /** Emits the full device list on every change. After a failure it keeps trying and recovers on its own. */
    fun observeDevices(): Flow<Outcome<List<Device>, DeviceError>>

    suspend fun connect(endpoint: WirelessEndpoint): Outcome<Unit, DeviceError>

    suspend fun disconnect(serial: String): Outcome<Unit, DeviceError>

    suspend fun pair(endpoint: WirelessEndpoint, pairingCode: String): Outcome<Unit, DeviceError>

    /** Emits the wireless devices announcing themselves on the network, on every change. */
    fun observeWirelessServices(): Flow<Outcome<List<WirelessService>, DeviceError>>
}
