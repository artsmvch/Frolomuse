package com.frolo.muse.di.impl.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.frolo.muse.Features;
import com.frolo.muse.LocaleHelper;
import com.frolo.muse.model.sort.SortOrder;


final class SortOrderImpl extends SortOrder {

    private final Context mContext;
    private final String mKey;
    @StringRes
    private final int mNameStringId;

    SortOrderImpl(@NonNull Context context, @Nullable String key, @StringRes int nameStringId) {
        mContext = context;
        mKey = key;
        mNameStringId = nameStringId;
    }

    @NonNull
    private Context getLocalizedContext() {
        if (Features.isLanguageChooserFeatureAvailable()) {
            return LocaleHelper.applyDefaultLanguage(mContext);
        } else {
            return mContext;
        }
    }

    @Override
    public String getLocalizedName() {
        return getLocalizedContext().getString(mNameStringId);
    }

    @Override
    public String getKey() {
        return mKey;
    }

}
