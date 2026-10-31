package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.domain.model.ConnectionType
import com.tlpcraft.adbdesktop.core.domain.model.Device
import com.tlpcraft.adbdesktop.core.domain.model.DeviceState

fun onlineDevice(serial: String, model: String? = null) = Device(serial, DeviceState.ONLINE, ConnectionType.USB, model = model)

fun offlineDevice(serial: String) = Device(serial, DeviceState.OFFLINE, ConnectionType.USB)
