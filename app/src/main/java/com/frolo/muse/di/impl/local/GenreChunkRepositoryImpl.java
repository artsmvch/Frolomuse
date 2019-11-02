package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.repository.GenreChunkRepository;

import java.util.LinkedHashMap;
import java.util.Map;

import io.reactivex.Single;


public class GenreChunkRepositoryImpl
        extends SongRepositoryImpl
        implements GenreChunkRepository {

    private final static String[] SORT_ORDER_KEYS = {
            SongQuery.Sort.BY_DEFAULT,
            SongQuery.Sort.BY_TITLE,
            SongQuery.Sort.BY_ALBUM,
            SongQuery.Sort.BY_ARTIST
    };

    // Returns sort order candidate if valid or default
    static String validateSortOrder(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(
                candidate,
                SORT_ORDER_KEYS,
                SongQuery.Sort.BY_DEFAULT);
    }

    private final Map<String, String> mSortOrders;

    public GenreChunkRepositoryImpl(final Context context) {
        super(context);
        mSortOrders = new LinkedHashMap<String, String>(4, 1f) {{
            put(SongQuery.Sort.BY_DEFAULT,
                    context.getString(R.string.sort_by_default));

            put(SongQuery.Sort.BY_TITLE,
                    context.getString(R.string.sort_by_name));

            put(SongQuery.Sort.BY_ALBUM,
                    context.getString(R.string.sort_by_album));

            put(SongQuery.Sort.BY_ARTIST,
                    context.getString(R.string.sort_by_artist));
        }};
    }

    @Override
    public Single<Map<String, String>> getSortOrders() {
        return Single.just(mSortOrders);
    }

}
