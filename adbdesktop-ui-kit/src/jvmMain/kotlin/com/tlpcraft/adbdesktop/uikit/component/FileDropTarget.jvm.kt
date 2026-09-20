package com.tlpcraft.adbdesktop.uikit.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import java.io.File
import java.net.URI

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun rememberFileDropTarget(onHoverChange: (Boolean) -> Unit, onFilesDrop: (List<String>) -> Unit): Modifier {
    val currentOnHoverChange by rememberUpdatedState(onHoverChange)
    val currentOnFilesDrop by rememberUpdatedState(onFilesDrop)
    val target = remember {
        object : DragAndDropTarget {
            override fun onEntered(event: DragAndDropEvent) = currentOnHoverChange(true)

            override fun onExited(event: DragAndDropEvent) = currentOnHoverChange(false)

            override fun onEnded(event: DragAndDropEvent) = currentOnHoverChange(false)

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val files = event.dragData() as? DragData.FilesList ?: return false
                currentOnFilesDrop(files.readFiles().map(::fileUriToPath))
                return true
            }
        }
    }
    return Modifier.dragAndDropTarget(shouldStartDragAndDrop = { it.dragData() is DragData.FilesList }, target = target)
}

internal fun fileUriToPath(uri: String): String = File(URI(uri)).path
