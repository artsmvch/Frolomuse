package com.frolo.muse;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;


public class LocalizedMessageException extends RuntimeException {

    private final String localizedMessage;

    public LocalizedMessageException(@NonNull Context context, @StringRes int messageId, @Nullable Throwable cause) {
        super(context.getString(messageId), cause);
        localizedMessage = getMessage();
    }

    public LocalizedMessageException(@NonNull Context context, @StringRes int messageId) {
        super(context.getString(messageId));
        localizedMessage = getMessage();
    }

    public String getLocalizedMessage() {
        return localizedMessage;
    }
}
