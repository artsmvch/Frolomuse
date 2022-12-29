package com.frolo.ui;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;


public final class KeyboardUtils {
    private static final String LOG_TAG = "KeyboardUtils";

    private KeyboardUtils() {
    }

    private static void hideFromWindowImpl(@NonNull Context context, @Nullable IBinder windowToken) {
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null && windowToken != null) {
            boolean result = imm.hideSoftInputFromWindow(windowToken, 0);
            if (!result) {
                Log.d(LOG_TAG, "Failed to hide Input method");
            }
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

    private static void showOnceImpl(@NonNull View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            boolean result = imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            if (!result) {
                Log.d(LOG_TAG, "Failed to show Input method");
            }
        }
    }

    public static void showOnce(@NonNull View view) {
        showOnceImpl(view);
    }

    public static void show(@NonNull SearchView view) {
        View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showOnceImpl(v);
                }
            }
        };
        view.setOnQueryTextFocusChangeListener(focusListener);
        view.setOnFocusChangeListener(focusListener);
        view.requestFocus();
    }

}
