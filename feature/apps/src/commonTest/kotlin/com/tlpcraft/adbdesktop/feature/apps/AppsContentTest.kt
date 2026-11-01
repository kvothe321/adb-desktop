package com.tlpcraft.adbdesktop.feature.apps

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.tlpcraft.adbdesktop.core.domain.model.AppDetails
import com.tlpcraft.adbdesktop.feature.apps.contract.AppDetailsState
import com.tlpcraft.adbdesktop.feature.apps.contract.AppListState
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsIntent
import com.tlpcraft.adbdesktop.feature.apps.contract.AppsUiState
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain

@OptIn(ExperimentalTestApi::class)
class AppsContentTest : FunSpec({
    val details = AppDetails("com.example.notes", "2.4.1", 241, 34, 26, "2023-03-12", "2024-11-20", 2_048, 1_024, null)

    test("renders the list and details, and reports clicks") {
        runComposeUiTest {
            val intents = mutableListOf<AppsIntent>()
            setContent {
                AppTheme(darkTheme = true) {
                    AppsContent(
                        uiState = AppsUiState(
                            serial = "pixel",
                            list = AppListState.Loaded(listOf("com.android.settings", "com.example.notes")),
                            selectedPackage = "com.example.notes",
                            details = AppDetailsState.Loaded(details),
                        ),
                        onIntent = { intents += it },
                    )
                }
            }

            onNodeWithText("2.4.1").assertExists()
            onNodeWithText("Settings").performClick()
            onNodeWithText("Install APK").performClick()

            intents shouldContain AppsIntent.AppClicked("com.android.settings")
            intents shouldContain AppsIntent.InstallClicked
        }
    }
})
