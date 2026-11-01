package com.tlpcraft.adbdesktop.feature.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.common.flow.whileActive
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.DeviceKey
import com.tlpcraft.adbdesktop.core.domain.model.KeyAction
import com.tlpcraft.adbdesktop.core.domain.model.MirrorInput
import com.tlpcraft.adbdesktop.core.domain.service.HostClipboard
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.ObserveMirrorPinnedUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.ObserveMirrorUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.SetMirrorPinnedUseCase
import com.tlpcraft.adbdesktop.feature.screen.contract.DeviceButton
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorIntent
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorSideEffect
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorState
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorUiState
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
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One mirror for the whole window, shown in the Screen section or pinned beside every section. */
@OptIn(ExperimentalCoroutinesApi::class)
class MirrorViewModel(
    private val observeSelectedDevice: ObserveSelectedDeviceUseCase,
    private val observeMirror: ObserveMirrorUseCase,
    private val observeMirrorPinned: ObserveMirrorPinnedUseCase,
    private val setMirrorPinned: SetMirrorPinnedUseCase,
    private val hostClipboard: HostClipboard,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MirrorUiState())
    val uiState: StateFlow<MirrorUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<MirrorSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<MirrorSideEffect> = _sideEffects.receiveAsFlow()

    private val isVisible = MutableStateFlow(false)
    private val input = Channel<MirrorInput>(Channel.UNLIMITED)

    init {
        viewModelScope.launch {
            observeSelectedDevice(Unit).collect { device -> _uiState.update { it.copy(serial = device?.serial) } }
        }
        viewModelScope.launch {
            observeMirrorPinned(Unit).collect { isPinned -> _uiState.update { it.copy(isPinned = isPinned) } }
        }
        trackMirror()
    }

    fun reduceIntent(intent: MirrorIntent) {
        when (intent) {
            MirrorIntent.MirrorClicked -> _uiState.update { it.copy(isOn = !it.isOn) }
            MirrorIntent.PinClicked -> viewModelScope.launch { setMirrorPinned(!_uiState.value.isPinned) }
            is MirrorIntent.VisibilityChanged -> isVisible.value = intent.isVisible
            is MirrorIntent.InputGiven -> send(intent.input)
            is MirrorIntent.DeviceButtonClicked -> press(intent.button)
            MirrorIntent.PasteRequested -> viewModelScope.launch { hostClipboard.readText()?.let { send(MirrorInput.Paste(it)) } }
        }
    }

    /** Input only means something on a screen the user can see, so none is queued while the mirror is not live. */
    private fun send(mirrorInput: MirrorInput) {
        if (_uiState.value.mirror is MirrorState.Live) input.trySend(mirrorInput)
    }

    private fun press(button: DeviceButton) {
        val key = when (button) {
            DeviceButton.Back -> DeviceKey.Back
            DeviceButton.Home -> DeviceKey.Home
            DeviceButton.Recents -> DeviceKey.AppSwitch
            DeviceButton.VolumeDown -> DeviceKey.VolumeDown
            DeviceButton.VolumeUp -> DeviceKey.VolumeUp
            DeviceButton.Power -> DeviceKey.Power
            DeviceButton.Rotate -> return send(MirrorInput.Rotate)
        }
        send(MirrorInput.Key(KeyAction.Down, key))
        send(MirrorInput.Key(KeyAction.Up, key))
    }

    /** Follows the selected device and only streams while the mirror is on show. */
    private fun trackMirror() = viewModelScope.launch {
        _uiState.map { state -> state.serial.takeIf { state.isOn } }.distinctUntilChanged().flatMapLatest { serial ->
            if (serial == null) flowOf(MirrorState.Off) else mirrorOf(serial).whileActive(isVisible).onStart { emit(MirrorState.Starting) }
        }.collect { mirror -> _uiState.update { it.copy(mirror = mirror) } }
    }

    private fun mirrorOf(serial: String): Flow<MirrorState> = observeMirror(ObserveMirrorUseCase.Params(serial, input.receiveAsFlow()))
        .onCompletion { cause -> if (cause == null) _uiState.update { it.copy(isOn = false) } }
        .map { outcome ->
            when (outcome) {
                is Outcome.Success -> {
                    MirrorState.Live(outcome.data)
                }

                is Outcome.Failure -> {
                    _uiState.update { it.copy(isOn = false) }
                    report(outcome.error)
                    MirrorState.Off
                }
            }
        }

    private suspend fun report(error: DeviceError) {
        _sideEffects.send(MirrorSideEffect.Failed((error as? DeviceError.CommandFailed)?.details ?: error.message))
    }
}
