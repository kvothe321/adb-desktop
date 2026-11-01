package com.tlpcraft.adbdesktop.core.data.repository

import app.cash.turbine.test
import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakeAdbAppIconDataSource
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakeAdbPackageDataSource
import com.tlpcraft.adbdesktop.core.data.repository.fake.FakeAdbPackageEventDataSource
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
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeout

class AppsRepositoryImplTest : FunSpec({
    val scope = CoroutineScope(SupervisorJob())

    afterSpec { scope.cancel() }

    test("user apps map to third-party packages") {
        val source = FakeAdbPackageDataSource()

        AppsRepositoryImpl(source, FakeAdbAppIconDataSource(), FakeAdbPackageEventDataSource(), scope).observeApps("pixel", AppFilter.USER).first() shouldBe
            Outcome.Success(listOf(AppInfo("com.example.notes")))
        source.requestedFilters shouldContainExactly listOf(PackageFilterDto.THIRD_PARTY)
    }

    test("installing updates observers at once") {
        val source = FakeAdbPackageDataSource()
        val repository = AppsRepositoryImpl(source, FakeAdbAppIconDataSource(), FakeAdbPackageEventDataSource(), scope, pollInterval = 1.hours)

        repository.observeApps("pixel", AppFilter.USER).test {
            awaitItem()

            source.packages = listOf("com.example.new")
            repository.install("pixel", listOf("new.apk"))

            awaitItem() shouldBe Outcome.Success(listOf(AppInfo("com.example.new")))
        }
    }

    test("an action updates observers at once") {
        val source = FakeAdbPackageDataSource()
        val repository = AppsRepositoryImpl(source, FakeAdbAppIconDataSource(), FakeAdbPackageEventDataSource(), scope, pollInterval = 1.hours)

        repository.observeApps("pixel", AppFilter.USER).test {
            awaitItem()

            source.packages = emptyList()
            repository.executeAction("pixel", "com.example.notes", AppAction.UNINSTALL)

            awaitItem() shouldBe Outcome.Success(emptyList())
        }
    }

    test("an app installed elsewhere shows up as soon as the device reports it") {
        val source = FakeAdbPackageDataSource()
        val events = FakeAdbPackageEventDataSource()

        AppsRepositoryImpl(source, FakeAdbAppIconDataSource(), events, scope, pollInterval = 1.hours).observeApps("pixel", AppFilter.USER).test {
            awaitItem()

            source.packages = listOf("com.example.new")
            events.changes.send("com.example.new")

            awaitItem() shouldBe Outcome.Success(listOf(AppInfo("com.example.new")))
        }
    }

    test("watchers of one device share a listener") {
        val events = FakeAdbPackageEventDataSource()
        val repository = AppsRepositoryImpl(FakeAdbPackageDataSource(), FakeAdbAppIconDataSource(), events, scope, pollInterval = 1.hours)

        repository.observeApps("pixel", AppFilter.USER).test {
            awaitItem()
            repository.observeApps("pixel", AppFilter.ALL).test {
                awaitItem()
                withTimeout(1.seconds) { while (events.listeners == 0) delay(10.milliseconds) }
                delay(100.milliseconds)
                events.listeners shouldBe 1
            }
        }
    }

    test("a device that cannot report changes is checked every interval instead") {
        val source = FakeAdbPackageDataSource()

        AppsRepositoryImpl(
            source,
            FakeAdbAppIconDataSource(),
            FakeAdbPackageEventDataSource(canReport = false),
            scope,
            pollInterval = 5.milliseconds,
        ).observeApps("pixel", AppFilter.USER).test {
            awaitItem()

            source.packages = listOf("com.example.new")

            awaitItem() shouldBe Outcome.Success(listOf(AppInfo("com.example.new")))
        }
    }

    test("open launches the app") {
        val source = FakeAdbPackageDataSource()

        AppsRepositoryImpl(
            source,
            FakeAdbAppIconDataSource(),
            FakeAdbPackageEventDataSource(),
            scope,
        ).executeAction("pixel", "com.example.notes", AppAction.OPEN)

        source.actions shouldContainExactly listOf(PackageActionDto.LAUNCH)
    }

    test("data source exceptions become typed errors") {
        AppsRepositoryImpl(
            FakeAdbPackageDataSource(),
            FakeAdbAppIconDataSource(),
            FakeAdbPackageEventDataSource(),
            scope,
        ).observeAppDetails("pixel", "com.example.notes").first() shouldBe
            Outcome.Failure(DeviceError.DeviceNotFound("pixel"))
    }

    context("icons") {
        val png = byteArrayOf(1, 2, 3)

        test("arrive as they are read and leave out apps without one") {
            val icons = FakeAdbAppIconDataSource(mapOf("com.a" to png, "com.b" to null))

            val found = AppsRepositoryImpl(
                FakeAdbPackageDataSource(),
                icons,
                FakeAdbPackageEventDataSource(),
                scope,
            ).observeAppIcons("pixel", listOf("com.a", "com.b")).toList().last()

            found.keys shouldBe setOf("com.a")
            found.getValue("com.a").png.toList() shouldBe png.toList()
        }

        test("are read once per device, apps without one included") {
            val icons = FakeAdbAppIconDataSource(mapOf("com.a" to png))
            val repository = AppsRepositoryImpl(FakeAdbPackageDataSource(), icons, FakeAdbPackageEventDataSource(), scope)
            repository.observeAppIcons("pixel", listOf("com.a", "com.b")).toList()

            repository.observeAppIcons("pixel", listOf("com.a", "com.b", "com.c")).toList().last().keys shouldBe setOf("com.a")

            icons.requested shouldBe listOf(listOf("com.a", "com.b"), listOf("com.c"))
        }

        test("a failure keeps the icons read before it") {
            val icons = FakeAdbAppIconDataSource(mapOf("com.a" to png, "com.b" to png), failsAfter = 1)

            AppsRepositoryImpl(
                FakeAdbPackageDataSource(),
                icons,
                FakeAdbPackageEventDataSource(),
                scope,
            ).observeAppIcons("pixel", listOf("com.a", "com.b")).toList().last().keys shouldBe
                setOf("com.a")
        }
    }
})
