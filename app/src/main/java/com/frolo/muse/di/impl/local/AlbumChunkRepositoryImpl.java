package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.repository.AlbumChunkRepository;

import java.util.LinkedHashMap;
import java.util.Map;

import io.reactivex.Single;


public class AlbumChunkRepositoryImpl
        extends SongRepositoryImpl
        implements AlbumChunkRepository {

    private final Map<String, String> mSortOrders;

    public AlbumChunkRepositoryImpl(final Context context) {
        super(context);
        this.mSortOrders = new LinkedHashMap<String, String>(2, 1f) {{
            put(SongQuery.Sort.BY_TITLE,
                    context.getString(R.string.sort_by_name));

            put(SongQuery.Sort.BY_ARTIST,
                    context.getString(R.string.sort_by_artist));
        }};
    }

    @Override
    public Single<Map<String, String>> getSortOrders() {
        return Single.just(mSortOrders);
    }
}
