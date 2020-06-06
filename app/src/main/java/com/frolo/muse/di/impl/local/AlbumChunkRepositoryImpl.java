package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.AlbumChunkRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;


public class AlbumChunkRepositoryImpl extends SongRepositoryImpl implements AlbumChunkRepository {

    private final List<SortOrder> mSortOrders;

    public AlbumChunkRepositoryImpl(final Context context) {
        super(context);
        this.mSortOrders = new ArrayList<SortOrder>(4) {{
            add(new SortOrderImpl(getContext(), SongQuery.Sort.BY_DEFAULT, R.string.sort_by_default));
            add(new SortOrderImpl(getContext(), SongQuery.Sort.BY_TITLE, R.string.sort_by_name));
            add(new SortOrderImpl(getContext(), SongQuery.Sort.BY_ARTIST, R.string.sort_by_artist));
            add(new SortOrderImpl(getContext(), SongQuery.Sort.BY_DURATION, R.string.sort_by_duration));
        }};
    }

    @Override
    public Single<List<SortOrder>> getSortOrders() {
        return Single.just(mSortOrders);
    }
}
