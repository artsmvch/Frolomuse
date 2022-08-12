package com.frolo.muse.di.impl.local;

import android.net.Uri;


@Deprecated
/* package-private */ final class Query {

    /**
     * Generates an exception that indicates a null cursor returned for the queried uri.
     * @param uri uri for which the query returned null
     * @return a generated exception
     */
    static Exception genNullCursorErr(Uri uri) {
        return new IllegalArgumentException("Query to " + uri + " returned null cursor");
    }

    private Query() {
    }
}
