package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.util.Invalidations
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbInfoDataSource
import com.tlpcraft.adbdesktop.core.data.source.preferences.PreferencesDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AdbInfo
import com.tlpcraft.adbdesktop.core.domain.repository.AdbSettingsRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest

class AdbSettingsRepositoryImpl(
    private val preferencesDataSource: PreferencesDataSource,
    private val infoDataSource: AdbInfoDataSource,
    private val pollInterval: Duration = 10.seconds,
) : AdbSettingsRepository {
    private val invalidations = Invalidations()

    override fun observeAdbPathOverride(): Flow<String?> = preferencesDataSource.observeAdbPath()

    override suspend fun setAdbPathOverride(path: String?) = preferencesDataSource.writeAdbPath(path)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAdbInfo(): Flow<Outcome<AdbInfo, DeviceError>> = preferencesDataSource.observeAdbPath()
        .distinctUntilChanged()
        .flatMapLatest {
            invalidations.observe(pollInterval) {
                safeCall(Throwable::toDeviceError) { infoDataSource.readInfo().let { AdbInfo(it.path, it.version) } }
            }
        }
}
