package com.frolo.muse.model;

import android.content.Context;

import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;

import com.frolo.muse.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class ThemeUtils {

    @Nullable
    @StyleRes
    public static Integer getStyleResourceId(@NotNull Theme theme) {
        switch (theme) {
            case LIGHT_BLUE:            return R.style.AppTheme_Light_Blue;
            case LIGHT_PINK:            return R.style.AppTheme_Light_Pink;
            case DARK_BLUE:             return R.style.Base_AppTheme_Dark_Blue;
            case DARK_BLUE_ESPECIAL:    return R.style.Base_AppTheme_Dark_Blue_Especial;
            case DARK_PURPLE:           return R.style.Base_AppTheme_Dark_Purple;
            case DARK_ORANGE:           return R.style.Base_AppTheme_Dark_Yellow;
            case DARK_GREEN:            return R.style.Base_AppTheme_Dark_Green;
            case DARK_FANCY:            return R.style.Base_AppTheme_Dark_Fancy;
            default:                    return null;
        }
    }

    @Nullable
    @StringRes
    public static Integer getNameResourceId(@NotNull Theme theme) {
        switch (theme) {
            case LIGHT_BLUE:            return R.string.light_blue_theme;
            case LIGHT_PINK:            return R.string.light_pink_theme;
            case DARK_BLUE:             return R.string.dark_blue_theme;
            case DARK_BLUE_ESPECIAL:    return R.string.dark_especial_theme;
            case DARK_PURPLE:           return R.string.dark_purple_theme;
            case DARK_ORANGE:           return R.string.dark_orange_theme;
            case DARK_GREEN:            return R.string.dark_green_theme;
            case DARK_FANCY:            return R.string.fancy_theme;
            default:                    return null;
        }
    }

    @NotNull
    public static Context createThemedContext(@NotNull Context context, @NotNull Theme theme) {
        Integer themeResId = getStyleResourceId(theme);
        if (themeResId == null) {
            return context;
        }
        return new ContextThemeWrapper(context, themeResId);
    }

    private ThemeUtils() {
    }
}
