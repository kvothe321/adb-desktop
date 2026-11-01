package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDomain
import com.tlpcraft.adbdesktop.core.data.repository.util.Invalidations
import com.tlpcraft.adbdesktop.core.data.repository.util.asOutcomeFlow
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbDeviceDataSource
import com.tlpcraft.adbdesktop.core.data.source.preferences.PreferencesDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.Device
import com.tlpcraft.adbdesktop.core.domain.model.WirelessEndpoint
import com.tlpcraft.adbdesktop.core.domain.model.WirelessService
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class DeviceRepositoryImpl(
    private val deviceDataSource: AdbDeviceDataSource,
    private val preferencesDataSource: PreferencesDataSource,
    scope: CoroutineScope,
    private val retryInterval: Duration = 5.seconds,
    private val discoveryInterval: Duration = 3.seconds,
) : DeviceRepository {
    private val invalidations = Invalidations()

    /** One adb process serves every observer. Tracking restarts when the adb path changes, and retries when it ends. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val devices: Flow<Outcome<List<Device>, DeviceError>> = preferencesDataSource.observeAdbPath()
        .distinctUntilChanged()
        .flatMapLatest { trackDevices() }
        .distinctUntilChanged()
        .shareIn(scope, SharingStarted.WhileSubscribed(SHARING_TIMEOUT_MILLIS, replayExpirationMillis = 0), replay = 1)

    override fun observeDevices(): Flow<Outcome<List<Device>, DeviceError>> = devices

    private fun trackDevices(): Flow<Outcome<List<Device>, DeviceError>> = flow {
        while (true) {
            emitAll(deviceDataSource.trackDevices().map { devices -> devices.map { it.toDomain() } }.asOutcomeFlow(Throwable::toDeviceError))
            delay(retryInterval)
        }
    }

    override suspend fun connect(endpoint: WirelessEndpoint): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { deviceDataSource.connect(endpoint.toString()) }

    override suspend fun disconnect(serial: String): Outcome<Unit, DeviceError> = safeCall(Throwable::toDeviceError) { deviceDataSource.disconnect(serial) }

    override suspend fun pair(endpoint: WirelessEndpoint, pairingCode: String): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { deviceDataSource.pair(endpoint.toString(), pairingCode) }.also { invalidations.invalidate() }

    override fun observeWirelessServices(): Flow<Outcome<List<WirelessService>, DeviceError>> = invalidations.observe(discoveryInterval) {
        safeCall(Throwable::toDeviceError) { deviceDataSource.discoverServices().map { it.toDomain() } }
    }

    private companion object {
        const val SHARING_TIMEOUT_MILLIS = 5_000L
    }
}
