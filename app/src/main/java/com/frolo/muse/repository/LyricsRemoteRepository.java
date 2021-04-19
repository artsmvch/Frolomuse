package com.frolo.muse.repository;

import com.frolo.muse.model.lyrics.Lyrics;
import com.frolo.muse.model.media.Song;

import io.reactivex.Single;


public interface LyricsRemoteRepository {
    /**
     * Tests this repository to see if it can actually search for lyrics. If this method returns true,
     * then we expect {@link LyricsRemoteRepository#getLyrics(Song)} to return valid lyrics for most songs.
     * @return true if this repository can search for lyrics, false - otherwise.
     */
    Single<Boolean> test();
    Single<Lyrics> getLyrics(Song song);
}
