package com.tlpcraft.adbdesktop.core.domain.usecase.device

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.core.domain.repository.CpuRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveCpuInfoUseCase(
    private val repository: CpuRepository,
) : ReactiveUseCase<String, Flow<Outcome<CpuInfo, DeviceError>>> {

    override fun invoke(param: String): Flow<Outcome<CpuInfo, DeviceError>> = repository.observeCpuInfo(param)
}
