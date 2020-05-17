package com.frolo.muse;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;


/**
 * Helper class for convenient reading of theme attributes.
 */
public final class StyleUtil {
    private StyleUtil() {
    }

    @ColorInt
    public static int readColorAttrValue(@NonNull Context context, @AttrRes int attrId) {
        final TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrId, typedValue, true);
        return typedValue.data;
    }

    @StyleRes
    public static int readStyleAttrValue(@NonNull Context context, @AttrRes int attrId) {
        final TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrId, typedValue, true);
        return typedValue.resourceId;
    }

    public static float readDimenAttrValue(@NonNull Context context, @AttrRes int attrId) {
        final TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrId, typedValue, true);
        return typedValue.getDimension(context.getResources().getDisplayMetrics());
    }

    @Nullable
    public static Drawable readDrawableAttrValue(@NonNull Context context, @AttrRes int attrId) {
        final TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrId, typedValue, true);
        return ContextCompat.getDrawable(context, typedValue.resourceId);
    }

    public static boolean readBooleanAttrValue(@NonNull Context context, @AttrRes int attrId) {
        final TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrId, typedValue, true);
        return typedValue.data != 0;
    }

}
