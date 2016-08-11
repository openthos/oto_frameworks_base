/*
 * Copyright (C) 2006 The Android Open Source Project
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

package android.util;

import android.os.SystemProperties;
import android.graphics.Rect;


/**
 * A structure describing general information about a display, such as its
 * size, density, and font scaling.
 * <p>To access the DisplayMetrics members, initialize an object like this:</p>
 * <pre> DisplayMetrics metrics = new DisplayMetrics();
 * getWindowManager().getDefaultDisplay().getMetrics(metrics);</pre>
 */
public class DisplayMetrics {

    private static final float WINDOW_INIT_PART_CHECK_4_3_16_9 = 1.50f;   // 4/3 < 1.5, 16/9 > 1.5
    private static final float WINDOW_INIT_PART_WIDTH_THIN_4_3 = 0.33f;
    private static final float WINDOW_INIT_PART_HEIGHT_THIN_4_3 = 0.88f;
    private static final float WINDOW_INIT_PART_WIDTH_THIN_16_9 = 0.25f;
    private static final float WINDOW_INIT_PART_HEIGHT_THIN_16_9 = 0.90f;
    private static final float WINDOW_INIT_PART_WIDTH_WIDE = 0.66f;
    private static final float WINDOW_INIT_PART_HEIGHT_WIDE = 0.75f;

    /* For initializing window position ofsset step */
    private static final int WINDOW_OFFSET_STEP = 35;
    private static final int WINDOW_OFFSET_MAX = 4 * WINDOW_OFFSET_STEP;

    /* For initializing startup menu window positon */
    public static final int WINDOW_STARTUP_MENU_WIDTH = 770;
    public static final int WINDOW_STARTUP_MENU_PART_HEIGHT = 3;

    /**
     * Standard quantized DPI for low-density screens.
     */
    public static final int DENSITY_LOW = 120;

    /**
     * Standard quantized DPI for medium-density screens.
     */
    public static final int DENSITY_MEDIUM = 160;

    /**
     * This is a secondary density, added for some common screen configurations.
     * It is recommended that applications not generally target this as a first
     * class density -- that is, don't supply specific graphics for this
     * density, instead allow the platform to scale from other densities
     * (typically {@link #DENSITY_HIGH}) as
     * appropriate.  In most cases (such as using bitmaps in
     * {@link android.graphics.drawable.Drawable}) the platform
     * can perform this scaling at load time, so the only cost is some slight
     * startup runtime overhead.
     *
     * <p>This density was original introduced to correspond with a
     * 720p TV screen: the density for 1080p televisions is
     * {@link #DENSITY_XHIGH}, and the value here provides the same UI
     * size for a TV running at 720p.  It has also found use in 7" tablets,
     * when these devices have 1280x720 displays.
     */
    public static final int DENSITY_TV = 213;

    /**
     * Standard quantized DPI for high-density screens.
     */
    public static final int DENSITY_HIGH = 240;

    /**
     * Intermediate density for screens that sit between {@link #DENSITY_HIGH} (240dpi) and
     * {@link #DENSITY_XHIGH} (320dpi). This is not a density that applications should target,
     * instead relying on the system to scale their {@link #DENSITY_XHIGH} assets for them.
     */
    public static final int DENSITY_280 = 280;

    /**
     * Standard quantized DPI for extra-high-density screens.
     */
    public static final int DENSITY_XHIGH = 320;

    /**
     * Intermediate density for screens that sit somewhere between
     * {@link #DENSITY_XHIGH} (320 dpi) and {@link #DENSITY_XXHIGH} (480 dpi).
     * This is not a density that applications should target, instead relying
     * on the system to scale their {@link #DENSITY_XXHIGH} assets for them.
     */
    public static final int DENSITY_400 = 400;

    /**
     * Standard quantized DPI for extra-extra-high-density screens.
     */
    public static final int DENSITY_XXHIGH = 480;

    /**
     * Intermediate density for screens that sit somewhere between
     * {@link #DENSITY_XXHIGH} (480 dpi) and {@link #DENSITY_XXXHIGH} (640 dpi).
     * This is not a density that applications should target, instead relying
     * on the system to scale their {@link #DENSITY_XXXHIGH} assets for them.
     */
    public static final int DENSITY_560 = 560;

    /**
     * Standard quantized DPI for extra-extra-extra-high-density screens.  Applications
     * should not generally worry about this density; relying on XHIGH graphics
     * being scaled up to it should be sufficient for almost all cases.  A typical
     * use of this density would be 4K television screens -- 3840x2160, which
     * is 2x a traditional HD 1920x1080 screen which runs at DENSITY_XHIGH.
     */
    public static final int DENSITY_XXXHIGH = 640;

