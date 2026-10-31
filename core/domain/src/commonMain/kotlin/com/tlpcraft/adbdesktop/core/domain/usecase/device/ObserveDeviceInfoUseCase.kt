package com.tlpcraft.adbdesktop.core.domain.usecase.device

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.DeviceInfo
import com.tlpcraft.adbdesktop.core.domain.repository.DeviceInfoRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveDeviceInfoUseCase(
    private val repository: DeviceInfoRepository,
) : ReactiveUseCase<String, Flow<Outcome<DeviceInfo, DeviceError>>> {

    override fun invoke(param: String): Flow<Outcome<DeviceInfo, DeviceError>> = repository.observeDeviceInfo(param)
}
