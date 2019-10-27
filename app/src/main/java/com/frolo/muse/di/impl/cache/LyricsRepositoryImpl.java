package com.frolo.muse.di.impl.cache;

import androidx.collection.LruCache;

import com.frolo.muse.model.lyrics.Lyrics;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.repository.LyricsRepository;

import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class LyricsRepositoryImpl implements LyricsRepository {

    private final LyricsRepository origin;
    private final LruCache<Song, Lyrics> cache;

    public LyricsRepositoryImpl(LyricsRepository origin, int maxSize) {
        this.origin = origin;
        this.cache = new LruCache<>(maxSize);
    }

    private Single<Lyrics> getCachedLyrics(final Song song) {
        return Single.fromCallable(new Callable<Lyrics>() {
            @Override
            public Lyrics call() throws Exception {
                return cache.get(song);
            }
        });
    }

    @Override
    public Single<Lyrics> getLyrics(final Song song) {
        return getCachedLyrics(song)
                .onErrorResumeNext(origin.getLyrics(song))
                .doOnSuccess(new Consumer<Lyrics>() {
                    @Override
                    public void accept(Lyrics lyrics) throws Exception {
                        // caching it
                        cache.put(song, lyrics);
                    }
                });
    }

    @Override
    public Completable setLyrics(final Song song, final Lyrics lyrics) {
        return origin.setLyrics(song, lyrics)
                .andThen(Completable.fromAction(new Action() {
                    @Override
                    public void run() {
                        cache.put(song, lyrics);
                    }
                }));
    }
}
