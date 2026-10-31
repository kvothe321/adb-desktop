package com.tlpcraft.adbdesktop.host

import com.tlpcraft.adbdesktop.core.common.dispatcher.DispatcherProvider
import com.tlpcraft.adbdesktop.core.domain.service.HostFiles
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

/** The native dialog where AWT has one. AWT cannot pick folders on every platform, so that one uses Swing. */
class JvmHostFiles(private val dispatchers: DispatcherProvider) : HostFiles {

    override suspend fun pickFiles(extension: String?): List<String> = onUiThread {
        val dialog = FileDialog(null as Frame?).apply {
            isMultipleMode = true
            if (extension != null) setFilenameFilter { _, name -> name.endsWith(".$extension", ignoreCase = true) }
            isVisible = true
        }
        dialog.files.map(File::getPath)
    }

    override suspend fun pickDirectory(): String? = onUiThread {
        val chooser = JFileChooser().apply { fileSelectionMode = JFileChooser.DIRECTORIES_ONLY }
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile.path else null
    }

    override suspend fun pickSaveLocation(suggestedName: String): String? = onUiThread {
        val dialog = FileDialog(null as Frame?, null, FileDialog.SAVE).apply {
            file = suggestedName
            isVisible = true
        }
        dialog.file?.let { File(dialog.directory, it).path }
    }

    override suspend fun write(path: String, content: ByteArray) = withContext(dispatchers.io) {
        File(path).writeBytes(content)
    }

    private suspend fun <T> onUiThread(block: () -> T): T = withContext(Dispatchers.Swing) { block() }
}