    /**
     * The reference density used throughout the system.
     */
    public static final int DENSITY_DEFAULT = DENSITY_MEDIUM;

    /**
     * Scaling factor to convert a density in DPI units to the density scale.
     * @hide
     */
    public static final float DENSITY_DEFAULT_SCALE = 1.0f / DENSITY_DEFAULT;

    /**
     * The device's density.
     * @hide because eventually this should be able to change while
     * running, so shouldn't be a constant.
     * @deprecated There is no longer a static density; you can find the
     * density for a display in {@link #densityDpi}.
     */
    @Deprecated
    public static int DENSITY_DEVICE = getDeviceDensity();

    /**
     * The absolute width of the display in pixels.
     */
    public int widthPixels;
    /**
     * The absolute height of the display in pixels.
     */
    public int heightPixels;
    /**
     * The logical density of the display.  This is a scaling factor for the
     * Density Independent Pixel unit, where one DIP is one pixel on an
     * approximately 160 dpi screen (for example a 240x320, 1.5"x2" screen), 
     * providing the baseline of the system's display. Thus on a 160dpi screen 
     * this density value will be 1; on a 120 dpi screen it would be .75; etc.
     *  
     * <p>This value does not exactly follow the real screen size (as given by 
     * {@link #xdpi} and {@link #ydpi}, but rather is used to scale the size of
     * the overall UI in steps based on gross changes in the display dpi.  For 
     * example, a 240x320 screen will have a density of 1 even if its width is 
     * 1.8", 1.3", etc. However, if the screen resolution is increased to 
     * 320x480 but the screen size remained 1.5"x2" then the density would be 
     * increased (probably to 1.5).
     *
     * @see #DENSITY_DEFAULT
     */
    public float density;
    /**
     * The screen density expressed as dots-per-inch.  May be either
     * {@link #DENSITY_LOW}, {@link #DENSITY_MEDIUM}, or {@link #DENSITY_HIGH}.
     */
    public int densityDpi;
    /**
     * A scaling factor for fonts displayed on the display.  This is the same
     * as {@link #density}, except that it may be adjusted in smaller
     * increments at runtime based on a user preference for the font size.
     */
    public float scaledDensity;
    /**
     * The exact physical pixels per inch of the screen in the X dimension.
     */
    public float xdpi;
    /**
     * The exact physical pixels per inch of the screen in the Y dimension.
     */
    public float ydpi;

    /**
     * The reported display width prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    public int noncompatWidthPixels;
    /**
     * The reported display height prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    public int noncompatHeightPixels;
    /**
     * The reported display density prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    public float noncompatDensity;
    /**
     * The reported display density prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    public int noncompatDensityDpi;
    /**
     * The reported scaled density prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    public float noncompatScaledDensity;
    /**
     * The reported display xdpi prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    public float noncompatXdpi;
    /**
     * The reported display ydpi prior to any compatibility mode scaling
     * being applied.
     * @hide
     */
    public float noncompatYdpi;

    /* Initializing window position */
    private int initPosX = WINDOW_OFFSET_STEP;
    private int initPosY = WINDOW_OFFSET_STEP;

    public DisplayMetrics() {
    }
    
    public void setTo(DisplayMetrics o) {
        widthPixels = o.widthPixels;
        heightPixels = o.heightPixels;
        density = o.density;
        densityDpi = o.densityDpi;
        scaledDensity = o.scaledDensity;
        xdpi = o.xdpi;
        ydpi = o.ydpi;
        noncompatWidthPixels = o.noncompatWidthPixels;
        noncompatHeightPixels = o.noncompatHeightPixels;
        noncompatDensity = o.noncompatDensity;
        noncompatDensityDpi = o.noncompatDensityDpi;
        noncompatScaledDensity = o.noncompatScaledDensity;
        noncompatXdpi = o.noncompatXdpi;
        noncompatYdpi = o.noncompatYdpi;
    }
    
    public void setToDefaults() {
        widthPixels = 0;
        heightPixels = 0;
        density =  DENSITY_DEVICE / (float) DENSITY_DEFAULT;
        densityDpi =  DENSITY_DEVICE;
        scaledDensity = density;
        xdpi = DENSITY_DEVICE;
        ydpi = DENSITY_DEVICE;
        noncompatWidthPixels = widthPixels;
        noncompatHeightPixels = heightPixels;
        noncompatDensity = density;
        noncompatDensityDpi = densityDpi;
        noncompatScaledDensity = scaledDensity;
        noncompatXdpi = xdpi;
        noncompatYdpi = ydpi;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DisplayMetrics && equals((DisplayMetrics)o);
    }

