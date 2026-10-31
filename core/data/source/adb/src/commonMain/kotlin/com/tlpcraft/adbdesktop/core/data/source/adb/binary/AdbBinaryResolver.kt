package com.tlpcraft.adbdesktop.core.data.source.adb.binary

import com.tlpcraft.adbdesktop.core.data.source.adb.AdbException

/**
 * Locates adb, in order: the user override, `ANDROID_HOME`, `ANDROID_SDK_ROOT`, then every `PATH` entry.
 * An invalid override fails instead of silently falling back, so the user sees why their choice is ignored.
 */
class AdbBinaryResolver(
    private val environment: AdbEnvironment,
    private val overrideProvider: AdbPathOverrideProvider,
) {
    suspend fun resolve(): String {
        val override = overrideProvider.adbPathOverride()
        if (override != null) {
            if (!environment.isExecutableFile(override)) throw AdbException.InvalidExecutable(override)
            return override
        }

        val candidates = candidates()
        return candidates.firstOrNull(environment::isExecutableFile) ?: throw AdbException.ExecutableNotFound(candidates)
    }

    private fun candidates(): List<String> {
        val sdkCandidates = SDK_VARIABLES
            .mapNotNull(environment::variable)
            .map { sdkRoot -> join(sdkRoot, PLATFORM_TOOLS, environment.executableName) }
        val pathCandidates = environment.variable(PATH_VARIABLE).orEmpty()
            .split(environment.pathSeparator)
            .filter(String::isNotBlank)
            .map { directory -> join(directory, environment.executableName) }
        return (sdkCandidates + pathCandidates).distinct()
    }

    private fun join(vararg parts: String): String = parts.joinToString(environment.fileSeparator) { it.trimEnd('/', '\\') }

    private companion object {
        val SDK_VARIABLES = listOf("ANDROID_HOME", "ANDROID_SDK_ROOT")
        const val PATH_VARIABLE = "PATH"
        const val PLATFORM_TOOLS = "platform-tools"
    }
}
