package com.tlpcraft.adbdesktop.plugin.convention.detekt

import com.tlpcraft.adbdesktop.plugin.shared.library
import com.tlpcraft.adbdesktop.plugin.shared.libs
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Applies detekt with the Compose rule set to every project. Presentation modules get an extra config
 * layer that forbids Material and raw design APIs, so only the design system can use them.
 */
class GlobalDetekt : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val baseConfig = rootProject.file("config/detekt/detekt.yml")
            val presentationConfig = rootProject.file("config/detekt/detekt-presentation.yml")
            val composeRules = libs.library("compose-rules-detekt")

            allprojects {
                pluginManager.apply(DetektPlugin::class.java)

                extensions.configure(DetektExtension::class.java) {
                    buildUponDefaultConfig = false
                    // detekt gives the first config file precedence, so the stricter layer goes first.
                    config.setFrom(listOfNotNull(baseConfig, presentationConfig.takeIf { isPresentationModule() }))
                    source.setFrom(files("src").takeIf { file("src").exists() } ?: files())
                }
                dependencies.add("detektPlugins", composeRules)
            }
        }
    }

    private fun Project.isPresentationModule(): Boolean = path.startsWith(":feature:") || path == ":desktopApp"
}
