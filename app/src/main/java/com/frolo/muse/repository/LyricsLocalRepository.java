package com.frolo.muse.repository;

import com.frolo.muse.model.lyrics.Lyrics;
import com.frolo.muse.model.media.Song;

import io.reactivex.Completable;
import io.reactivex.Single;


public interface LyricsLocalRepository {
    Single<Lyrics> getLyrics(Song song);
    Completable setLyrics(Song song, Lyrics lyrics);
}
