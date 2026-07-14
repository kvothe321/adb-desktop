package com.tlpcraft.adbdesktop.feature.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.common.getOrNull
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.Device
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ConnectWirelessDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.DisconnectDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.DiscoverWirelessDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveCpuInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDeviceInfoUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveDevicesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.PairWirelessDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.SelectDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.settings.SetAdbPathOverrideUseCase
import com.tlpcraft.adbdesktop.feature.devices.contract.ConnectFormError
import com.tlpcraft.adbdesktop.feature.devices.contract.ConnectFormState
import com.tlpcraft.adbdesktop.feature.devices.contract.ConnectMode
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceItem
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceListState
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceStats
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesFailure
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesIntent
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesSideEffect
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DevicesViewModel(
    private val observeDevices: ObserveDevicesUseCase,
    private val observeSelectedDevice: ObserveSelectedDeviceUseCase,
    private val observeDeviceInfo: ObserveDeviceInfoUseCase,
    private val observeCpuInfo: ObserveCpuInfoUseCase,
    private val selectDevice: SelectDeviceUseCase,
    private val connectWirelessDevice: ConnectWirelessDeviceUseCase,
    private val pairWirelessDevice: PairWirelessDeviceUseCase,
    private val disconnectDevice: DisconnectDeviceUseCase,
    private val discoverWirelessDevices: DiscoverWirelessDevicesUseCase,
    private val setAdbPathOverride: SetAdbPathOverrideUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState: StateFlow<DevicesUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<DevicesSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<DevicesSideEffect> = _sideEffects.receiveAsFlow()

    private val trackingRequests = MutableStateFlow(0)

    init {
        trackDevices()
        trackSelection()
        trackStatsOfOnlineDevices()
    }

    fun reduceIntent(intent: DevicesIntent) {
        when (intent) {
            DevicesIntent.RetryClicked -> trackingRequests.update { it + 1 }
            is DevicesIntent.DeviceClicked -> toggleSelection(intent.serial)
            is DevicesIntent.DisconnectClicked -> disconnect(intent.serial)
            is DevicesIntent.AdbPathChanged -> _uiState.update { it.copy(adbPathInput = intent.value) }
            DevicesIntent.AdbPathSubmitted -> saveAdbPath()
            DevicesIntent.ConnectFormOpened -> openConnectForm()
            DevicesIntent.ConnectFormDismissed -> _uiState.update { it.copy(connectForm = null) }
            is DevicesIntent.ConnectModeSelected -> updateForm { it.copy(mode = intent.mode, error = null) }
            is DevicesIntent.AddressChanged -> updateForm { it.copy(address = intent.value, error = null) }
            is DevicesIntent.PairingCodeChanged -> updateForm { it.copy(pairingCode = intent.value, error = null) }
            DevicesIntent.ConnectSubmitted -> submitConnectForm()
        }
    }

    private fun openConnectForm() {
        _uiState.update { it.copy(connectForm = ConnectFormState()) }
        viewModelScope.launch {
            discoverWirelessDevices(Unit).getOrNull()?.let { services -> updateForm { it.copy(discovered = services) } }
        }
    }

    private fun trackDevices() = viewModelScope.launch {
        trackingRequests.flatMapLatest { observeDevices(Unit) }.collect { outcome ->
            _uiState.update { it.copy(list = outcome.toListState()) }
        }
    }

    private fun trackSelection() = viewModelScope.launch {
        observeSelectedDevice(Unit).collect { device -> _uiState.update { it.copy(selectedSerial = device?.serial) } }
    }

    private fun trackStatsOfOnlineDevices() = viewModelScope.launch {
        _uiState.map { it.onlineSerials() }.distinctUntilChanged().collectLatest { serials ->
            _uiState.update { state -> state.copy(stats = state.stats.filterKeys { it in serials }) }
            coroutineScope { serials.forEach { serial -> launchStatsCollection(serial) } }
        }
    }

    private fun CoroutineScope.launchStatsCollection(serial: String) {
        launch {
            observeDeviceInfo(serial).collect { outcome ->
                outcome.getOrNull()?.let { info ->
                    updateStats(serial) { it.copy(androidVersion = info.androidVersion, batteryLevel = info.batteryLevel) }
                }
            }
        }
        launch {
            observeCpuInfo(serial).collect { outcome ->
                outcome.getOrNull()?.let { cpu -> updateStats(serial) { it.copy(cpuUsagePercent = cpu.usagePercent) } }
            }
        }
    }

    private fun toggleSelection(serial: String) {
        val state = _uiState.value
        val device = state.loadedDevices().firstOrNull { it.serial == serial } ?: return
        if (!device.isOnline) return
        viewModelScope.launch { selectDevice(serial.takeUnless { state.selectedSerial == serial }) }
    }

    private fun disconnect(serial: String) = viewModelScope.launch {
        val effect = when (disconnectDevice(serial)) {
            is Outcome.Success -> DevicesSideEffect.Disconnected(serial)
            is Outcome.Failure -> DevicesSideEffect.DisconnectFailed(serial)
        }
        _sideEffects.send(effect)
    }

    private fun saveAdbPath() = viewModelScope.launch {
        setAdbPathOverride(_uiState.value.adbPathInput)
    }

    private fun submitConnectForm() {
        val form = _uiState.value.connectForm ?: return
        if (!form.canSubmit) return
        updateForm { it.copy(inProgress = true, error = null) }

        viewModelScope.launch {
            val result = when (form.mode) {
                ConnectMode.Connect -> connectWirelessDevice(form.address)
                ConnectMode.Pair -> pairWirelessDevice(PairWirelessDeviceUseCase.Params(form.address, form.pairingCode))
            }
            when (result) {
                is Outcome.Success -> {
                    _uiState.update { it.copy(connectForm = null) }
                    _sideEffects.send(form.successEffect())
                }

                is Outcome.Failure -> {
                    updateForm { it.copy(inProgress = false, error = result.error.toFormError()) }
                }
            }
        }
    }

    private fun updateForm(transform: (ConnectFormState) -> ConnectFormState) {
        _uiState.update { state -> state.copy(connectForm = state.connectForm?.let(transform)) }
    }

    private fun updateStats(serial: String, transform: (DeviceStats) -> DeviceStats) {
        _uiState.update { state -> state.copy(stats = state.stats + (serial to transform(state.stats[serial] ?: DeviceStats()))) }
    }
}

