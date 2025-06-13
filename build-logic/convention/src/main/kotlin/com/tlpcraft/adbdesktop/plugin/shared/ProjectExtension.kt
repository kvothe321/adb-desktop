package com.tlpcraft.adbdesktop.plugin.shared

import org.gradle.api.Project

internal fun Project.applyPlugins(vararg names: String): Unit = names.forEach { name ->
    libs.findPlugin(name).get().get().pluginId.let(pluginManager::apply)
}

internal fun Project.getPackageLikeProjectPathName(): String = path
    .split(':')
    .filter { it.isNotBlank() }
    .joinToString(separator = ".") { it.toValidPackageSegment() }

private fun String.toValidPackageSegment(): String {
    val normalized = lowercase()
        .map { c -> if (c.isLetterOrDigit() || c == '_') c else '_' }
        .joinToString("")
        .trim('_')
        .ifEmpty { "app" }

    return if (normalized.first().isDigit()) "_$normalized" else normalized
}
