package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.frolo.muse.BuildConfig;
import com.frolo.music.model.Media;
import com.frolo.music.model.Song;
import com.frolo.music.model.SongFilter;
import com.frolo.music.model.SortOrder;
import com.frolo.music.repository.MediaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * Base media repository that contains some common useful methods.
 * @param <E> type of media
 */
abstract class BaseMediaRepository<E extends Media> implements MediaRepository<E> {

    private final LibraryConfiguration mConfiguration;

    BaseMediaRepository(@NonNull LibraryConfiguration configuration) {
        mConfiguration = configuration;
    }

    @NonNull
    protected final Context getContext() {
        return mConfiguration.getContext();
    }

    @NonNull
    protected final ContentResolver getContentResolver() {
        return mConfiguration.getContext().getContentResolver();
    }

    @NonNull
    protected final Flowable<SongFilter> getSongFilter() {
        return mConfiguration.getSongFilterProvider().getSongFilter();
    }

    @NonNull
    protected final SortOrder createSortOrder(String key, @StringRes int nameStringId) {
        return new SortOrderImpl(getContext(), key, nameStringId);
    }

    @NonNull
    protected final List<SortOrder> collectSortOrders(SortOrder... items) {
        final List<SortOrder> list = new ArrayList<>(items.length);

        for (SortOrder item : items) {

            // the list can contain only non-null items
            if (item == null) {
                continue;
            }

            // checking that the keys are distinct
            boolean hasKeyAlready = false;

            for (SortOrder sortOrder : list) {
                if (SortOrder.areKeysTheSame(sortOrder, item)) {
                    hasKeyAlready = true;

                    if (BuildConfig.DEBUG) {
                        // strict for debug builds only
                        throw new IllegalArgumentException("Duplicate keys found: " + item.getKey());
                    }

                    break;
                }
            }

            if (!hasKeyAlready) {
                list.add(item);
            }
        }

        return Collections.unmodifiableList(list);
    }

    /**
     * Returns a list of available sort orders for this repository.
     * NOTE: this method is blocking and should be called on a background thread.
     * @return a list of available sort orders
     */
    protected abstract List<SortOrder> blockingGetSortOrders();

    @Override
    public final Single<List<SortOrder>> getSortOrders() {
        return Single.fromCallable(new Callable<List<SortOrder>>() {
            @Override
            public List<SortOrder> call() throws Exception {
                final List<SortOrder> result = blockingGetSortOrders();
                return result != null ? result : Collections.emptyList();
            }
        }).subscribeOn(Schedulers.computation());
    }

    @Override
    public Single<List<Song>> collectSongs(Collection<E> items) {
        List<Single<List<Song>>> sources = new ArrayList<>(items.size());
        for (E item : items) {
            Single<List<Song>> source = collectSongs(item);
            sources.add(source);
        }
        Function<Object[], List<Song>> zipper = objects -> {
            List<Song> result = new ArrayList<>();
            for (Object obj : objects) {
                @SuppressWarnings("unchecked")
                List<Song> chunk = (List<Song>) obj;
                result.addAll(chunk);
            }
            return result;
        };
        return Single.zip(sources, zipper);
    }
}
