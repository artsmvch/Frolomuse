package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;


final class Query {

    private final static Object NOTHING = new Object();
    private final static Handler HANDLER = new Handler(Looper.getMainLooper());

    interface Builder<T> {
        T build(Cursor cursor, String[] projection);
    }

    /**
     * Generates an exception that indicates a null cursor returned for the queried uri.
     * @param uri uri for which the query returned null
     * @return a generated exception
     */
    /*package*/ static Exception genNullCursorErr(Uri uri) {
        return new IllegalArgumentException(
                "Query returned null cursor for uri: " + uri);
    }

    /**
     * Returns a valid sort order for content resolvers.
     * If the given 'candidate' is null or empty then the sort order is null.
     * Otherwise the 'candidate' is returned.
     * @param candidate to validate
     * @return a valid sort order
     */
    @Nullable
    /*package*/ static String validateSortOrder(@Nullable String candidate) {
        if (candidate == null) {
            return null;
        }
        if (candidate.isEmpty()) {
            return null;
        }
        return candidate;
    }

    /**
     * Creates a flowable that emits an object every time some data changed on the uri provided.
     * An additional object is emitted on the subscribe.
     * @param contentResolver content resolver
     * @param uri uri to observe
     * @return a flowable source
     */
    /*package*/ static Flowable<Object> createFlowable(
            final ContentResolver contentResolver,
            final Uri uri
    ) {
        return Flowable.create(new FlowableOnSubscribe<Object>() {
            @Override
            public void subscribe(final FlowableEmitter<Object> emitter) {
                if (!emitter.isCancelled()) {
                    final boolean notifyForDescendants = true;
                    final ContentObserver trigger = new ContentObserver(HANDLER) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            if (!emitter.isCancelled()) {
                                emitter.onNext(NOTHING);
                            }
                        }
                    };

                    contentResolver.registerContentObserver(
                            uri,
                            notifyForDescendants,
                            trigger);

                    emitter.setDisposable(Disposables.fromAction(new Action() {
                        @Override
                        public void run() {
                            contentResolver.unregisterContentObserver(trigger);
                        }
                    }));
                }

                if (!emitter.isCancelled()) {
                    emitter.onNext(NOTHING);
                }
            }
        }, BackpressureStrategy.LATEST);
    }

    /**
     * Creates a flowable that emits an object every time some data changed on the uris provided.
     * An additional object is emitted on the subscribe.
     * @param contentResolver content resolver
     * @param uris uris to observe
     * @return a flowable source
     */
    /*package*/ static Flowable<Object> createFlowable(
            final ContentResolver contentResolver,
            final List<Uri> uris
    ) {
        return Flowable.create(new FlowableOnSubscribe<Object>() {
            @Override
            public void subscribe(final FlowableEmitter<Object> emitter) {
                if (!emitter.isCancelled()) {
                    final boolean notifyForDescendants = true;
                    final List<ContentObserver> triggers = new ArrayList<>(uris.size());

                    for (Uri uri : uris) {
                        final ContentObserver trigger = new ContentObserver(HANDLER) {
                            @Override
                            public void onChange(boolean selfChange, Uri uri) {
                                if (!emitter.isCancelled()) {
                                    emitter.onNext(NOTHING);
                                }
                            }
                        };

                        contentResolver.registerContentObserver(
                                uri,
                                notifyForDescendants,
                                trigger);

                        triggers.add(trigger);
                    }

                    emitter.setDisposable(Disposables.fromAction(new Action() {
                        @Override
                        public void run() {
                            for (ContentObserver trigger : triggers) {
                                contentResolver.unregisterContentObserver(trigger);
                            }
                        }
                    }));
                }

                if (!emitter.isCancelled()) {
                    emitter.onNext(NOTHING);
                }
            }
        }, BackpressureStrategy.LATEST);
    }

    /*package*/ static <T> Flowable<T> createFlowable(
            final ContentResolver contentResolver,
            final Uri uri,
            Callable<T> callable
    ) {
        final Maybe<T> maybe = Maybe.fromCallable(callable);

        return createFlowable(contentResolver, uri)
                .flatMapMaybe(new Function<Object, MaybeSource<? extends T>>() {
                    @Override
                    public MaybeSource<? extends T> apply(Object o) {
                        return maybe;
                    }
                });
    }

    /*package*/ static <T> Flowable<T> createFlowable(
            final ContentResolver contentResolver,
            final List<Uri> uris,
            Callable<T> callable
    ) {
        final Maybe<T> maybe = Maybe.fromCallable(callable);

        return createFlowable(contentResolver, uris)
                .flatMapMaybe(new Function<Object, MaybeSource<? extends T>>() {
                    @Override
                    public MaybeSource<? extends T> apply(Object o) {
                        return maybe;
                    }
                });
    }

    /*package*/ static <T> Flowable<List<T>> query(
            final ContentResolver resolver,
            final Uri uri,
            final String[] projection,
            final String selection,
            final String[] selectionArgs,
            final String sortOrder,
            final Builder<T> builder
    ) {
        return createFlowable(
                resolver,
                uri,
                new Callable<List<T>>() {
                    @Override
                    public List<T> call() throws Exception {
                        Cursor cursor = resolver.query(
                                uri, projection, selection, selectionArgs, validateSortOrder(sortOrder));

                        if (cursor == null) {
                            throw genNullCursorErr(uri);
                        }

                        List<T> items = new ArrayList<>(cursor.getCount());

                        try {
                            if (cursor.moveToFirst()) {
                                do {
                                    items.add(builder.build(cursor, projection));
                                } while (cursor.moveToNext());
                            }
                        } finally {
                            cursor.close();
                        }

                        return items;
                    }
                }
        );
    }

    /*package*/ static <T> Flowable<T> querySingle(
            final ContentResolver resolver,
            final Uri uri,
            final String[] projection,
            final long itemId,
            final Builder<T> builder
    ) {
        final Uri itemUri = ContentUris.withAppendedId(uri, itemId);
        return createFlowable(
                resolver,
                itemUri,
                new Callable<T>() {
                    @Override
                    public T call() throws Exception {
                        String selection = null;
                        String[] selectionArgs = null;
                        String sortOrder = null;

                        Cursor cursor = resolver.query(
                                itemUri, projection, selection, selectionArgs, sortOrder);

                        if (cursor == null) {
                            throw genNullCursorErr(itemUri);
                        }

                        T item = null;

                        try {
                            if (cursor.moveToFirst()) {
                                item = builder.build(cursor, projection);
                            }
                        } finally {
                            cursor.close();
                        }

                        if (item == null) {
                            throw new IllegalArgumentException("No item found for id: " + itemId);
                        }

                        return item;
                    }
                }
        );
    }

    private Query() {
    }
}
