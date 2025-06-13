package com.tlpcraft.adbdesktop.data.repository

import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import kotlinx.coroutines.flow.Flow

/**
 * Interface for interacting with ADB devices.
 *
 * This interface defines methods to fetch connected ADB devices and perform operations related to them.
 */
interface DeviceRepository {

    /**
     * Fetches the list of connected ADB devices.
     *
     * @return A [Result] containing a [Flow] of a list of [AdbDevice] objects.
     */
    suspend fun getConnectedDevices(): Result<List<AdbDevice>>
}
