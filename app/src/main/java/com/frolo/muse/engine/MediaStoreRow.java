package com.frolo.muse.engine;

import android.net.Uri;

import org.jetbrains.annotations.NotNull;


/**
 * Represents a row from the local MediaStore with the specified {@link MediaStoreRow#getUri()}.
 */
public interface MediaStoreRow {
    @NotNull
    Uri getUri();
}
