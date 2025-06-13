plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(internalLibs.kotlin.multiplatform.gradlePlugin)
    compileOnly(internalLibs.android.gradlePlugin)
    compileOnly(internalLibs.compose.gradlePlugin)
    implementation(internalLibs.ktlint.gradlePlugin)
    implementation(internalLibs.detekt.gradlePlugin)
}

group = "com.tlpcraft.adbdesktop.plugin.convention"

gradlePlugin {
    plugins {
        register("kotlinMultiplatformLibrary") {
            id = "${project.group}.kotlin.multiplatform.library"
            implementationClass = "${project.group}.KotlinMultiplatformLibrary"
        }
        register("composeMultiplatformLibrary") {
            id = "${project.group}.compose.multiplatform.library"
            implementationClass = "${project.group}.ComposeMultiplatformLibrary"
        }
        register("globalKtlint") {
            id = "${project.group}.linting.ktlint"
            implementationClass = "${project.group}.linting.ktlint.GlobalKtlint"
        }
        register("globalDetekt") {
            id = "${project.group}.detekt"
            implementationClass = "${project.group}.detekt.GlobalDetekt"
        }
        register("commitLint") {
            id = "${project.group}.linting.commitlint"
            implementationClass = "${project.group}.linting.commitlint.CommitLintPlugin"
        }
    }
}
