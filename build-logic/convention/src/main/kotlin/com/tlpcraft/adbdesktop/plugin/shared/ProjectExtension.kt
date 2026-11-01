package com.tlpcraft.adbdesktop.plugin.shared

import org.gradle.api.Project

internal fun Project.applyPlugins(vararg names: String): Unit = names.forEach { name ->
    libs.findPlugin(name).get().get().pluginId.let(pluginManager::apply)
}

/** JDK toolchain version shared by every module, read from the `java` entry of the version catalog. */
internal val Project.javaToolchainVersion: Int
    get() = libs.findVersion("java").get().requiredVersion.toInt()

/** Compose Desktop with the Skia runtime of the host, which local runs and off screen UI tests need. */
val Project.composeDesktopRuntime: String
    get() {
        val osName = providers.systemProperty("os.name").get().lowercase()
        val platform = when {
            "mac" in osName -> "macos"
            "windows" in osName -> "windows"
            else -> "linux"
        }
        val architecture = if (providers.systemProperty("os.arch").get() in ARM_ARCHITECTURES) "arm64" else "x64"
        val version = libs.findVersion("composeMultiplatform").get().requiredVersion
        return "org.jetbrains.compose.desktop:desktop-jvm-$platform-$architecture:$version"
    }

internal fun Project.getPackageLikeProjectPathName(): String = path
    .split(':')
    .filter { it.isNotBlank() }
    .joinToString(separator = ".") { it.toValidPackageSegment() }

private val ARM_ARCHITECTURES = setOf("aarch64", "arm64")

private fun String.toValidPackageSegment(): String {
    val normalized = lowercase()
        .map { c -> if (c.isLetterOrDigit() || c == '_') c else '_' }
        .joinToString("")
        .trim('_')
        .ifEmpty { "module" }

    return if (normalized.first().isDigit()) "_$normalized" else normalized
}
