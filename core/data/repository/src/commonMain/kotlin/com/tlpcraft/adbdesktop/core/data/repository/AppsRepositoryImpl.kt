package com.tlpcraft.adbdesktop.core.data.repository

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDeviceError
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDomain
import com.tlpcraft.adbdesktop.core.data.repository.mapper.toDto
import com.tlpcraft.adbdesktop.core.data.repository.util.Invalidations
import com.tlpcraft.adbdesktop.core.data.repository.util.safeCall
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbAppIconDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbPackageDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.AdbPackageEventDataSource
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppIcon
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
    private val iconDataSource: AdbAppIconDataSource,
    private val packageEventDataSource: AdbPackageEventDataSource,
    private val scope: CoroutineScope,
    private val pollInterval: Duration = 5.seconds,
) : AppsRepository {
    private val invalidations = Invalidations()

    /** One listener per device, shared by everyone watching its apps. */
    private val packageChanges = mutableMapOf<String, SharedFlow<Unit>>()
    private val packageChangesLock = Mutex()

    /** Icons rarely change, so each is read once per device and app while the app runs. `null` marks one without an icon. */
    private val iconCache = mutableMapOf<String, MutableMap<String, AppIcon?>>()
    private val iconCacheLock = Mutex()

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

    override fun observeAppIcons(serial: String, packageNames: List<String>): Flow<Map<String, AppIcon>> = flow {
        suspend fun known() = iconCacheLock.withLock {
            iconCache[serial].orEmpty().let { cache -> packageNames.mapNotNull { name -> cache[name]?.let { name to it } }.toMap() }
        }

        emit(known())
        val missing = iconCacheLock.withLock { packageNames.filterNot { iconCache[serial]?.containsKey(it) == true } }
        if (missing.isEmpty()) return@flow
        var readSinceEmit = 0
        iconDataSource.streamIcons(serial, missing)
            .catch { emit(known()) } // Icons only decorate: what could not be read keeps the plain app glyph.
            .collect { (packageName, png) ->
                iconCacheLock.withLock { iconCache.getOrPut(serial) { mutableMapOf() }[packageName] = png?.let(::AppIcon) }
                if (++readSinceEmit == ICONS_PER_EMISSION) {
                    readSinceEmit = 0
                    emit(known())
                }
            }
        emit(known())
    }.distinctUntilChanged { old, new -> old.keys == new.keys }

    override suspend fun install(serial: String, apkPaths: List<String>): Outcome<Unit, DeviceError> =
        safeCall(Throwable::toDeviceError) { packageDataSource.install(serial, apkPaths) }.also { invalidations.invalidate() }

    private companion object {
        const val ICONS_PER_EMISSION = 20
    }
}
