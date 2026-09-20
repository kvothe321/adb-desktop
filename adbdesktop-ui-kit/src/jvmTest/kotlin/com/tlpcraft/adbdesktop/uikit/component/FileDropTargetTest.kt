package com.tlpcraft.adbdesktop.uikit.component

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File

class FileDropTargetTest : FunSpec({
    test("dropped file URIs become plain paths, including spaces") {
        val file = File(System.getProperty("java.io.tmpdir"), "a file.apk")

        fileUriToPath(file.toURI().toString()) shouldBe file.path
    }
})
