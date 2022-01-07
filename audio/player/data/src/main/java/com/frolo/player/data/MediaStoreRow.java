package com.frolo.player.data;

import android.net.Uri;

import androidx.annotation.NonNull;


/**
 * Represents a row from the local MediaStore with the specified {@link MediaStoreRow#getUri()}.
 */
public interface MediaStoreRow {
    @NonNull
    Uri getUri();
}
