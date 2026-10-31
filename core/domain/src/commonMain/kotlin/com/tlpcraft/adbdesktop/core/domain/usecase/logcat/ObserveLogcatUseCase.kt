package com.tlpcraft.adbdesktop.core.domain.usecase.logcat

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.LogEntry
import com.tlpcraft.adbdesktop.core.domain.repository.LogcatRepository
import com.tlpcraft.adbdesktop.core.domain.usecase.ReactiveUseCase
import kotlinx.coroutines.flow.Flow

class ObserveLogcatUseCase(
    private val repository: LogcatRepository,
) : ReactiveUseCase<String, Flow<Outcome<List<LogEntry>, DeviceError>>> {

    override fun invoke(param: String): Flow<Outcome<List<LogEntry>, DeviceError>> = repository.observeLogs(param)
}
