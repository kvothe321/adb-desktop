package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDomain
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDto
import com.tlpcraft.adbdesktop.core.data.repository.util.Invalidations
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbAppAppearanceDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbPackageDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbPackageEventDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppAppearance
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import com.tlpcraft.adbdesktop.core.domain.repository.AppsRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppsRepositoryImpl(
    private val packageDataSource: AdbPackageDataSource,
    private val appearanceDataSource: AdbAppAppearanceDataSource,
    private val packageEventDataSource: AdbPackageEventDataSource,
    private val scope: CoroutineScope,
    private val pollInterval: Duration = 5.seconds,
) : AppsRepository {
    private val invalidations = Invalidations()

    /** One listener per device, shared by everyone watching its apps. */
    private val packageChanges = mutableMapOf<String, SharedFlow<Unit>>()
    private val packageChangesLock = Mutex()

    /** Names and icons rarely change, so each app's are read once per device while the app runs. `null` marks an app with neither. */
    private val appearanceCache = mutableMapOf<String, MutableMap<String, AppAppearance?>>()
    private val appearanceCacheLock = Mutex()

    override fun observeApps(serial: String, filter: AppFilter): Flow<Outcome<List<AppInfo>, DeviceError>> = invalidations.observe(packageChangesOf(serial)) {
        safeCall(Throwable::toDeviceError) { packageDataSource.listPackages(serial, filter.toDto()).map(::AppInfo) }
    }

    private fun packageChangesOf(serial: String): Flow<Unit> = flow {
        val shared = packageChangesLock.withLock {
            packageChanges.getOrPut(serial) { listenForPackageChanges(serial).shareIn(scope, SharingStarted.WhileSubscribed()) }
        }
        emitAll(shared)
    }

    /** When the device cannot report changes, the listener ends and checking every [pollInterval] takes over. */
    private fun listenForPackageChanges(serial: String): Flow<Unit> = flow {
        packageEventDataSource.streamPackageChanges(serial).catch { }.collect { emit(Unit) } // A failure is handled like the end.
        while (true) {
            delay(pollInterval)
            emit(Unit)
        }
    }

    /** Sizes in the details change without any package event, so they are checked every [pollInterval]. */
    override fun observeAppDetails(serial: String, packageName: String): Flow<Outcome<AppDetails, DeviceError>> = invalidations.observe(pollInterval) {
        safeCall(Throwable::toDeviceError) { packageDataSource.readPackageDetails(serial, packageName).toDomain() }
    }

    override suspend fun executeAction(serial: String, packageName: String, action: AppAction): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { packageDataSource.runAction(serial, packageName, action.toDto()) }.also { invalidations.invalidate() }

    override fun observeAppAppearances(serial: String, packageNames: List<String>): Flow<Map<String, AppAppearance>> = flow {
        suspend fun known() = appearanceCacheLock.withLock {
            appearanceCache[serial].orEmpty().let { cache -> packageNames.mapNotNull { name -> cache[name]?.let { name to it } }.toMap() }
        }

        emit(known())
        val missing = appearanceCacheLock.withLock { packageNames.filterNot { appearanceCache[serial]?.containsKey(it) == true } }
        if (missing.isEmpty()) return@flow
        var readSinceEmit = 0
        appearanceDataSource.streamAppearances(serial, missing)
            .catch { emit(known()) } // Names and icons only decorate: what could not be read keeps the package name and plain glyph.
            .collect { read ->
                val appearance = read.takeIf { it.label != null || it.icon != null }?.let { AppAppearance(it.label, it.icon) }
                appearanceCacheLock.withLock { appearanceCache.getOrPut(serial) { mutableMapOf() }[read.packageName] = appearance }
                if (++readSinceEmit == APPEARANCES_PER_EMISSION) {
                    readSinceEmit = 0
                    emit(known())
                }
            }
        emit(known())
    }.distinctUntilChanged { old, new -> old.keys == new.keys }

    override suspend fun install(serial: String, apkPaths: List<String>): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { packageDataSource.install(serial, apkPaths) }.also { invalidations.invalidate() }

    private companion object {
        const val APPEARANCES_PER_EMISSION = 20
    }
}
