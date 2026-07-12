package com.tlpcraft.adbdesktop.core.data.repository.mapper

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.RemoteFileDto
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFile
import com.tlpcraft.adbdesktop.core.domain.model.RemoteFileType
import com.tlpcraft.adbdesktop.core.domain.model.RemotePath

internal fun RemoteFileDto.toDomain(directory: String) = RemoteFile(
    name = name,
    path = RemotePath.child(directory, name),
    type = fileType(),
    sizeBytes = sizeBytes,
    modified = modified,
    permissions = permissions,
    linkTarget = linkTarget,
)

private fun RemoteFileDto.fileType(): RemoteFileType = when (permissions.first()) {
    'd' -> RemoteFileType.DIRECTORY
    'l' -> RemoteFileType.LINK
    '-' -> RemoteFileType.FILE
    else -> RemoteFileType.OTHER
}
