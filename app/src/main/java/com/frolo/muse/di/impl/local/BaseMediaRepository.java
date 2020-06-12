package com.frolo.muse.di.impl.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.model.media.Media;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.MediaRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


/**
 * Base media repository that contains some common useful methods.
 * @param <E> type of media
 */
abstract class BaseMediaRepository<E extends Media> implements MediaRepository<E> {

    private final Context mContext;

    BaseMediaRepository(@NonNull Context context) {
        mContext = context;
    }

    @NonNull
    protected final Context getContext() {
        return mContext;
    }

    @NonNull
    protected final SortOrder createSortOrder(String key, @StringRes int nameStringId) {
        return new SortOrderImpl(mContext, key, nameStringId);
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

}
