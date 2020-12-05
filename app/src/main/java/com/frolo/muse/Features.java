package com.frolo.muse;

import android.os.Build;

import androidx.annotation.AnyThread;


@AnyThread
public final class Features {

    /**
     * Checks if the album editor feature is available.
     * For now, this feature only works on Android P and lower (API <= 28)
     * due to media store limitations in newer OS versions.
     * @return true if the album editor feature is available.
     */
    public static boolean isAlbumEditorFeatureAvailable() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.P;
    }

    private Features() {
    }
}
