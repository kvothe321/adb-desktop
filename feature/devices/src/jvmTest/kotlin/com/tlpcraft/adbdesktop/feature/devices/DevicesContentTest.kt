package com.tlpcraft.adbdesktop.feature.devices

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.tlpcraft.adbdesktop.core.domain.model.ConnectionType
import com.tlpcraft.adbdesktop.core.domain.model.DeviceState
import com.tlpcraft.adbdesktop.feature.devices.contract.ConnectFormState
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceItem
import com.tlpcraft.adbdesktop.feature.devices.contract.DeviceListState
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesIntent
import com.tlpcraft.adbdesktop.feature.devices.contract.DevicesUiState
import com.tlpcraft.adbdesktop.uikit.theme.AppTheme
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain

@OptIn(ExperimentalTestApi::class)
class DevicesContentTest : FunSpec({
    test("an online device can be clicked and a wireless one disconnected") {
        runComposeUiTest {
            val intents = mutableListOf<DevicesIntent>()
            val device = DeviceItem("192.168.1.42:5555", "Pixel 8", DeviceState.ONLINE, ConnectionType.WIRELESS)
            setContent {
                AppTheme { DevicesContent(uiState = DevicesUiState(list = DeviceListState.Loaded(listOf(device))), onIntent = { intents += it }) }
            }

            onNodeWithText("Pixel 8").performClick()
            onNodeWithText("Disconnect").performClick()
            onNodeWithText("Connect over Wi-Fi").performClick()

            intents shouldContain DevicesIntent.DeviceClicked(device.serial)
            intents shouldContain DevicesIntent.DisconnectClicked(device.serial)
            intents shouldContain DevicesIntent.ConnectFormOpened
        }
    }

    test("the connect dialog offers pairing") {
        runComposeUiTest {
            val intents = mutableListOf<DevicesIntent>()
            setContent {
                AppTheme {
                    DevicesContent(
                        uiState = DevicesUiState(list = DeviceListState.Loaded(emptyList()), connectForm = ConnectFormState(address = "10.0.0.2")),
                        onIntent = { intents += it },
                    )
                }
            }

            onNodeWithText("Pair with code").performClick()
            onNodeWithText("Cancel").performClick()

            intents shouldContain DevicesIntent.ConnectFormDismissed
        }
    }
})
