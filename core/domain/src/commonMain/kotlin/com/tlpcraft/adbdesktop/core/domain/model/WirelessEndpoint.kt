package com.tlpcraft.adbdesktop.core.domain.model

data class WirelessEndpoint(val host: String, val port: Int) {
    override fun toString(): String = if (host.contains(':')) "[$host]:$port" else "$host:$port"

    companion object {
        const val DEFAULT_CONNECT_PORT = 5555
        private val VALID_PORTS = 1..65_535

        /**
         * Parses `host`, `host:port` or `[ipv6]:port`.
         * Returns `null` when the input is malformed, or has no port and no [defaultPort] is given.
         */
        fun parse(input: String, defaultPort: Int? = null): WirelessEndpoint? {
            val text = input.trim()
            if (text.isEmpty() || text.any(Char::isWhitespace)) return null

            return splitAddress(text)?.toEndpoint(defaultPort)
        }

        private fun Address.toEndpoint(defaultPort: Int?): WirelessEndpoint? {
            val port = if (portText == null) defaultPort else portText.toIntOrNull()
            return if (host.isNotEmpty() && port != null && port in VALID_PORTS) WirelessEndpoint(host, port) else null
        }

        private fun splitAddress(text: String): Address? = when {
            text.startsWith('[') -> splitBracketedAddress(text)
            text.count { it == ':' } == 1 -> Address(host = text.substringBefore(':'), portText = text.substringAfter(':'))
            text.contains(':') -> null
            else -> Address(host = text, portText = null)
        }

        private fun splitBracketedAddress(text: String): Address? {
            val closingBracket = text.indexOf(']')
            if (closingBracket <= 0) return null

            val host = text.substring(1, closingBracket)
            val rest = text.substring(closingBracket + 1)
            return when {
                rest.isEmpty() -> Address(host, portText = null)
                rest.startsWith(':') -> Address(host, portText = rest.drop(1))
                else -> null
            }
        }

        private data class Address(val host: String, val portText: String?)
    }
}
