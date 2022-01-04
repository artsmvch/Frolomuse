package com.frolo.vendor;

import android.os.Build;

import java.util.Locale;


/**
 * Here are convenient methods for determining the manufacturer of the device.
 * Methods work like in {@link com.google.android.material.internal.ManufacturerUtils}.
 */
public final class ManufacturerUtils {

    private static final String XIAOMI = "xiaomi";

    private ManufacturerUtils() { }

    /**
     * Returns true if the manufacturer of this device is Xiaomi.
     * @return true if the manufacturer of this device is Xiaomi, false - otherwise
     */
    public static boolean isXiaomiDevice() {
        return Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).equals(XIAOMI);
    }

}
