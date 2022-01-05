package com.frolo.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public final class FragmentUtils {

    public static boolean isAttached(@NonNull Fragment fragment) {
        return fragment.getHost() != null;
    }

    public static boolean isInForeground(@NonNull Fragment fragment) {
        return isAttached(fragment) && fragment.isResumed() && fragment.isVisible();
    }

    public static void removeAllFragmentsNow(@NonNull FragmentManager manager) {
        final FragmentTransaction transaction = manager.beginTransaction();
        for (Fragment fragment : manager.getFragments()) {
            transaction.remove(fragment);
        }
        transaction.commitNow();
    }

    private FragmentUtils() {
    }
}
