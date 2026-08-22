package com.tlpcraft.adbdesktop.feature.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.common.flow.whileActive
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import com.tlpcraft.adbdesktop.core.domain.service.HostFiles
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.ExecuteAppActionUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.InstallApkUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.ObserveAppDetailsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.ObserveAppsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.feature.apps.contract.AppDetailsState
import com.tlpcraft.adbdesktop.feature.apps.contract.AppListState
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsIntent
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsSideEffect
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsUiState
import com.tlpcraft.adbdesktop.feature.apps.contract.isDestructive
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AppsViewModel(
    private val observeSelectedDevice: ObserveSelectedDeviceUseCase,
    private val observeApps: ObserveAppsUseCase,
    private val observeAppDetails: ObserveAppDetailsUseCase,
    private val executeAppAction: ExecuteAppActionUseCase,
    private val installApk: InstallApkUseCase,
    private val hostFiles: HostFiles,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<AppsSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<AppsSideEffect> = _sideEffects.receiveAsFlow()

    private val isVisible = MutableStateFlow(false)

    init {
        trackSelectedDevice()
        trackApps()
        trackAppDetails()
    }

    fun reduceIntent(intent: AppsIntent) {
        when (intent) {
            is AppsIntent.FilterSelected -> _uiState.update { it.copy(filter = intent.filter) }
            is AppsIntent.QueryChanged -> _uiState.update { it.copy(query = intent.value) }
            is AppsIntent.AppClicked -> _uiState.update { it.copy(selectedPackage = intent.packageName) }
            is AppsIntent.ActionClicked -> requestAction(intent.action)
            AppsIntent.ActionConfirmed -> _uiState.value.pendingConfirmation?.let(::runAction)
            AppsIntent.ActionCancelled -> _uiState.update { it.copy(pendingConfirmation = null) }
            is AppsIntent.VisibilityChanged -> isVisible.value = intent.isVisible
            AppsIntent.InstallClicked -> viewModelScope.launch { install(hostFiles.pickFiles(APK_EXTENSION)) }
            is AppsIntent.FilesDropped -> viewModelScope.launch { installDropped(intent.paths) }
        }
    }

    private fun trackSelectedDevice() = viewModelScope.launch {
        observeSelectedDevice(Unit).map { it?.serial }.distinctUntilChanged().collect { serial ->
            _uiState.update { it.copy(serial = serial, selectedPackage = null, pendingConfirmation = null) }
        }
    }

    private fun trackApps() = viewModelScope.launch {
        _uiState.map { it.serial to it.filter }.distinctUntilChanged().flatMapLatest { (serial, filter) ->
            if (serial == null) {
                flowOf(AppListState.NoDevice)
            } else {
                observeApps(ObserveAppsUseCase.Params(serial, filter))
                    .map { outcome -> outcome.toListState() }
                    .whileActive(isVisible)
                    .onStart { emit(AppListState.Loading) }
            }
        }.collect { list -> _uiState.update { it.copy(list = list) } }
    }

    private fun trackAppDetails() = viewModelScope.launch {
        _uiState.map { it.serial to it.selectedPackage }.distinctUntilChanged().flatMapLatest { (serial, packageName) ->
            if (serial == null || packageName == null) {
                flowOf(AppDetailsState.Hidden)
            } else {
                observeAppDetails(ObserveAppDetailsUseCase.Params(serial, packageName))
                    .map { outcome -> outcome.toDetailsState() }
                    .whileActive(isVisible)
                    .onStart { emit(AppDetailsState.Loading) }
            }
        }.collect { details -> _uiState.update { it.copy(details = details) } }
    }

    private suspend fun installDropped(paths: List<String>) {
        val apkPaths = paths.filter { it.endsWith(".$APK_EXTENSION", ignoreCase = true) }
        if (apkPaths.isEmpty()) _sideEffects.send(AppsSideEffect.NoApkDropped) else install(apkPaths)
    }

    private suspend fun install(apkPaths: List<String>) {
        val state = _uiState.value
        val serial = state.serial ?: return
        if (state.isInstalling || apkPaths.isEmpty()) return

        _uiState.update { it.copy(isInstalling = true) }
        val result = installApk(InstallApkUseCase.Params(serial, apkPaths))
        _uiState.update { it.copy(isInstalling = false) }
        when (result) {
            is Outcome.Success -> _sideEffects.send(AppsSideEffect.Installed(apkPaths.size))
            is Outcome.Failure -> _sideEffects.send(AppsSideEffect.InstallFailed(result.error.detailsForUser()))
        }
    }

    private fun DeviceError.detailsForUser(): String = (this as? DeviceError.CommandFailed)?.details ?: message

    private fun requestAction(action: AppAction) {
        if (action.isDestructive) {
            _uiState.update { it.copy(pendingConfirmation = action) }
        } else {
            runAction(action)
        }
    }

    private fun runAction(action: AppAction) {
        val (serial, packageName) = _uiState.value.actionTarget() ?: return

        _uiState.update { it.copy(pendingConfirmation = null, runningAction = action) }
        viewModelScope.launch {
            val result = executeAppAction(ExecuteAppActionUseCase.Params(serial, packageName, action))
            _uiState.update { it.copy(runningAction = null) }
            when (result) {
                is Outcome.Success -> onActionSucceeded(action, packageName)
                is Outcome.Failure -> _sideEffects.send(AppsSideEffect.ActionFailed(action, packageName, result.error.detailsForUser()))
            }
        }
    }

    private suspend fun onActionSucceeded(action: AppAction, packageName: String) {
        if (action == AppAction.UNINSTALL) _uiState.update { it.copy(selectedPackage = null) }
        _sideEffects.send(AppsSideEffect.ActionSucceeded(action, packageName))
    }
}

private const val APK_EXTENSION = "apk"

private fun Outcome<List<AppInfo>, DeviceError>.toListState(): AppListState = when (this) {
    is Outcome.Success -> AppListState.Loaded(data.map { it.packageName })
    is Outcome.Failure -> AppListState.Failed(error.message)
}

private fun Outcome<AppDetails, DeviceError>.toDetailsState(): AppDetailsState = when (this) {
    is Outcome.Success -> AppDetailsState.Loaded(data)
    is Outcome.Failure -> AppDetailsState.Failed(error.message)
}

/** Device serial and package an action applies to, or `null` while no app is selected or an action is running. */
private fun AppsUiState.actionTarget(): Pair<String, String>? {
    if (runningAction != null) return null
    val targetSerial = serial ?: return null
    val targetPackage = selectedPackage ?: return null
    return targetSerial to targetPackage
}