    /**
     * Returns true if these display metrics equal the other display metrics.
     *
     * @param other The display metrics with which to compare.
     * @return True if the display metrics are equal.
     */
    public boolean equals(DisplayMetrics other) {
        return equalsPhysical(other)
                && scaledDensity == other.scaledDensity
                && noncompatScaledDensity == other.noncompatScaledDensity;
    }

    /**
     * Returns true if the physical aspects of the two display metrics
     * are equal.  This ignores the scaled density, which is a logical
     * attribute based on the current desired font size.
     *
     * @param other The display metrics with which to compare.
     * @return True if the display metrics are equal.
     * @hide
     */
    public boolean equalsPhysical(DisplayMetrics other) {
        return other != null
                && widthPixels == other.widthPixels
                && heightPixels == other.heightPixels
                && density == other.density
                && densityDpi == other.densityDpi
                && xdpi == other.xdpi
                && ydpi == other.ydpi
                && noncompatWidthPixels == other.noncompatWidthPixels
                && noncompatHeightPixels == other.noncompatHeightPixels
                && noncompatDensity == other.noncompatDensity
                && noncompatDensityDpi == other.noncompatDensityDpi
                && noncompatXdpi == other.noncompatXdpi
                && noncompatYdpi == other.noncompatYdpi;
    }

    @Override
    public int hashCode() {
        return widthPixels * heightPixels * densityDpi;
    }

    @Override
    public String toString() {
        return "DisplayMetrics{density=" + density + ", width=" + widthPixels +
            ", height=" + heightPixels + ", scaledDensity=" + scaledDensity +
            ", xdpi=" + xdpi + ", ydpi=" + ydpi + "}";
    }

    private static int getDeviceDensity() {
        // qemu.sf.lcd_density can be used to override ro.sf.lcd_density
        // when running in the emulator, allowing for dynamic configurations.
        // The reason for this is that ro.sf.lcd_density is write-once and is
        // set by the init process when it parses build.prop before anything else.
        return SystemProperties.getInt("qemu.sf.lcd_density",
                SystemProperties.getInt("ro.sf.lcd_density", DENSITY_DEFAULT));
    }

    public boolean is_16_9() {
        return (float) widthPixels / (float) heightPixels > WINDOW_INIT_PART_CHECK_4_3_16_9;
    }

    public int getInitWindowWidthPhone() {
        if (is_16_9()) {
            return (int)((float) widthPixels * WINDOW_INIT_PART_WIDTH_THIN_16_9);
        } else {
            return (int)((float) widthPixels * WINDOW_INIT_PART_WIDTH_THIN_4_3);
        }
    }

    public int getInitWindowWidthNormal() {
        return (int)((float)widthPixels * WINDOW_INIT_PART_WIDTH_WIDE);
    }

    public int getInitWindowHeightPhone() {
        if (is_16_9()) {
            return (int)((float) heightPixels * WINDOW_INIT_PART_HEIGHT_THIN_16_9);
        } else {
            return (int)((float) heightPixels * WINDOW_INIT_PART_HEIGHT_THIN_4_3);
        }
    }

    public int getInitWindowHeightNormal() {
        return (int)((float) heightPixels * WINDOW_INIT_PART_HEIGHT_WIDE);
    }

    private int getDefaultPosX() {
        initPosX += WINDOW_OFFSET_STEP;
        if (initPosX > WINDOW_OFFSET_MAX) {
            initPosX = WINDOW_OFFSET_STEP;
        }
        return initPosX;
    }

    private int getDefaultPosY() {
        initPosY += WINDOW_OFFSET_STEP;
        if (initPosY > WINDOW_OFFSET_MAX) {
            initPosY = WINDOW_OFFSET_STEP;
        }
        return initPosY;
    }

    public Rect getDefaultFrameRect(boolean phoneStyle) {
        int posX = getDefaultPosX();
        int posY = getDefaultPosY();
        int width = phoneStyle ? getInitWindowWidthPhone() : getInitWindowWidthNormal();
        int height = phoneStyle ? getInitWindowHeightPhone() : getInitWindowHeightNormal();
        return new Rect(posX, posY, posX + width, posY + height);
    }
}
