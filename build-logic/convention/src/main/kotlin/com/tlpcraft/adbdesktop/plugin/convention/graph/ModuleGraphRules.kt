package com.tlpcraft.adbdesktop.plugin.convention.graph

/**
 * Allowed project dependencies per module. Patterns use `*` for one path segment.
 * Test configurations may additionally use the modules in [Rule.testOnly].
 */
internal object ModuleGraphRules {
    private val rules = listOf(
        Rule(module = ":core:common"),
        Rule(module = ":core:logger"),
        Rule(module = ":core:domain", allowed = listOf(":core:common"), testOnly = listOf(":core:testing")),
        Rule(module = ":core:testing", allowed = listOf(":core:domain", ":core:common")),
        Rule(module = ":core:data:source:*", allowed = listOf(":core:common", ":core:logger")),
        Rule(module = ":core:data:repository", allowed = listOf(":core:domain", ":core:common", ":core:data:source:*")),
        Rule(module = ":adbdesktop-ui-kit"),
        Rule(module = ":feature:*", allowed = listOf(":core:domain", ":core:common", ":adbdesktop-ui-kit"), testOnly = listOf(":core:testing")),
        Rule(module = ":desktopApp", allowed = listOf(":*", ":*:*", ":*:*:*", ":*:*:*:*")),
    )

    fun violations(module: String, dependencies: List<ModuleDependency>): List<String> {
        if (dependencies.isEmpty()) return emptyList()
        val rule = rules.firstOrNull { it.module.matchesPath(module) }
            ?: return listOf("$module has no entry in ModuleGraphRules; add one before depending on other modules")

        return dependencies
            .filterNot { dependency -> rule.permits(dependency) }
            .map { "$module must not depend on ${it.path} (${it.configuration})" }
            .distinct()
    }

    private data class Rule(val module: String, val allowed: List<String> = emptyList(), val testOnly: List<String> = emptyList()) {
        fun permits(dependency: ModuleDependency): Boolean {
            val candidates = if (dependency.isTest) allowed + testOnly else allowed
            return candidates.any { it.matchesPath(dependency.path) }
        }
    }

    private fun String.matchesPath(path: String): Boolean {
        val patternSegments = split(':')
        val pathSegments = path.split(':')
        return patternSegments.size == pathSegments.size &&
            patternSegments.zip(pathSegments).all { (pattern, segment) -> pattern == "*" || pattern == segment }
    }
}

internal data class ModuleDependency(val configuration: String, val path: String) {
    val isTest: Boolean
        get() = configuration.contains("test", ignoreCase = true)

    fun serialize(): String = "$configuration$SEPARATOR$path"

    companion object {
        private const val SEPARATOR = "|"

        fun deserialize(value: String): ModuleDependency = ModuleDependency(value.substringBefore(SEPARATOR), value.substringAfter(SEPARATOR))
    }
}
