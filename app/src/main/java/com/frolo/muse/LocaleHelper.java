package com.frolo.muse;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import java.util.Locale;


public final class LocaleHelper {

    @NonNull
    public static Context applyLanguage(@NonNull Context context, @NonNull String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        Configuration newConfiguration = new Configuration(configuration);

        return context.createConfigurationContext(newConfiguration);
    }

    private LocaleHelper() {
    }
}
