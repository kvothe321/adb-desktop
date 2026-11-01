package com.tlpcraft.adbdesktop.core.data.repository.mapper

import com.tlpcraft.adbdesktop.core.data.source.adb.dto.MdnsServiceDto
import com.tlpcraft.adbdesktop.core.domain.model.WirelessService

private const val PAIRING_SERVICE = "_adb-tls-pairing._tcp"

internal fun MdnsServiceDto.toDomain() = WirelessService(
    name = name,
    address = address,
    kind = if (type == PAIRING_SERVICE) WirelessService.Kind.PAIRING else WirelessService.Kind.CONNECT,
)
