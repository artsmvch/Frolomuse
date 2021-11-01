package com.frolo.muse;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public final class FragmentUtils {

    public static boolean isAttached(@NonNull Fragment fragment) {
        return fragment.getHost() != null;
    }

    public static boolean isInForeground(@NonNull Fragment fragment) {
        return isAttached(fragment) && fragment.isResumed() && fragment.isVisible();
    }

    private FragmentUtils() {
    }
}
