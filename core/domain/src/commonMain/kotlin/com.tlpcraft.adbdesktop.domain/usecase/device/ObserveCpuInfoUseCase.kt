package com.tlpcraft.adbdesktop.domain.usecase.device

import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.CpuInfo
import com.tlpcraft.adbdesktop.domain.repository.CpuRepository
import com.tlpcraft.adbdesktop.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

/**
 * Observes the CPU state of a single connected device identified by its ADB serial.
 *
 * Param is the device serial string (e.g. `"emulator-5554"` or `"R5CT10ABCDE"`).
 */
class ObserveCpuInfoUseCase(
    private val repository: CpuRepository
) : ReactiveUseCase<String, Flow<Outcome<CpuInfo, DeviceError>>> {

    override fun invoke(param: String): Flow<Outcome<CpuInfo, DeviceError>> = repository.observeCpuInfo(param)
}
