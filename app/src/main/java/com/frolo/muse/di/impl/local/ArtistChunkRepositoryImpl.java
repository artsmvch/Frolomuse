package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.ArtistChunkRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;


public class ArtistChunkRepositoryImpl extends SongRepositoryImpl implements ArtistChunkRepository {

    private final static String[] SORT_ORDER_KEYS = {
        SongQuery.Sort.BY_DEFAULT,
        SongQuery.Sort.BY_TITLE,
        SongQuery.Sort.BY_ALBUM,
        SongQuery.Sort.BY_DURATION
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, SongQuery.Sort.BY_DEFAULT);
    }

    private final List<SortOrder> mSortOrders;

    public ArtistChunkRepositoryImpl(final Context context) {
        super(context);
        mSortOrders = new ArrayList<SortOrder>(4) {{
            add(new SortOrderImpl(getContext(), SongQuery.Sort.BY_DEFAULT, R.string.sort_by_default));
            add(new SortOrderImpl(getContext(), SongQuery.Sort.BY_TITLE, R.string.sort_by_name));
            add(new SortOrderImpl(getContext(), SongQuery.Sort.BY_ALBUM, R.string.sort_by_album));
            add(new SortOrderImpl(getContext(), SongQuery.Sort.BY_DURATION, R.string.sort_by_duration));
        }};
    }

    @Override
    public Single<List<SortOrder>> getSortOrders() {
        return Single.just(mSortOrders);
    }
}