private fun DevicesUiState.loadedDevices(): List<DeviceItem> = (list as? DeviceListState.Loaded)?.devices.orEmpty()

private fun DevicesUiState.onlineSerials(): Set<String> = loadedDevices().filter { it.isOnline }.map { it.serial }.toSet()

private fun ConnectFormState.successEffect(): DevicesSideEffect = when (mode) {
    ConnectMode.Connect -> DevicesSideEffect.Connected(address.trim())
    ConnectMode.Pair -> DevicesSideEffect.Paired(address.trim())
}

private fun Outcome<List<Device>, DeviceError>.toListState(): DeviceListState = when (this) {
    is Outcome.Success -> DeviceListState.Loaded(data.map { it.toItem() })
    is Outcome.Failure -> DeviceListState.Failed(error.toFailure())
}

private fun Device.toItem() = DeviceItem(serial = serial, name = model, state = state, connectionType = connectionType)

private fun DeviceError.toFailure(): DevicesFailure = when (this) {
    is DeviceError.AdbNotFound -> DevicesFailure.AdbNotFound(searchedLocations)
    is DeviceError.InvalidAdbPath -> DevicesFailure.InvalidAdbPath(path)
    else -> DevicesFailure.Unavailable(message)
}

private fun DeviceError.toFormError(): ConnectFormError = when (this) {
    is DeviceError.InvalidEndpoint -> ConnectFormError.InvalidAddress
    DeviceError.InvalidPairingCode -> ConnectFormError.InvalidPairingCode
    is DeviceError.ConnectionFailed -> ConnectFormError.Failed(reason)
    is DeviceError.PairingFailed -> ConnectFormError.Failed(reason)
    else -> ConnectFormError.Failed(message)
}
