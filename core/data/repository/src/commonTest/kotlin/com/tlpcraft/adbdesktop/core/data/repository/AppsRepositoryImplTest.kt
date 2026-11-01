package com.tlpcraft.adbdesktop.core.data.repository

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakeAdbPackageDataSource
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageActionDto
import com.tlpcraft.adbdesktop.core.data.source.adb.dto.PackageFilterDto
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.AppAction
import com.tlpcraft.adbdesktop.core.domain.model.AppFilter
import com.tlpcraft.adbdesktop.core.domain.model.AppInfo
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.first

class AppsRepositoryImplTest : FunSpec({
    test("user apps map to third-party packages") {
        val source = FakeAdbPackageDataSource()

        AppsRepositoryImpl(source).observeApps("pixel", AppFilter.USER).first() shouldBe Outcome.Success(listOf(AppInfo("com.example.notes")))
        source.requestedFilters shouldContainExactly listOf(PackageFilterDto.THIRD_PARTY)
    }

    test("installing updates observers at once") {
        val source = FakeAdbPackageDataSource()
        val repository = AppsRepositoryImpl(source, pollInterval = 1.hours)

        repository.observeApps("pixel", AppFilter.USER).test {
            awaitItem()

            source.packages = listOf("com.example.new")
            repository.install("pixel", listOf("new.apk"))

            awaitItem() shouldBe Outcome.Success(listOf(AppInfo("com.example.new")))
        }
    }

    test("an action updates observers at once") {
        val source = FakeAdbPackageDataSource()
        val repository = AppsRepositoryImpl(source, pollInterval = 1.hours)

        repository.observeApps("pixel", AppFilter.USER).test {
            awaitItem()

            source.packages = emptyList()
            repository.executeAction("pixel", "com.example.notes", AppAction.UNINSTALL)

            awaitItem() shouldBe Outcome.Success(emptyList())
        }
    }

    test("an app installed elsewhere is noticed") {
        val source = FakeAdbPackageDataSource()

        AppsRepositoryImpl(source, pollInterval = 5.milliseconds).observeApps("pixel", AppFilter.USER).test {
            awaitItem()

            source.packages = listOf("com.example.new")

            awaitItem() shouldBe Outcome.Success(listOf(AppInfo("com.example.new")))
        }
    }

    test("open launches the app") {
        val source = FakeAdbPackageDataSource()

        AppsRepositoryImpl(source).executeAction("pixel", "com.example.notes", AppAction.OPEN)

        source.actions shouldContainExactly listOf(PackageActionDto.LAUNCH)
    }

    test("data source exceptions become typed errors") {
        AppsRepositoryImpl(FakeAdbPackageDataSource()).observeAppDetails("pixel", "com.example.notes").first() shouldBe
            Outcome.Failure(DeviceError.DeviceNotFound("pixel"))
    }
})
