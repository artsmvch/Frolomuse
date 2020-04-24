package com.frolo.muse;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public final class Keyboards {
    private Keyboards() {
    }

    public static void closeFrom(@NonNull Activity activity) {
        final View currentFocus = activity.getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm =
                    (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    public static void closeFrom(@NonNull Fragment fragment) {
        final Activity activity = fragment.getActivity();
        if (activity != null) {
            closeFrom(activity);
        }
    }

}
