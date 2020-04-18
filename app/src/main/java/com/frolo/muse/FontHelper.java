package com.frolo.muse;

import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;


public class FontHelper {

    public static void overrideDefaultFont(
        Context context,
        String staticTypefaceFieldName,
        String fontAssetName
    ) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(),
                fontAssetName);
        replaceFont(staticTypefaceFieldName, regular);
    }

    private static void replaceFont(
        String staticTypefaceFieldName,
        final Typeface newTypeface
    ) {
        try {
            final Field staticField = Typeface.class
                    .getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (Throwable ignored) {
        }
    }
}