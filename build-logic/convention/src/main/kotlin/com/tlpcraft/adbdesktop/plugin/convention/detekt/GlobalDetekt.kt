package com.tlpcraft.adbdesktop.plugin.convention.detekt

import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project

class GlobalDetekt : Plugin<Project> {
    override fun apply(target: Project) {
        println("[BUILD-LOGIC] - Applying Global Detekt Convention Plugin")
        with(target) {
            val detektConfigFile = rootProject.file("${rootProject.rootDir}/detekt.yml")
            copyDetektConfig(detektConfigFile)

            allprojects {
                pluginManager.apply(DetektPlugin::class.java)

                extensions.configure(DetektExtension::class.java) {
                    config.setFrom(detektConfigFile)
                    source.setFrom(
                        files(
                            "src/commonMain/kotlin",
                            "src/androidMain/kotlin",
                            "src/iosMain/kotlin",
                            "src/desktopMain/kotlin"
                        )
                    )
                }
            }
        }
    }

    private fun copyDetektConfig(destination: File) {
        destination.parentFile.mkdirs()
        javaClass.classLoader.getResourceAsStream("detekt.yml")?.use { inputStream ->
            destination.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IllegalStateException("detekt.yml not found in resources")
    }
}
