package com.tlpcraft.adbdesktop.core.domain.usecase.device

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.WirelessEndpoint
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

/** Connects to `host[:port]`, using the default adb port when none is given. */
class ConnectWirelessDeviceUseCase(
    private val repository: DeviceRepository,
) : UseCase<String, Outcome<Unit, DeviceError>> {

    override suspend fun invoke(param: String): Outcome<Unit, DeviceError> {
        val endpoint = WirelessEndpoint.parse(param, defaultPort = WirelessEndpoint.DEFAULT_CONNECT_PORT)
            ?: return Outcome.Failure(DeviceError.InvalidEndpoint(param))
        return repository.connect(endpoint)
    }
}
