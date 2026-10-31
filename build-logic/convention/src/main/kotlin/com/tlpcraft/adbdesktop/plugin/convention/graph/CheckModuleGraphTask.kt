package com.tlpcraft.adbdesktop.plugin.convention.graph

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class CheckModuleGraphTask : DefaultTask() {
    @get:Input
    abstract val modulePath: Property<String>

    /** Serialized [ModuleDependency] values, kept as strings so the task stays configuration-cache friendly. */
    @get:Input
    abstract val dependencies: ListProperty<String>

    @TaskAction
    fun check() {
        val violations = ModuleGraphRules.violations(modulePath.get(), dependencies.get().map(ModuleDependency::deserialize))
        if (violations.isNotEmpty()) {
            throw GradleException("Module dependency rules violated:\n" + violations.joinToString("\n") { "  - $it" })
        }
    }
}
