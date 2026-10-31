package com.tlpcraft.adbdesktop.plugin.convention.graph

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

/** Registers `checkModuleGraph` in every module and makes `check` depend on it. */
class ModuleGraphCheck : Plugin<Project> {
    override fun apply(target: Project) {
        target.subprojects {
            val module = this
            val graphCheck = tasks.register<CheckModuleGraphTask>(TASK_NAME) {
                group = "verification"
                description = "Fails when this module depends on a module the architecture forbids."
                modulePath.set(module.path)
                dependencies.set(
                    provider {
                        module.configurations.flatMap { configuration ->
                            configuration.dependencies.withType<ProjectDependency>().map { ModuleDependency(configuration.name, it.path).serialize() }
                        }.distinct()
                    },
                )
            }
            tasks.matching { it.name == "check" }.configureEach { dependsOn(graphCheck) }
        }
    }

    private companion object {
        const val TASK_NAME = "checkModuleGraph"
    }
}
