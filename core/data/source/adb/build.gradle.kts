import com.tlpcraft.adbdesktop.plugin.shared.hostNativeClassifier

plugins {
    id(libs.plugins.adbdesktop.kotlin.multiplatform.library.get().pluginId)
}

val deviceHelper by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    deviceHelper(project(path = ":device-helper", configuration = "helperApk"))
}

/** The helper APK goes in as a resource next to the scrcpy server. */
val bundleDeviceHelper by tasks.registering(Sync::class) {
    from(deviceHelper) { rename { "device-helper.apk" } }
    into(layout.buildDirectory.dir("generated/deviceHelper"))
}

kotlin {
    sourceSets {
        jvmMain {
            resources.srcDir(bundleDeviceHelper)
        }
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
