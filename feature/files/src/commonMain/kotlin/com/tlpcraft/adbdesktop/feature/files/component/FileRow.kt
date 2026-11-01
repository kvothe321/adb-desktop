package com.tlpcraft.adbdesktop.feature.files.component

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFileType
import com.tlpcraft.adbdesktop.feature.files.resources.Res
import com.tlpcraft.adbdesktop.feature.files.resources.files_delete
import com.tlpcraft.adbdesktop.feature.files.resources.files_download
import com.tlpcraft.adbdesktop.feature.files.resources.files_no_access
import com.tlpcraft.adbdesktop.uikit.component.ControlSize
import com.tlpcraft.adbdesktop.uikit.component.IconButton
import com.tlpcraft.adbdesktop.uikit.component.IconTile
import com.tlpcraft.adbdesktop.uikit.component.ListRow
import com.tlpcraft.adbdesktop.uikit.component.Tone
import com.tlpcraft.adbdesktop.uikit.icon.IconName
import com.tlpcraft.adbdesktop.uikit.preview.PreviewContext
import com.tlpcraft.adbdesktop.uikit.preview.UiKitPreview
import org.jetbrains.compose.resources.stringResource

private const val BYTES_PER_UNIT = 1_024.0
private const val ONE_DECIMAL = 10
private val SIZE_UNITS = listOf("B", "KB", "MB", "GB")

@Composable
internal fun FileRow(
    file: RemoteFile,
    enabled: Boolean,
    onClick: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListRow(
        headline = file.name,
        supporting = if (file.isAccessible) file.description() else stringResource(Res.string.files_no_access),
        monospaceSupporting = true,
        modifier = modifier,
        onClick = onClick.takeIf { file.canOpen },
        leading = { IconTile(file.type.icon, tone = if (file.type.canOpen && file.isAccessible) Tone.Accent else Tone.Neutral) },
        trailing = {
            Row {
                IconButton(
                    icon = IconName.Download,
                    contentDescription = stringResource(Res.string.files_download),
                    onClick = onDownload,
                    size = ControlSize.Small,
                    enabled = enabled && file.isAccessible,
                )
                IconButton(
                    icon = IconName.Delete,
                    contentDescription = stringResource(Res.string.files_delete),
                    onClick = onDelete,
                    size = ControlSize.Small,
                    enabled = enabled && file.isAccessible,
                )
            }
        },
    )
}

private val RemoteFileType.icon: IconName
    get() = if (canOpen) IconName.Folder else IconName.File

private fun RemoteFile.description(): String = listOfNotNull(
    linkTarget?.let { "-> $it" },
    sizeBytes?.takeIf { type == RemoteFileType.FILE }?.toReadableSize(),
    modified,
    permissions,
).joinToString("  ")

internal fun Long.toReadableSize(): String {
    var value = toDouble()
    var unitIndex = 0
    while (value >= BYTES_PER_UNIT && unitIndex < SIZE_UNITS.lastIndex) {
        value /= BYTES_PER_UNIT
        unitIndex++
    }
    val rounded = (value * ONE_DECIMAL).toLong().toDouble() / ONE_DECIMAL
    return "$rounded ${SIZE_UNITS[unitIndex]}"
}

@UiKitPreview
@Composable
private fun FileRowNoAccessPreview() = PreviewContext {
    FileRow(
        file = RemoteFile(
            name = "data_mirror",
            path = "/data_mirror",
            type = RemoteFileType.DIRECTORY,
            sizeBytes = null,
            modified = null,
            permissions = "d?????????",
        ),
        enabled = true,
        onClick = {},
        onDownload = {},
        onDelete = {},
    )
}

@UiKitPreview
@Composable
private fun FileRowPreview() = PreviewContext {
    FileRow(
        file = RemoteFile(
            name = "notes.txt",
            path = "/sdcard/notes.txt",
            type = RemoteFileType.FILE,
            sizeBytes = 2_048,
            modified = "2026-10-31 23:05",
            permissions = "-rw-rw----",
        ),
        enabled = true,
        onClick = {},
        onDownload = {},
        onDelete = {},
    )
}
