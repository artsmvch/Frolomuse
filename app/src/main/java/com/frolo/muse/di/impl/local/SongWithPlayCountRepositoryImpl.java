package com.frolo.muse.di.impl.local;

import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.media.SongWithPlayCount;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.SongRepository;
import com.frolo.muse.repository.SongWithPlayCountRepository;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;


public class SongWithPlayCountRepositoryImpl extends BaseMediaRepository<SongWithPlayCount> implements SongWithPlayCountRepository {

    private final SongRepository mDelegate;

    private final Function<List<Song>, Publisher<List<SongWithPlayCount>>> MAPPER =
        new Function<List<Song>, Publisher<List<SongWithPlayCount>>>() {
            @Override
            public Publisher<List<SongWithPlayCount>> apply(List<Song> songs) {
                final List<Flowable<SongWithPlayCount>> sources = new ArrayList<>(songs.size());
                for (final Song song : songs) {
                    Flowable<SongWithPlayCount> source =
                            SongQuery.getSongWithPlayCount(getContext().getContentResolver(), song);

                    sources.add(source);
                }

                Function<Object[], List<SongWithPlayCount>> combiner =
                    new Function<Object[], List<SongWithPlayCount>>() {
                        @Override
                        public List<SongWithPlayCount> apply(Object[] objects) {
                            List<SongWithPlayCount> items = new ArrayList<>(objects.length);
                            for (Object obj : objects) {
                                items.add((SongWithPlayCount) obj);
                            }
                            return items;
                        }
                    };

                return Flowable.combineLatest(sources, combiner);
            }
        };

    public SongWithPlayCountRepositoryImpl(LibraryConfiguration configuration, SongRepository delegate) {
        super(configuration);
        mDelegate = delegate;
    }

    private Single<List<Song>> map(final Collection<SongWithPlayCount> items) {
        return Single.fromCallable(new Callable<List<Song>>() {
            @Override
            public List<Song> call() {
                ArrayList<Song> songs = new ArrayList<>(items.size());
                songs.addAll(items);
                return songs;
            }
        });
    }

    @Override
    protected List<SortOrder> blockingGetSortOrders() {
        return Collections.emptyList();
    }

    @Override
    public Flowable<List<SongWithPlayCount>> getAllItems() {
        return SongQuery.querySongsWithPlayCount(getContext().getContentResolver(), 0);
    }

    @Override
    public Flowable<List<SongWithPlayCount>> getAllItems(String sortOrder) {
        return mDelegate.getAllItems(sortOrder).switchMap(MAPPER);
    }

    @Override
    public Flowable<List<SongWithPlayCount>> getFilteredItems(String namePiece) {
        return mDelegate.getFilteredItems(namePiece).switchMap(MAPPER);
    }

    @Override
    public Flowable<SongWithPlayCount> getItem(long id) {
        return mDelegate.getItem(id)
            .switchMap(new Function<Song, Publisher<SongWithPlayCount>>() {
                @Override
                public Publisher<SongWithPlayCount> apply(final Song song) {
                    return SongQuery.getSongWithPlayCount(getContext().getContentResolver(), song);
                }
            });
    }

    @Override
    public Completable delete(SongWithPlayCount item) {
        return mDelegate.delete(item);
    }

    @Override
    public Completable delete(Collection<SongWithPlayCount> items) {
        return map(items).flatMapCompletable(new Function<Collection<Song>, CompletableSource>() {
            @Override
            public CompletableSource apply(Collection<Song> songs) {
                return mDelegate.delete(songs);
            }
        });
    }

    @Override
    public Completable addToPlaylist(Playlist playlist, SongWithPlayCount item) {
        return mDelegate.addToPlaylist(playlist, item);
    }

    @Override
    public Completable addToPlaylist(final Playlist playlist, Collection<SongWithPlayCount> items) {
        return map(items).flatMapCompletable(new Function<Collection<Song>, CompletableSource>() {
            @Override
            public CompletableSource apply(Collection<Song> songs) {
                return mDelegate.addToPlaylist(playlist, songs);
            }
        });
    }

    @Override
    public Single<List<Song>> collectSongs(SongWithPlayCount item) {
        return map(Collections.singleton(item));
    }

    @Override
    public Single<List<Song>> collectSongs(Collection<SongWithPlayCount> items) {
        return map(items);
    }

    @Override
    public Flowable<List<SongWithPlayCount>> getAllFavouriteItems() {
        return mDelegate.getAllFavouriteItems().switchMap(MAPPER);
    }

    @Override
    public Flowable<Boolean> isFavourite(SongWithPlayCount item) {
        return mDelegate.isFavourite(item);
    }

    @Override
    public Completable changeFavourite(SongWithPlayCount item) {
        return mDelegate.changeFavourite(item);
    }

    @Override
    public Single<Boolean> isShortcutSupported(SongWithPlayCount item) {
        return mDelegate.isShortcutSupported(item);
    }

    @Override
    public Completable createShortcut(SongWithPlayCount item) {
        return mDelegate.createShortcut(item);
    }

}
