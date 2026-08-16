package com.tlpcraft.adbdesktop.feature.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.model.RemotePath
import com.tlpcraft.adbdesktop.core.domain.model.TransferProgress
import com.tlpcraft.adbdesktop.core.domain.service.HostFiles
import com.tlpcraft.adbdesktop.core.domain.usecase.device.ObserveSelectedDeviceUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.CreateFolderUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.DeleteFileUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.DownloadFileUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.ListFilesUseCase
import com.tlpcraft.adbdesktop.core.domain.usecase.files.UploadFileUseCase
import com.tlpcraft.adbdesktop.feature.files.contract.DEFAULT_DIRECTORY
import com.tlpcraft.adbdesktop.feature.files.contract.FileListing
import com.tlpcraft.adbdesktop.feature.files.contract.FilesIntent
import com.tlpcraft.adbdesktop.feature.files.contract.FilesSideEffect
import com.tlpcraft.adbdesktop.feature.files.contract.FilesUiState
import com.tlpcraft.adbdesktop.feature.files.contract.NewFolderForm
import com.tlpcraft.adbdesktop.feature.files.contract.TransferState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FilesViewModel(
    private val observeSelectedDevice: ObserveSelectedDeviceUseCase,
    private val listFiles: ListFilesUseCase,
    private val uploadFile: UploadFileUseCase,
    private val downloadFile: DownloadFileUseCase,
    private val deleteFile: DeleteFileUseCase,
    private val createFolder: CreateFolderUseCase,
    private val hostFiles: HostFiles,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<FilesSideEffect>(Channel.BUFFERED)
    val sideEffects: Flow<FilesSideEffect> = _sideEffects.receiveAsFlow()

    private var listingJob: Job? = null
    private var operationJob: Job? = null

    init {
        viewModelScope.launch {
            observeSelectedDevice(Unit).map { it?.serial }.distinctUntilChanged().collect(::onDeviceChanged)
        }
    }

    fun reduceIntent(intent: FilesIntent) {
        when (intent) {
            is FilesIntent.EntryClicked -> if (intent.file.type.canOpen) open(intent.file.path)
            FilesIntent.UpClicked -> open(RemotePath.parent(_uiState.value.directory))
            is FilesIntent.PathChanged -> _uiState.update { it.copy(pathInput = intent.value) }
            FilesIntent.PathSubmitted -> _uiState.value.pathInput.trim().takeIf { it.startsWith(RemotePath.ROOT) }?.let(::open)
            FilesIntent.RefreshClicked -> open(_uiState.value.directory)
            FilesIntent.UploadClicked -> runExclusive { uploadAll(hostFiles.pickFiles()) }
            is FilesIntent.FilesDropped -> runExclusive { uploadAll(intent.paths) }
            FilesIntent.CancelTransferClicked -> cancelTransfer()
            is FilesIntent.DownloadClicked -> download(intent.file)
            is FilesIntent.DeleteClicked -> _uiState.update { it.copy(pendingDelete = intent.file) }
            FilesIntent.DeleteConfirmed -> confirmDelete()
            FilesIntent.DeleteCancelled -> _uiState.update { it.copy(pendingDelete = null) }
            FilesIntent.NewFolderClicked -> _uiState.update { it.copy(newFolder = NewFolderForm()) }
            is FilesIntent.NewFolderNameChanged -> _uiState.update { it.copy(newFolder = NewFolderForm(name = intent.value)) }
            FilesIntent.NewFolderSubmitted -> submitNewFolder()
            FilesIntent.NewFolderDismissed -> _uiState.update { it.copy(newFolder = null) }
        }
    }

    private fun onDeviceChanged(serial: String?) {
        listingJob?.cancel()
        _uiState.update { FilesUiState(serial = serial) }
        if (serial != null) open(DEFAULT_DIRECTORY)
    }

    private fun open(directory: String) {
        val serial = _uiState.value.serial ?: return
        listingJob?.cancel()
        _uiState.update { it.copy(directory = directory, pathInput = directory, listing = FileListing.Loading) }
        listingJob = viewModelScope.launch {
            val listing = when (val result = listFiles(ListFilesUseCase.Params(serial, directory))) {
                is Outcome.Success -> FileListing.Loaded(result.data)
                is Outcome.Failure -> FileListing.Failed(result.error.detailsForUser())
            }
            _uiState.update { it.copy(listing = listing) }
        }
    }

    private suspend fun uploadAll(paths: List<String>) {
        val state = _uiState.value
        val serial = state.serial ?: return
        if (paths.isEmpty()) return

        val failure = paths.withIndex().firstNotNullOfOrNull { (index, path) ->
            track(position = index + 1, count = paths.size, uploadFile(UploadFileUseCase.Params(serial, path, state.directory)))
        }
        _sideEffects.send(failure?.let { FilesSideEffect.OperationFailed(it.detailsForUser()) } ?: FilesSideEffect.Uploaded(state.directory))
        open(state.directory)
    }

    private fun download(file: RemoteFile) {
        val serial = _uiState.value.serial ?: return
        runExclusive {
            hostFiles.pickDirectory()?.let { directory ->
                val failure = track(position = 1, count = 1, downloadFile(DownloadFileUseCase.Params(serial, file, directory)))
                _sideEffects.send(failure?.let { FilesSideEffect.OperationFailed(it.detailsForUser()) } ?: FilesSideEffect.Downloaded(file.name, directory))
            }
        }
    }

    /** Follows one transfer until it ends and returns what went wrong, if anything. */
    private suspend fun track(position: Int, count: Int, transfer: Flow<Outcome<TransferProgress, DeviceError>>): DeviceError? {
        var failure: DeviceError? = null
        transfer.collect { outcome ->
            when (outcome) {
                is Outcome.Success -> _uiState.update { it.copy(transfer = TransferState(outcome.data.name, outcome.data.fraction, position, count)) }
                is Outcome.Failure -> failure = outcome.error
            }
        }
        return failure
    }

    private fun cancelTransfer() {
        if (_uiState.value.transfer == null) return
        operationJob?.cancel()
        _sideEffects.trySend(FilesSideEffect.TransferCancelled)
        open(_uiState.value.directory)
    }

    private fun confirmDelete() {
        val state = _uiState.value
        val serial = state.serial ?: return
        val file = state.pendingDelete ?: return
        _uiState.update { it.copy(pendingDelete = null) }
        runExclusive {
            report(deleteFile(DeleteFileUseCase.Params(serial, file.path))) { FilesSideEffect.Deleted(file.name) }
            open(state.directory)
        }
    }

    private fun submitNewFolder() {
        val state = _uiState.value
        val serial = state.serial ?: return
        val form = state.newFolder?.takeIf { it.canSubmit } ?: return
        runExclusive {
            when (val result = createFolder(CreateFolderUseCase.Params(serial, state.directory, form.name))) {
                is Outcome.Success -> {
                    _uiState.update { it.copy(newFolder = null) }
                    _sideEffects.send(FilesSideEffect.FolderCreated(form.name.trim()))
                    open(state.directory)
                }

                is Outcome.Failure -> {
                    onNewFolderFailed(result.error)
                }
            }
        }
    }

    private suspend fun onNewFolderFailed(error: DeviceError) {
        if (error is DeviceError.InvalidFileName) {
            _uiState.update { state -> state.copy(newFolder = state.newFolder?.copy(isInvalid = true)) }
        } else {
            _uiState.update { it.copy(newFolder = null) }
            _sideEffects.send(FilesSideEffect.OperationFailed(error.detailsForUser()))
        }
    }

    /** Transfers can take minutes, so only one runs at a time and the toolbar reflects it. */
    private fun runExclusive(operation: suspend () -> Unit) {
        if (_uiState.value.isBusy) return
        _uiState.update { it.copy(isBusy = true) }
        operationJob = viewModelScope.launch {
            try {
                operation()
            } finally {
                _uiState.update { it.copy(isBusy = false, transfer = null) }
            }
        }
    }

    private suspend fun report(result: Outcome<Unit, DeviceError>, success: () -> FilesSideEffect) {
        val effect = when (result) {
            is Outcome.Success -> success()
            is Outcome.Failure -> FilesSideEffect.OperationFailed(result.error.detailsForUser())
        }
        _sideEffects.send(effect)
    }

    private fun DeviceError.detailsForUser(): String = (this as? DeviceError.CommandFailed)?.details ?: message
}
