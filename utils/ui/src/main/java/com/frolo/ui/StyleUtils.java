package com.frolo.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.content.res.AppCompatResources;

import com.frolo.debug.DebugUtils;


/**
 * Helper class for convenient reading of theme attributes.
 */
public final class StyleUtils {

    private static final int FALLBACK_COLOR = Color.TRANSPARENT;

    private static void failedToResolveAttribute(@AttrRes int attrId) {
        if (DebugUtils.isDebug()) {
            throw new IllegalArgumentException("Failed to resolve attribute: " + attrId);
        }
    }

    private static void assertColorInt(@NonNull TypedValue value) {
        boolean isColorInt = value.type >= TypedValue.TYPE_FIRST_COLOR_INT
                && value.type <= TypedValue.TYPE_LAST_COLOR_INT;
        if (!isColorInt) {
            if (DebugUtils.isDebug()) {
                throw new IllegalArgumentException("Resolved attribute value is not a color. " +
                        "Actual type: " + value.type);
            }
        }
    }

    @ColorInt
    public static int resolveColor(@NonNull Context context, @AttrRes int attrId) {
        ColorStateList list = resolveColorStateList(context, attrId);
        if (list == null) {
            failedToResolveAttribute(attrId);
            return FALLBACK_COLOR;
        }
        return list.getDefaultColor();
    }

    @Nullable
    public static ColorStateList resolveColorStateList(@NonNull Context context, @AttrRes int attrId) {
        final TypedValue typedValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(attrId, typedValue, false)) {
            failedToResolveAttribute(attrId);
            // Failed to resolve the attribute, returning null
            return null;
        }
        if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return ColorStateList.valueOf(typedValue.data);
        } else {
            return AppCompatResources.getColorStateList(context, typedValue.data);
        }
    }

    @StyleRes
    public static int resolveStyleRes(@NonNull Context context, @AttrRes int attrId) {
        final TypedValue typedValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(attrId, typedValue, true)) {
            failedToResolveAttribute(attrId);
        }
        return typedValue.resourceId;
    }

    public static float resolveDimen(@NonNull Context context, @AttrRes int attrId) {
        final TypedValue typedValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(attrId, typedValue, true)) {
            failedToResolveAttribute(attrId);
        }
        return typedValue.getDimension(context.getResources().getDisplayMetrics());
    }

    @Nullable
    public static Drawable resolveDrawable(@NonNull Context context, @AttrRes int attrId) {
        final TypedValue typedValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(attrId, typedValue, true)) {
            failedToResolveAttribute(attrId);
        }
        return AppCompatResources.getDrawable(context, typedValue.resourceId);
    }

    public static boolean resolveBool(@NonNull Context context, @AttrRes int attrId) {
        final TypedValue typedValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(attrId, typedValue, true)) {
            failedToResolveAttribute(attrId);
        }
        return typedValue.data != 0;
    }

    private StyleUtils() {
    }

}
