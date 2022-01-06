package com.frolo.player.data;

import android.net.Uri;

import org.jetbrains.annotations.NotNull;


/**
 * Represents a row from the local MediaStore with the specified {@link MediaStoreRow#getUri()}.
 */
public interface MediaStoreRow {
    @NotNull
    Uri getUri();
}
