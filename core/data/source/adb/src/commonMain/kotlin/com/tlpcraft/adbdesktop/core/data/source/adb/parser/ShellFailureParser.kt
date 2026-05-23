package com.tlpcraft.adbdesktop.core.data.source.adb.parser

/** `pm` and `am` print failures to stdout and may still exit with 0 on older Android versions. */
internal object ShellFailureParser {
    private val failurePrefixes = listOf("Failure", "Error", "Exception")

    fun isFailure(output: String): Boolean = failurePrefixes.any { output.trim().startsWith(it) }
}
