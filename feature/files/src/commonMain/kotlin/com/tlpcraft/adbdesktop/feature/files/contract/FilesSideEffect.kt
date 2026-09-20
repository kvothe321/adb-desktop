package com.tlpcraft.adbdesktop.feature.files.contract

sealed interface FilesSideEffect {
    data class Uploaded(val directory: String) : FilesSideEffect

    data class Downloaded(val name: String, val directory: String) : FilesSideEffect

    data object TransferCancelled : FilesSideEffect

    data class Deleted(val name: String) : FilesSideEffect

    data class FolderCreated(val name: String) : FilesSideEffect

    /** [details] is the message from the device, shown as it is. */
    data class OperationFailed(val details: String) : FilesSideEffect
}
