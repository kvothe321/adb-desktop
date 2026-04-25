package com.tlpcraft.adbdesktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.domain.Outcome
import com.tlpcraft.adbdesktop.domain.error.DeviceError
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.domain.usecase.device.ObserveDevicesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/**
 * App-scoped ViewModel that owns two orthogonal concerns that need to be shared
 * across every screen in ADB Desktop:
 *
 * 1. **Device list** — continuously polls `adb devices` and exposes the latest
 *    [Outcome] so any screen can react to devices appearing or disappearing.
 *
 * 2. **Device selection** — tracks the device the user has explicitly chosen to
 *    work with (e.g. browse its installed packages). The selection is automatically
 *    cleared whenever the selected device goes offline or disappears from the list,
 *    so downstream screens never operate on a stale serial.
 *
 * This ViewModel is created once in [AppShell] and its state is passed down to
 * child screens as plain parameters, keeping feature modules free from any
 * dependency on this class.
 */
class SharedDeviceViewModel(
    observeDevices: ObserveDevicesUseCase
) : ViewModel() {

    private val _selectedDevice = MutableStateFlow<AdbDevice?>(null)

    /** The device currently selected by the user, or `null` when nothing is selected. */
    val selectedDevice: StateFlow<AdbDevice?> = _selectedDevice.asStateFlow()

    /**
     * The latest result of `adb devices`.
     *
     * - `null`                   → first poll not yet completed (loading).
     * - [Outcome.Failure]        → ADB command failed (daemon not running, etc.).
     * - [Outcome.Success]        → list of known devices (may be empty).
     */
    val devicesState: StateFlow<Outcome<List<AdbDevice>, DeviceError>?> =
        observeDevices(Unit)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    init {
        // Auto-deselect whenever the selected device goes offline or disappears.
        devicesState
            .onEach { outcome ->
                val current = _selectedDevice.value ?: return@onEach
                val devices = outcome?.fold(onSuccess = { it }, onFailure = { emptyList() })
                    ?: return@onEach

                val updated = devices.firstOrNull { it.serial == current.serial }
                when {
                    updated == null -> deselect() // device unplugged
                    updated.status != "device" -> deselect() // device went offline / unauthorized
                    else -> _selectedDevice.value = updated // keep reference fresh
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Marks the device with [serial] as the active selection.
     * No-op if the serial is not in the current list or the device is not online.
     */
    fun select(serial: String) {
        val devices = devicesState.value
            ?.fold(onSuccess = { it }, onFailure = { emptyList() })
            ?: return
        val device = devices.firstOrNull { it.serial == serial && it.status == "device" } ?: return
        _selectedDevice.value = device
    }

    /** Clears the current selection. */
    fun deselect() {
        _selectedDevice.value = null
    }
}
