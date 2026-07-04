package com.tlpcraft.adbdesktop.core.domain.usecase.device

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.WirelessEndpoint
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.UseCase

/** Pairs with a device showing "Pair device with pairing code". The pairing port is always explicit. */
class PairWirelessDeviceUseCase(
    private val repository: DeviceRepository,
) : UseCase<PairWirelessDeviceUseCase.Params, Outcome<Unit, DeviceError>> {

    data class Params(val endpoint: String, val pairingCode: String)

    override suspend fun invoke(param: Params): Outcome<Unit, DeviceError> {
        val endpoint = WirelessEndpoint.parse(param.endpoint)
            ?: return Outcome.Failure(DeviceError.InvalidEndpoint(param.endpoint))
        val code = param.pairingCode.trim()
        if (!code.isValidPairingCode()) return Outcome.Failure(DeviceError.InvalidPairingCode)

        return repository.pair(endpoint, code)
    }

    private fun String.isValidPairingCode(): Boolean = length == PAIRING_CODE_LENGTH && all(Char::isDigit)

    private companion object {
        const val PAIRING_CODE_LENGTH = 6
    }
}
