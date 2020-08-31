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

package com.android.server.wm.flicker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.test.InstrumentationRegistry;

/** Helper functions to retrieve system window sizes and positions. */
public class WindowUtils {

    @NonNull
    public static Rect getDisplayBounds() {
        Point display = new Point();
        WindowManager wm =
                (WindowManager)
                        InstrumentationRegistry.getContext()
                                .getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealSize(display);
        return new Rect(0, 0, display.x, display.y);
    }

    private static int getCurrentRotation() {
        WindowManager wm =
                (WindowManager)
                        InstrumentationRegistry.getContext()
                                .getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getRotation();
    }

    @NonNull
    public static Region getDisplayBounds(int requestedRotation) {
        Rect displayBounds = getDisplayBounds();
        int currentDisplayRotation = getCurrentRotation();

        boolean displayIsRotated =
                (currentDisplayRotation == Surface.ROTATION_90
                        || currentDisplayRotation == Surface.ROTATION_270);

        boolean requestedDisplayIsRotated =
                requestedRotation == Surface.ROTATION_90
                        || requestedRotation == Surface.ROTATION_270;

        // if the current orientation changes with the requested rotation,
        // flip height and width of display bounds.
        if (displayIsRotated != requestedDisplayIsRotated) {
            return new Region(0, 0, displayBounds.height(), displayBounds.width());
        }

        return new Region(0, 0, displayBounds.width(), displayBounds.height());
    }

    @NonNull
    public static Region getAppPosition(int requestedRotation) {
        Rect displayBounds = getDisplayBounds();
        int currentDisplayRotation = getCurrentRotation();

        boolean displayIsRotated =
                currentDisplayRotation == Surface.ROTATION_90
                        || currentDisplayRotation == Surface.ROTATION_270;

        boolean requestedAppIsRotated =
                requestedRotation == Surface.ROTATION_90
                        || requestedRotation == Surface.ROTATION_270;

        // display size will change if the display is reflected. Flip height and width of app if the
        // requested rotation is different from the current rotation.
        if (displayIsRotated != requestedAppIsRotated) {
            return new Region(0, 0, displayBounds.height(), displayBounds.width());
        }

        return new Region(0, 0, displayBounds.width(), displayBounds.height());
    }

    @NonNull
    public static Region getStatusBarPosition(int requestedRotation) {
        Resources resources = InstrumentationRegistry.getContext().getResources();
        String resourceName;
        Rect displayBounds = getDisplayBounds();
        int width;
        if (requestedRotation == Surface.ROTATION_0 || requestedRotation == Surface.ROTATION_180) {
            resourceName = "status_bar_height_portrait";
            width = Math.min(displayBounds.width(), displayBounds.height());
        } else {
            resourceName = "status_bar_height_landscape";
            width = Math.max(displayBounds.width(), displayBounds.height());
        }

        int resourceId = resources.getIdentifier(resourceName, "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);

        return new Region(0, 0, width, height);
    }

    @NonNull
    public static Region getNavigationBarPosition(int requestedRotation) {
        Rect displayBounds = getDisplayBounds();
        int displayWidth;
        int displayHeight;

        if (requestedRotation == Surface.ROTATION_0 || requestedRotation == Surface.ROTATION_180) {
            displayWidth = displayBounds.width();
            displayHeight = displayBounds.height();
        } else {
            // swap display dimensions in landscape or seascape mode
            displayWidth = displayBounds.height();
            displayHeight = displayBounds.width();
        }

        int navBarWidth = getDimensionPixelSize("navigation_bar_width");
        int navBarHeight = getNavigationBarHeight();

        Region navBarLocation;
        if (requestedRotation == Surface.ROTATION_0
                || requestedRotation == Surface.ROTATION_180
                || isGesturalNavigationEnabled()) {
            // nav bar is at the bottom of the screen
            navBarLocation =
                    new Region(0, displayHeight - navBarHeight, displayWidth, displayHeight);
        } else if (requestedRotation == Surface.ROTATION_90) {
            return new Region(0, 0, navBarWidth, displayHeight);
        } else {
            return new Region(displayWidth - navBarWidth, 0, displayWidth, displayHeight);
        }

        return navBarLocation;
    }

    /*
     * Checks if the device uses gestural navigation
     */
    private static boolean isGesturalNavigationEnabled() {
        Resources resources = InstrumentationRegistry.getContext().getResources();
        int resourceId =
                resources.getIdentifier("config_navBarInteractionMode", "integer", "android");
        return resources.getInteger(resourceId) == 2 /* NAV_BAR_MODE_GESTURAL */;
    }

    public static int getDimensionPixelSize(String resourceName) {
        Resources resources = InstrumentationRegistry.getContext().getResources();
        int resourceId = resources.getIdentifier(resourceName, "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    public static int getNavigationBarHeight() {
        int navBarHeight = getDimensionPixelSize("navigation_bar_height");
        if (isGesturalNavigationEnabled()) {
            navBarHeight += getDimensionPixelSize("navigation_bar_gesture_height");
        }

        return navBarHeight;
    }

    public static int getDockedStackDividerInset() {
        Resources resources = InstrumentationRegistry.getContext().getResources();
        int resourceId = resources.getIdentifier("docked_stack_divider_insets", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }
}
