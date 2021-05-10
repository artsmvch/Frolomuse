package com.frolo.muse;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.content.res.AppCompatResources;


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

    @Nullable
    public static ColorStateList readColorStateListAttrValue(@NonNull Context context, @AttrRes int attrId) {
        final TypedValue typedValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(attrId, typedValue, false)) {
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
        return AppCompatResources.getDrawable(context, typedValue.resourceId);
    }

    public static boolean readBooleanAttrValue(@NonNull Context context, @AttrRes int attrId) {
        final TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrId, typedValue, true);
        return typedValue.data != 0;
    }

}
