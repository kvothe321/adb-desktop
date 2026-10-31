package com.tlpcraft.adbdesktop.core.domain.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RemotePathTest : FunSpec({
    test("child joins with one separator") {
        RemotePath.child("/sdcard", "Download") shouldBe "/sdcard/Download"
        RemotePath.child("/", "sdcard") shouldBe "/sdcard"
    }

    test("parent stops at the root") {
        RemotePath.parent("/sdcard/Download") shouldBe "/sdcard"
        RemotePath.parent("/sdcard") shouldBe "/"
        RemotePath.parent("/") shouldBe "/"
    }
})
