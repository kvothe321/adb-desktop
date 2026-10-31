package com.tlpcraft.adbdesktop.plugin.shared

import org.gradle.api.Project

internal fun Project.applyPlugins(vararg names: String): Unit = names.forEach { name ->
    libs.findPlugin(name).get().get().pluginId.let(pluginManager::apply)
}

/** JDK toolchain version shared by every module, read from the `java` entry of the version catalog. */
internal val Project.javaToolchainVersion: Int
    get() = libs.findVersion("java").get().requiredVersion.toInt()

internal fun Project.getPackageLikeProjectPathName(): String = path
    .split(':')
    .filter { it.isNotBlank() }
    .joinToString(separator = ".") { it.toValidPackageSegment() }

private fun String.toValidPackageSegment(): String {
    val normalized = lowercase()
        .map { c -> if (c.isLetterOrDigit() || c == '_') c else '_' }
        .joinToString("")
        .trim('_')
        .ifEmpty { "module" }

    return if (normalized.first().isDigit()) "_$normalized" else normalized
}
