package com.tlpcraft.adbdesktop.feature.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.service.HostFiles
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.ExecuteAppActionUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.GetAppDetailsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.GetAppsUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.apps.InstallApkUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.feature.apps.contract.AppDetailsState
import com.tlpcraft.adbdesktop.feature.apps.contract.AppListState
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsIntent
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsSideEffect
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsUiState
import com.tlpcraft.adbdesktop.feature.apps.contract.isDestructive
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppsViewModel(
    private val observeSelectedDevice: ObserveSelectedDeviceUseCase,
    private val getApps: GetAppsUseCase,
    private val getAppDetails: GetAppDetailsUseCase,
    private val executeAppAction: ExecuteAppActionUseCase,
    private val installApk: InstallApkUseCase,
    private val hostFiles: HostFiles,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<AppsSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<AppsSideEffect> = _sideEffects.receiveAsFlow()

    private val reloadRequests = MutableStateFlow(0)
    private var detailsJob: Job? = null

    init {
        trackSelectedDevice()
        loadAppsOnChange()
    }

    fun reduceIntent(intent: AppsIntent) {
        when (intent) {
            is AppsIntent.FilterSelected -> _uiState.update { it.copy(filter = intent.filter) }
            is AppsIntent.QueryChanged -> _uiState.update { it.copy(query = intent.value) }
            is AppsIntent.AppClicked -> selectApp(intent.packageName)
            is AppsIntent.ActionClicked -> requestAction(intent.action)
            AppsIntent.ActionConfirmed -> _uiState.value.pendingConfirmation?.let(::runAction)
            AppsIntent.ActionCancelled -> _uiState.update { it.copy(pendingConfirmation = null) }
            AppsIntent.RetryClicked -> reloadRequests.update { it + 1 }
            AppsIntent.InstallClicked -> viewModelScope.launch { install(hostFiles.pickFiles(APK_EXTENSION)) }
            is AppsIntent.FilesDropped -> viewModelScope.launch { installDropped(intent.paths) }
        }
    }

    private fun trackSelectedDevice() = viewModelScope.launch {
        observeSelectedDevice(Unit).map { it?.serial }.distinctUntilChanged().collect { serial ->
            detailsJob?.cancel()
            _uiState.update { it.copy(serial = serial, selectedPackage = null, details = AppDetailsState.Hidden, pendingConfirmation = null) }
        }
    }

    private fun loadAppsOnChange() = viewModelScope.launch {
        combine(
            _uiState.map { it.serial to it.filter }.distinctUntilChanged(),
            reloadRequests,
        ) { request, _ -> request }.collectLatest { (serial, filter) -> loadApps(serial, filter) }
    }

    private suspend fun loadApps(serial: String?, filter: AppFilter) {
        if (serial == null) {
            _uiState.update { it.copy(list = AppListState.NoDevice) }
            return
        }
        _uiState.update { it.copy(list = AppListState.Loading) }
        val list = when (val result = getApps(GetAppsUseCase.Params(serial, filter))) {
            is Outcome.Success -> AppListState.Loaded(result.data.map { it.packageName })
            is Outcome.Failure -> AppListState.Failed(result.error.message)
        }
        _uiState.update { it.copy(list = list) }
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
            is Outcome.Success -> {
                reloadRequests.update { it + 1 }
                _sideEffects.send(AppsSideEffect.Installed(apkPaths.size))
            }

            is Outcome.Failure -> {
                _sideEffects.send(AppsSideEffect.InstallFailed(result.error.detailsForUser()))
            }
        }
    }

    private fun DeviceError.detailsForUser(): String = (this as? DeviceError.CommandFailed)?.details ?: message

    private fun selectApp(packageName: String) {
        _uiState.update { it.copy(selectedPackage = packageName) }
        loadDetails()
    }

    private fun loadDetails() {
        val state = _uiState.value
        val serial = state.serial ?: return
        val packageName = state.selectedPackage ?: return

        detailsJob?.cancel()
        detailsJob = viewModelScope.launch {
            _uiState.update { it.copy(details = AppDetailsState.Loading) }
            val details = when (val result = getAppDetails(GetAppDetailsUseCase.Params(serial, packageName))) {
                is Outcome.Success -> AppDetailsState.Loaded(result.data)
                is Outcome.Failure -> AppDetailsState.Failed(result.error.message)
            }
            _uiState.update { it.copy(details = details) }
        }
    }

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
        if (action == AppAction.UNINSTALL) {
            _uiState.update { it.copy(selectedPackage = null, details = AppDetailsState.Hidden) }
            reloadRequests.update { it + 1 }
        } else {
            loadDetails()
        }
        _sideEffects.send(AppsSideEffect.ActionSucceeded(action, packageName))
    }
}

private const val APK_EXTENSION = "apk"

/** Device serial and package an action applies to, or `null` while no app is selected or an action is running. */
private fun AppsUiState.actionTarget(): Pair<String, String>? {
    if (runningAction != null) return null
    val targetSerial = serial ?: return null
    val targetPackage = selectedPackage ?: return null
    return targetSerial to targetPackage
}
