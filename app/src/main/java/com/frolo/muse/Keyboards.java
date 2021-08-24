package com.frolo.muse;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public final class Keyboards {
    private Keyboards() {
    }

    private static void hideFromWindowImpl(@NonNull Context context, @Nullable IBinder windowToken) {
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null && windowToken != null) {
            imm.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    public static void hideFrom(@NonNull Activity activity) {
        View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            hideFrom(currentFocus);
        }
    }

    public static void hideFrom(@NonNull Fragment fragment) {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            hideFrom(activity);
        }
    }

    public static void hideFrom(@NonNull View view) {
        hideFromWindowImpl(view.getContext(), view.getWindowToken());
    }

}
