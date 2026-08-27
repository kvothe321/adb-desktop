package com.tlpcraft.adbdesktop.core.domain.usecase.shell

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.TerminalSize
import com.tlpcraft.adbdesktop.core.domain.repository.ShellRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class RunShellSessionUseCase(
    private val repository: ShellRepository,
) : ReactiveUseCase<RunShellSessionUseCase.Params, Flow<Outcome<String, DeviceError>>> {

    data class Params(val serial: String, val input: Flow<String>, val sizes: Flow<TerminalSize>)

    override fun invoke(param: Params): Flow<Outcome<String, DeviceError>> = repository.runSession(param.serial, param.input, param.sizes)
}
