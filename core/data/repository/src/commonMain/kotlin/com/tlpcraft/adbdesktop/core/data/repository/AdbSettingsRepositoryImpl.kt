package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbInfoDataSource
import com.tlpcraft.adbdesktop.core.data.source.preferences.PreferencesDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AdbInfo
import com.tlpcraft.adbdesktop.core.domain.repository.AdbSettingsRepository
import kotlinx.coroutines.flow.Flow

class AdbSettingsRepositoryImpl(
    private val preferencesDataSource: PreferencesDataSource,
    private val infoDataSource: AdbInfoDataSource,
) : AdbSettingsRepository {

    override fun observeAdbPathOverride(): Flow<String?> = preferencesDataSource.observeAdbPath()

    override suspend fun setAdbPathOverride(path: String?) = preferencesDataSource.writeAdbPath(path)

    override suspend fun readAdbInfo(): Outcome<AdbInfo, DeviceError> = safeCall(Throwable::toDeviceError) {
        infoDataSource.readInfo().let { AdbInfo(it.path, it.version) }
    }
}
