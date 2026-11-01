import com.tlpcraft.adbdesktop.plugin.shared.hostNativeClassifier

plugins {
    id(libs.plugins.adbdesktop.kotlin.multiplatform.library.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.logger)
        }
        jvmMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.bytedeco.ffmpeg)
            implementation("org.bytedeco:ffmpeg:${libs.versions.bytedecoFfmpeg.get()}:$hostNativeClassifier")
            implementation("org.bytedeco:javacpp:${libs.versions.bytedecoJavacpp.get()}:$hostNativeClassifier")
        }
    }
}
