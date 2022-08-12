package com.frolo.muse.di.impl.remote.lyrics;

import com.frolo.muse.model.lyrics.Lyrics;


interface LyricsApi {
    Lyrics getLyrics(String artistName, String songName) throws Exception;
}
