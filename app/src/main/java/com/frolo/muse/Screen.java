package com.frolo.muse;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Px;


public final class Screen {

    private static DisplayMetrics getDefaultDisplayMetrics() {
        return Resources.getSystem().getDisplayMetrics();
    }

    @Px
    public static int dp(@NonNull Context context, int dp) {
        return dp(context, (float) dp);
    }

    @Px
    public static int dp(@NonNull Context context, float dp) {
        return (int) dpFloat(context, dp);
    }

    public static float dpFloat(@NonNull Context context, float dp) {
        return dpFloat(context.getResources().getDisplayMetrics(), dp);
    }

    // NOTE: not recommended for use.
    public static float dpFloat(float dp) {
        return dpFloat(getDefaultDisplayMetrics(), dp);
    }

    private static float dpFloat(@NonNull DisplayMetrics metrics, float dp) {
        double scale = ((double) metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
        return (float) (dp * scale);
    }

    @Px
    public static int sp(@NonNull Context context, int sp) {
        return (int) spFloat(context, sp);
    }

    @Px
    public static int sp(@NonNull Context context, float sp) {
        return (int) spFloat(context, sp);
    }

    public static float spFloat(@NonNull Context context, float sp) {
        return spFloat(context.getResources().getDisplayMetrics(), sp);
    }

    // NOTE: not recommended for use.
    public static float spFloat(float sp) {
        return spFloat(getDefaultDisplayMetrics(), sp);
    }

    private static float spFloat(@NonNull DisplayMetrics metrics, float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
    }

    @Px
    public static int getScreenWidth(@NonNull Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            throw new NullPointerException("Context is missing WindowManager");
        }
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static boolean isLandscape(@NonNull Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static int getLayoutSize(@NonNull Context context) {
        return context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
    }

    public static boolean isSmall(@NonNull Context context) {
        return getLayoutSize(context) == Configuration.SCREENLAYOUT_SIZE_SMALL;
    }

    public static boolean isNormal(@NonNull Context context) {
        return getLayoutSize(context) == Configuration.SCREENLAYOUT_SIZE_NORMAL;
    }

    public static boolean isLarge(@NonNull Context context) {
        return getLayoutSize(context) == Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isXLarge(@NonNull Context context) {
        return getLayoutSize(context) == Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private Screen() {
    }
}
