package com.frolo.muse.di.impl.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

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

    @Override
    public String getLocalizedName() {
        return mContext.getString(mNameStringId);
    }

    @Override
    public String getKey() {
        return mKey;
    }

}
