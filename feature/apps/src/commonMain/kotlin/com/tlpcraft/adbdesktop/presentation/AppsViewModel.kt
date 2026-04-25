package com.tlpcraft.adbdesktop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.domain.model.AdbDevice
import com.tlpcraft.adbdesktop.domain.model.AppFilter
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

@OptIn(ExperimentalCoroutinesApi::class)
class AppsViewModel(
    private val getApps: GetAppsUseCase
) : ViewModel() {

    private val selectedDevice = MutableStateFlow<AdbDevice?>(null)
    private val activeFilter = MutableStateFlow(AppFilter.ALL)
    private val searchQuery = MutableStateFlow("")

    /** Called from the screen whenever the shared selection changes. */
    fun updateSelectedDevice(device: AdbDevice?) {
        selectedDevice.value = device
    }

    val uiState: StateFlow<AppsUiState> = combine(
        selectedDevice,
        activeFilter
    ) { selectedDevice, filter ->
        selectedDevice to filter
    }
        .flatMapLatest { (selectedDevice, filter) ->
            if (selectedDevice == null || selectedDevice.status != "device") {
                flowOf<AppsUiState>(AppsUiState.NoDevice)
            } else {
                val serial = selectedDevice.serial
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
                                    searchQuery = searchQuery.value
                                )
                            },
                            onFailure = { error ->
                                AppsUiState.Error(
                                    deviceSerial = serial,
                                    activeFilter = filter,
                                    message = error.message
                                )
                            }
                        )
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
            initialValue = AppsUiState.Loading
        )

    fun onFilterSelected(filter: AppFilter) {
        activeFilter.value = filter
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.update { query }
    }
}
