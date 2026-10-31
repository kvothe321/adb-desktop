package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.util.asOutcomeFlow
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbShellDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.repository.ShellRepository
import kotlinx.coroutines.flow.Flow

class ShellRepositoryImpl(private val shellDataSource: AdbShellDataSource) : ShellRepository {

    override fun runSession(serial: String, commands: Flow<String>): Flow<Outcome<String, DeviceError>> = shellDataSource.openSession(serial, commands)
        .asOutcomeFlow(Throwable::toDeviceError)
}
