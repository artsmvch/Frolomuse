package com.frolo.muse.logger;

import android.os.Build;


final class Const {

    static final String DEVICE_INFO;
    static {
        String deviceInfo;
        try {
            // Safely get device info
            deviceInfo = getDeviceInfo();
        } catch (Throwable ignored) {
            deviceInfo = "Unknown";
        }
        DEVICE_INFO = deviceInfo;
    }

    /**
     * Returns info about this device.
     * E.g. Samsung Note 4 6.0 MARSHMALLOW, LGE LG-D410 4.4.2 KITKAT.
     * @return info about this device
     */
    private static String getDeviceInfo() {
        return Build.MANUFACTURER
                + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
    }

    private Const() {
    }

}
