package com.tlpcraft.adbdesktop.feature.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.common.flow.whileActive
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.service.HostClipboard
import com.tlpcraft.adbdesktop.core.domain.service.HostFiles
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.CaptureScreenshotUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.ObserveMirrorUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.RecordScreenUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.SaveRecordingUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.screen.StopRecordingUseCase
import com.tlpcraft.adbdesktop.feature.screen.contract.MirrorState
import com.tlpcraft.adbdesktop.feature.screen.contract.RecordingState
import com.tlpcraft.adbdesktop.feature.screen.contract.ScreenIntent
import com.tlpcraft.adbdesktop.feature.screen.contract.ScreenSideEffect
import com.tlpcraft.adbdesktop.feature.screen.contract.ScreenUiState
import com.tlpcraft.adbdesktop.feature.screen.contract.Screenshot
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenViewModel(
    private val observeSelectedDevice: ObserveSelectedDeviceUseCase,
    private val captureScreenshot: CaptureScreenshotUseCase,
    private val observeMirror: ObserveMirrorUseCase,
    private val recordScreen: RecordScreenUseCase,
    private val stopRecording: StopRecordingUseCase,
    private val saveRecording: SaveRecordingUseCase,
    private val hostFiles: HostFiles,
    private val hostClipboard: HostClipboard,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenUiState())
    val uiState: StateFlow<ScreenUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<ScreenSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<ScreenSideEffect> = _sideEffects.receiveAsFlow()

    private val isVisible = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            observeSelectedDevice(Unit).collect { device -> _uiState.update { it.copy(serial = device?.serial) } }
        }
        trackMirror()
    }

    fun reduceIntent(intent: ScreenIntent) {
        when (intent) {
            ScreenIntent.CaptureClicked -> capture()
            ScreenIntent.SaveScreenshotClicked -> saveScreenshot()
            ScreenIntent.CopyScreenshotClicked -> copyScreenshot()
            ScreenIntent.RecordClicked -> record()
            ScreenIntent.StopClicked -> stop()
            ScreenIntent.MirrorClicked -> _uiState.update { it.copy(isMirrorOn = !it.isMirrorOn) }
            is ScreenIntent.VisibilityChanged -> isVisible.value = intent.isVisible
        }
    }

    /** Follows the selected device and only streams while the screen is on show. */
    private fun trackMirror() = viewModelScope.launch {
        _uiState.map { state -> state.serial.takeIf { state.isMirrorOn } }.distinctUntilChanged().flatMapLatest { serial ->
            if (serial == null) flowOf(MirrorState.Off) else mirrorOf(serial).whileActive(isVisible).onStart { emit(MirrorState.Starting) }
        }.collect { mirror -> _uiState.update { it.copy(mirror = mirror) } }
    }

    private fun mirrorOf(serial: String): Flow<MirrorState> = observeMirror(serial)
        .onCompletion { cause -> if (cause == null) _uiState.update { it.copy(isMirrorOn = false) } }
        .map { outcome ->
            when (outcome) {
                is Outcome.Success -> {
                    MirrorState.Live(outcome.data)
                }

                is Outcome.Failure -> {
                    _uiState.update { it.copy(isMirrorOn = false) }
                    report(outcome.error)
                    MirrorState.Off
                }
            }
        }

    private fun capture() {
        val state = _uiState.value
        val serial = state.serial?.takeIf { state.canCapture } ?: return
        _uiState.update { it.copy(isCapturing = true) }
        viewModelScope.launch {
            when (val result = captureScreenshot(serial)) {
                is Outcome.Success -> _uiState.update { it.copy(screenshot = Screenshot(result.data)) }
                is Outcome.Failure -> report(result.error)
            }
            _uiState.update { it.copy(isCapturing = false) }
        }
    }

    private fun saveScreenshot() {
        val screenshot = _uiState.value.screenshot ?: return
        viewModelScope.launch {
            hostFiles.pickSaveLocation(SCREENSHOT_FILE_NAME)?.let { path ->
                hostFiles.write(path, screenshot.png)
                _sideEffects.send(ScreenSideEffect.ScreenshotSaved(path))
            }
        }
    }

    private fun copyScreenshot() {
        val screenshot = _uiState.value.screenshot ?: return
        viewModelScope.launch {
            hostClipboard.copyPng(screenshot.png)
            _sideEffects.send(ScreenSideEffect.ScreenshotCopied)
        }
    }

    private fun record() {
        val state = _uiState.value
        val serial = state.serial?.takeIf { state.canRecord } ?: return
        _uiState.update { it.copy(recording = RecordingState.Recording, recordingSeconds = 0) }
        val clock = startRecordingClock()
        viewModelScope.launch {
            val result = recordScreen(serial)
            clock.cancel()
            when (result) {
                is Outcome.Success -> offerToSaveRecording(serial)
                is Outcome.Failure -> report(result.error)
            }
            _uiState.update { it.copy(recording = RecordingState.Idle) }
        }
    }

    private fun startRecordingClock() = viewModelScope.launch {
        while (true) {
            delay(1.seconds)
            _uiState.update { it.copy(recordingSeconds = it.recordingSeconds + 1) }
        }
    }

    private suspend fun offerToSaveRecording(serial: String) {
        _uiState.update { it.copy(recording = RecordingState.Saving) }
        val path = hostFiles.pickSaveLocation(RECORDING_FILE_NAME)
        if (path == null) {
            _sideEffects.send(ScreenSideEffect.RecordingDiscarded)
            return
        }
        when (val result = saveRecording(SaveRecordingUseCase.Params(serial, path))) {
            is Outcome.Success -> _sideEffects.send(ScreenSideEffect.RecordingSaved(path))
            is Outcome.Failure -> report(result.error)
        }
    }

    private fun stop() {
        val state = _uiState.value
        val serial = state.serial ?: return
        if (state.recording != RecordingState.Recording) return
        _uiState.update { it.copy(recording = RecordingState.Stopping) }
        viewModelScope.launch {
            val result = stopRecording(serial)
            if (result is Outcome.Failure) report(result.error)
        }
    }

    private suspend fun report(error: DeviceError) {
        _sideEffects.send(ScreenSideEffect.Failed((error as? DeviceError.CommandFailed)?.details ?: error.message))
    }

    private companion object {
        const val SCREENSHOT_FILE_NAME = "screenshot.png"
        const val RECORDING_FILE_NAME = "recording.mp4"
    }
}
