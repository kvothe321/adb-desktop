package com.tlpcraft.adbdesktop.domain.usecase.device

import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.DeviceInfo
import com.tlpcraft.adbdesktop.domain.repository.DeviceInfoRepository
import com.tlpcraft.adbdesktop.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveDeviceInfoUseCase(
    private val repository: DeviceInfoRepository,
) : ReactiveUseCase<String, Flow<Outcome<DeviceInfo, DeviceError>>> {

    override fun invoke(param: String): Flow<Outcome<DeviceInfo, DeviceError>> = repository.observeDeviceInfo(param)
}
