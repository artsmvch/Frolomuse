package com.frolo.muse;


public final class BuildInfo {
    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    public static boolean isGoogleServicesEnabled() {
        return BuildConfig.GOOGLE_SERVICES_ENABLED;
    }

    public static boolean isFirebaseEnabled() {
        return BuildConfig.GOOGLE_SERVICES_ENABLED;
    }
}
