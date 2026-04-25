package com.tlpcraft.adbdesktop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.domain.model.AppAction
import com.tlpcraft.adbdesktop.domain.model.AppFilter
import com.tlpcraft.adbdesktop.domain.model.AppInfo
import com.tlpcraft.adbdesktop.domain.usecase.apps.ExecuteAppActionUseCase
import com.tlpcraft.adbdesktop.domain.usecase.apps.GetAppDetailsUseCase
import com.tlpcraft.adbdesktop.domain.usecase.apps.GetAppsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AppsViewModel(
    private val getApps: GetAppsUseCase,
    private val getAppDetails: GetAppDetailsUseCase,
    private val executeAppAction: ExecuteAppActionUseCase,
) : ViewModel() {

    private val selectedDevice = MutableStateFlow<AdbDevice?>(null)
    private val activeFilter = MutableStateFlow(AppFilter.ALL)
    private val searchQuery = MutableStateFlow("")
    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    private val _detailsRefresh = MutableStateFlow(0)

    val selectedApp: StateFlow<AppInfo?> = _selectedApp

    fun updateSelectedDevice(device: AdbDevice?) {
        selectedDevice.value = device
        _selectedApp.value = null
    }

    fun selectApp(app: AppInfo?) {
        _selectedApp.value = app
        _detailsRefresh.value = 0
    }

    fun onFilterSelected(filter: AppFilter) {
        activeFilter.value = filter
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.update { query }
    }

    // ── App list ──────────────────────────────────────────────────────────────

    val uiState: StateFlow<AppsUiState> = combine(selectedDevice, activeFilter) { device, filter ->
        device to filter
    }
        .flatMapLatest { (device, filter) ->
            if (device == null || device.status != "device") {
                flowOf<AppsUiState>(AppsUiState.NoDevice)
            } else {
                val serial = device.serial
                flow {
                    emit(AppsUiState.FetchingApps(serial, filter))
                    val result = getApps(GetAppsUseCase.Params(serial, filter))
                    emit(
                        result.fold(
                            onSuccess = { apps ->
                                AppsUiState.Content(
                                    deviceSerial = serial,
                                    activeFilter = filter,
                                    apps = apps,
                                    searchQuery = searchQuery.value,
                                )
                            },
                            onFailure = { error ->
                                AppsUiState.Error(
                                    deviceSerial = serial,
                                    activeFilter = filter,
                                    message = error.message,
                                )
                            },
                        ),
                    )
                }
            }
        }
        .combine(searchQuery) { state, query ->
            if (state is AppsUiState.Content) state.copy(searchQuery = query) else state
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppsUiState.Loading,
        )

    // ── App details ───────────────────────────────────────────────────────────

    val appDetailsState: StateFlow<AppDetailsState> = combine(
        selectedDevice,
        _selectedApp,
        _detailsRefresh,
    ) { device, app, _ -> device to app }
        .flatMapLatest { (device, app) ->
            if (device == null || app == null) return@flatMapLatest flowOf<AppDetailsState>(AppDetailsState.Empty)
            flow<AppDetailsState> {
                emit(AppDetailsState.Loading)
                val result = getAppDetails(GetAppDetailsUseCase.Params(device.serial, app.packageName))
                emit(
                    result.fold(
                        onSuccess = { AppDetailsState.Ready(it) },
                        onFailure = { AppDetailsState.Error(it.message) },
                    ),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppDetailsState.Empty,
        )

    // ── Actions ───────────────────────────────────────────────────────────────

    fun executeAction(action: AppAction) {
        val device = selectedDevice.value ?: return
        val app = _selectedApp.value ?: return
        viewModelScope.launch {
            val params = ExecuteAppActionUseCase.Params(device.serial, app.packageName, action)
            executeAppAction(params).onSuccess {
                when (action) {
                    AppAction.UNINSTALL -> {
                        _selectedApp.value = null
                        // Trigger list refresh by re-emitting current device
                        selectedDevice.value = selectedDevice.value
                    }
                    else -> _detailsRefresh.value++
                }
            }
        }
    }
}
