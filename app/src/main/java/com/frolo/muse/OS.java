package com.frolo.muse;

import android.os.Build;


public final class OS {

    public static boolean isAtLeastN() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    private OS() {
    }
}
