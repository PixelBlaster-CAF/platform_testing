/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.wm.flicker.service

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.android.server.wm.flicker.helpers.wakeUpAndGoToHomeScreen
import com.android.server.wm.flicker.rules.FlickerMetricsCollectorRule
import com.android.server.wm.traces.parser.windowmanager.WindowManagerStateHelper
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * A test for [FlickerMetricsCollectorRule] checking that the traces are collected.
 * To run this test: `atest FlickerLibTest:FlickerMetricsCollectorRuleTest`
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FlickerMetricsCollectorRuleTest {
    @get:Rule
    val flickerRule = FlickerMetricsCollectorRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private val wmHelper = WindowManagerStateHelper(instrumentation)

    @Before
    fun setup() {
        device.wakeUpAndGoToHomeScreen()
        wmHelper.waitForHomeActivityVisible()
    }

    @Test
    fun runAssertion() {
        device.pressHome()
        wmHelper.waitForHomeActivityVisible()
        flickerRule.checkPresubmitAssertions()
    }
}