/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.server.wm.flicker.monitor;

import static android.surfaceflinger.nano.Layerstrace.LayersTraceFileProto.MAGIC_NUMBER_H;
import static android.surfaceflinger.nano.Layerstrace.LayersTraceFileProto.MAGIC_NUMBER_L;

import static com.google.common.truth.Truth.assertThat;

import android.app.Instrumentation;
import android.surfaceflinger.nano.Layerstrace.LayersTraceFileProto;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.google.common.io.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.nio.file.Path;

/**
 * Contains {@link LayersTraceMonitor} tests. To run this test: {@code atest
 * FlickerLibTest:LayersTraceMonitorTest}
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LayersTraceMonitorTest {
    private LayersTraceMonitor mLayersTraceMonitor;

    @Before
    public void setup() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Path outputDir = instrumentation.getTargetContext().getExternalFilesDir(null).toPath();
        mLayersTraceMonitor = new LayersTraceMonitor(outputDir);
    }

    @After
    public void teardown() {
        mLayersTraceMonitor.stop();
        mLayersTraceMonitor.getOutputTraceFilePath("captureLayersTrace").toFile().delete();
    }

    @Test
    public void canStartLayersTrace() throws Exception {
        mLayersTraceMonitor.start();
        assertThat(mLayersTraceMonitor.isEnabled()).isTrue();
    }

    @Test
    public void canStopLayersTrace() throws Exception {
        mLayersTraceMonitor.start();
        assertThat(mLayersTraceMonitor.isEnabled()).isTrue();
        mLayersTraceMonitor.stop();
        assertThat(mLayersTraceMonitor.isEnabled()).isFalse();
    }

    @Test
    public void captureLayersTrace() throws Exception {
        mLayersTraceMonitor.start();
        mLayersTraceMonitor.stop();
        Path testFilePath = mLayersTraceMonitor.save("captureWindowTrace");
        File testFile = testFilePath.toFile();
        assertThat(testFile.exists()).isTrue();
        String calculatedChecksum = TraceMonitor.calculateChecksum(testFilePath);
        assertThat(calculatedChecksum).isEqualTo(mLayersTraceMonitor.getChecksum());
        byte[] trace = Files.toByteArray(testFile);
        assertThat(trace.length).isGreaterThan(0);
        LayersTraceFileProto mLayerTraceFileProto = LayersTraceFileProto.parseFrom(trace);
        assertThat(mLayerTraceFileProto.magicNumber)
                .isEqualTo((long) MAGIC_NUMBER_H << 32 | MAGIC_NUMBER_L);
    }
}
