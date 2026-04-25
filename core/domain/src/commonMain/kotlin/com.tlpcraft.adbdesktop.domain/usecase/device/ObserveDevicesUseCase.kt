package com.tlpcraft.adbdesktop.domain.usecase.device

import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.domain.repository.AdbDeviceRepository
import com.tlpcraft.adbdesktop.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class ObserveDevicesUseCase(
    private val repository: AdbDeviceRepository
) : ReactiveUseCase<Unit, Flow<Outcome<List<AdbDevice>, DeviceError>>> {

    override fun invoke(param: Unit): Flow<Outcome<List<AdbDevice>, DeviceError>> = repository.observeDevices()
        .map<List<AdbDevice>, Outcome<List<AdbDevice>, DeviceError>> { Outcome.Success(it) }
        .catch { emit(Outcome.Failure(DeviceError.CommandFailed(it))) }
}
