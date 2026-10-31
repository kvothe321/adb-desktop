package com.tlpcraft.adbdesktop.core.data.source.adb.parser

internal object InstallOutputParser {
    private const val SUCCESS_LINE = "Success"
    private val failureReason = Regex("""Failure \[(.+?)]""")

    fun isSuccess(output: String): Boolean = output.lineSequence().any { it.trim() == SUCCESS_LINE }

    fun failureReason(output: String): String = failureReason.find(output)?.groupValues?.get(1) ?: output.trim()
}
