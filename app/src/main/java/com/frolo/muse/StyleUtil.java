package com.frolo.muse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;


public final class StyleUtil {
    private StyleUtil() {
    }

    @ColorInt
    public static int getVisualizerColor(@NonNull Context context) {
        int[] attrs = { R.attr.colorSecondary };
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int color = ta.getColor(0, Color.WHITE);
        ta.recycle();
        return color;
    }

    @ColorInt
    public static int getIconTintColor(@NonNull Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.iconImageTint, typedValue, true);
        return typedValue.data;
    }

    @SuppressLint("ResourceType")
    @ColorInt
    public static int[] getModeColors(@NonNull Context context) {
        int[] colors = new int[2];
        int[] attrs = { R.attr.modeOnTint, R.attr.modeOffTint};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        colors[0] = ta.getColor(0, Color.RED);
        colors[1] = ta.getColor(1, Color.LTGRAY);
        ta.recycle();
        return colors;
    }

    @ColorInt
    public static int getHighlightColor(@NonNull Context context) {
        int[] attrs = { R.attr.colorSecondaryVariant };
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int color = ta.getColor(0, Color.YELLOW);
        ta.recycle();
        return color;
    }

    @ColorInt
    public static int getTextColor(@NonNull Context context) {
        int[] attrs = { android.R.attr.textColor };
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int color = ta.getColor(0, Color.BLACK);
        ta.recycle();
        return color;
    }

    @ColorInt
    public static int getColorContextual(@NonNull Context context) {
        int[] attrs = { R.attr.colorSecondary };
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int color = ta.getColor(0, Color.BLACK);
        ta.recycle();
        return color;
    }

    @ColorInt
    public static int getColorOnContextual(@NonNull Context context) {
        int[] attrs = { R.attr.colorOnSecondary };
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int color = ta.getColor(0, Color.WHITE);
        ta.recycle();
        return color;
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

}
