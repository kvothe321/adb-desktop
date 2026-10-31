package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDomain
import com.tlpcraft.adbdesktop.core.data.repository.util.asOutcomeFlow
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDeviceDataSource
import com.tlpcraft.adbdesktop.core.data.source.preferences.PreferencesDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.Device
import com.tlpcraft.adbdesktop.core.domain.model.WirelessEndpoint
import com.tlpcraft.adbdesktop.core.domain.model.WirelessService
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class DeviceRepositoryImpl(
    private val deviceDataSource: AdbDeviceDataSource,
    private val preferencesDataSource: PreferencesDataSource,
) : DeviceRepository {

    /** Restarts tracking whenever the adb path changes, so fixing the path recovers without a restart. */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeDevices(): Flow<Outcome<List<Device>, DeviceError>> = preferencesDataSource.observeAdbPath()
        .distinctUntilChanged()
        .flatMapLatest {
            deviceDataSource.trackDevices()
                .map { devices -> devices.map { it.toDomain() } }
                .asOutcomeFlow(Throwable::toDeviceError)
        }

    override suspend fun connect(endpoint: WirelessEndpoint): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { deviceDataSource.connect(endpoint.toString()) }

    override suspend fun disconnect(serial: String): Outcome<Unit, DeviceError> = safeCall(Throwable::toDeviceError) { deviceDataSource.disconnect(serial) }

    override suspend fun pair(endpoint: WirelessEndpoint, pairingCode: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { deviceDataSource.pair(endpoint.toString(), pairingCode) }

    override suspend fun discoverWirelessServices(): Outcome<List<WirelessService>, DeviceError> =
        safeCall(Throwable::toDeviceError) { deviceDataSource.discoverServices().map { it.toDomain() } }
}
