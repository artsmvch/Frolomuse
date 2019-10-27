package com.frolo.muse.di.impl.remote;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.model.lyrics.Lyrics;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.repository.LyricsRepository;

import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Single;

public class LyricsRepositoryImpl implements LyricsRepository {

    private final GetLyricsApi api = new GetLyricsApi(BuildConfig.GET_LYRICS_API_KEY);

    @Override
    public Single<Lyrics> getLyrics(final Song song) {
        return Single.fromCallable(new Callable<Lyrics>() {
            @Override
            public Lyrics call() throws Exception {
                return api.getLyrics(song.getArtist(), song.getTitle());
            }
        });
    }

    @Override
    public Completable setLyrics(Song song, Lyrics lyrics) {
        return Completable.error(new UnsupportedOperationException());
    }
}
