package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.domain.repository.AdbSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAdbSettingsRepository(initialPath: String? = null) : AdbSettingsRepository {
    val adbPath = MutableStateFlow(initialPath)

    override fun observeAdbPathOverride(): Flow<String?> = adbPath

    override suspend fun setAdbPathOverride(path: String?) {
        adbPath.value = path
    }
}
