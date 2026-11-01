package com.tlpcraft.adbdesktop.core.data.source.adb.parser

/** `Failure [CODE]` when the package manager refuses an APK, a Java stack trace when installing fails earlier. */
internal object InstallOutputParser {
    private const val SUCCESS_LINE = "Success"
    private const val CAUSED_BY = "Caused by:"
    private val failureCode = Regex("""Failure \[(.+?)]""")

    fun isSuccess(output: String): Boolean = output.lineSequence().any { it.trim() == SUCCESS_LINE }

    fun failureReason(output: String): String = failureCode.find(output)?.groupValues?.get(1)
        ?: rootCause(output)
        ?: output.trim()

    private fun rootCause(output: String): String? = output.lineSequence()
        .map(String::trim)
        .lastOrNull { it.startsWith(CAUSED_BY) }
        ?.substringAfterLast(": ")
}
