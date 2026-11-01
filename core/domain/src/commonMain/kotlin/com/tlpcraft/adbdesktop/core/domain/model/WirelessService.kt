package com.tlpcraft.adbdesktop.core.domain.model

/** A device advertising itself on the local network. */
data class WirelessService(val name: String, val address: String, val kind: Kind) {
    enum class Kind {
        CONNECT,
        PAIRING,
    }
}
