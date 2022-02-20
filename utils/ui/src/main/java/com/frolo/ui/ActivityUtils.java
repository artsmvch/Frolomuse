package com.frolo.ui;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.List;


public final class ActivityUtils {

    public static boolean isFinishingOrDestroyed(@NonNull Activity activity) {
        return activity.isFinishing() || activity.isDestroyed();
    }

    /**
     * Executes the {@code action} when the activity is finally destroyed,
     * that is, after its onDestroy method has been called.
     * @param activity activity
     * @param action to execute
     */
    public static void runOnFinalDestroy(@NonNull Activity activity, @NonNull Runnable action) {
        if (isFinallyDestroyed(activity)) {
            action.run();
            return;
        }
        Application.ActivityLifecycleCallbacks callback = new SimpleActivityLifecycleCallbacks() {
            @Override
            public void onActivityPostDestroyed(@NonNull Activity destroyed) {
                if (destroyed == activity) {
                    activity.getApplication().unregisterActivityLifecycleCallbacks(this);
                    action.run();
                }
            }
        };
        activity.getApplication().registerActivityLifecycleCallbacks(callback);
    }

    private static boolean isFinallyDestroyed(@NonNull Activity activity) {
        if (activity instanceof FragmentActivity) {
            // The FragmentActivity is considered finally destroyed if its fragment manager
            // is destroyed and all of its fragments are destroyed.
            FragmentManager fm = ((FragmentActivity) activity).getSupportFragmentManager();
            List<Fragment> fragments = fm.getFragments();
            for (Fragment fragment : fragments) {
                if (fragment.getHost() == null) {
                    // Idk what to do with this
                    continue;
                }
                // We can use the child fragment manager to check
                // if the fragment is actually destroyed by now.
                FragmentManager childFm = fragment.getChildFragmentManager();
                if (!childFm.isDestroyed()) {
                    // This fragment is still alive
                    return false;
                }
            }
            return activity.isDestroyed() && fm.isDestroyed();
        } else {
            return activity.isDestroyed();
        }
    }

    private ActivityUtils() {
    }
}
