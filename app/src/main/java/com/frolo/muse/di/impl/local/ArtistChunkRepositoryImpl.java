package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.repository.ArtistChunkRepository;

import java.util.LinkedHashMap;
import java.util.Map;

import io.reactivex.Single;


public class ArtistChunkRepositoryImpl
        extends SongRepositoryImpl
        implements ArtistChunkRepository {

    private final Map<String, String> mSortOrders;

    public ArtistChunkRepositoryImpl(final Context context) {
        super(context);
        mSortOrders = new LinkedHashMap<String, String>(2, 1f) {{
            put(SongQuery.Sort.BY_TITLE,
                    context.getString(R.string.sort_by_name));

            put(SongQuery.Sort.BY_ALBUM,
                    context.getString(R.string.sort_by_album));
        }};
    }

    @Override
    public Single<Map<String, String>> getSortOrders() {
        return Single.just(mSortOrders);
    }
}
