package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AdbInfo
import com.tlpcraft.adbdesktop.core.domain.repository.AdbSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAdbSettingsRepository(initialPath: String? = null) : AdbSettingsRepository {
    val adbPath = MutableStateFlow(initialPath)
    val adbInfo = MutableStateFlow<Outcome<AdbInfo, DeviceError>>(Outcome.Success(AdbInfo("/usr/bin/adb", "37.0.0")))

    override fun observeAdbPathOverride(): Flow<String?> = adbPath

    override suspend fun setAdbPathOverride(path: String?) {
        adbPath.value = path
    }

    override fun observeAdbInfo(): Flow<Outcome<AdbInfo, DeviceError>> = adbInfo
}
