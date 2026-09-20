package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.Device
import com.tlpcraft.adbdesktop.core.domain.model.WirelessEndpoint
import com.tlpcraft.adbdesktop.core.domain.model.WirelessService
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDeviceRepository(initialDevices: List<Device> = emptyList()) : DeviceRepository {
    val devices = MutableStateFlow<Outcome<List<Device>, DeviceError>>(Outcome.Success(initialDevices))
    var connectResult: Outcome<Unit, DeviceError> = Outcome.Success(Unit)
    var disconnectResult: Outcome<Unit, DeviceError> = Outcome.Success(Unit)
    var pairResult: Outcome<Unit, DeviceError> = Outcome.Success(Unit)

    val discoveredServices = MutableStateFlow<List<WirelessService>>(emptyList())

    val connectedEndpoints = mutableListOf<WirelessEndpoint>()
    val disconnectedSerials = mutableListOf<String>()
    val pairings = mutableListOf<Pair<WirelessEndpoint, String>>()

    override fun observeDevices(): Flow<Outcome<List<Device>, DeviceError>> = devices

    override suspend fun connect(endpoint: WirelessEndpoint): Outcome<Unit, DeviceError> {
        connectedEndpoints += endpoint
        return connectResult
    }

    override suspend fun disconnect(serial: String): Outcome<Unit, DeviceError> {
        disconnectedSerials += serial
        return disconnectResult
    }

    override suspend fun pair(endpoint: WirelessEndpoint, pairingCode: String): Outcome<Unit, DeviceError> {
        pairings += endpoint to pairingCode
        return pairResult
    }

    override fun observeWirelessServices(): Flow<Outcome<List<WirelessService>, DeviceError>> = discoveredServices.map { Outcome.Success(it) }
}
