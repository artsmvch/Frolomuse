package com.frolo.muse.di.impl.remote.lyrics;

import androidx.annotation.NonNull;

import com.frolo.muse.model.lyrics.Lyrics;

import java.util.ArrayList;
import java.util.List;


/**
 * A composite lyrics api that contains a collection of other APIs in it. When fetching lyrics,
 * if one of the APIs fails, the next one from the collection tries. The order of fetching is
 * defined by the passed list.
 */
final class LyricsApiChain implements LyricsApi {
    @NonNull
    private final List<LyricsApi> mApiList;

    LyricsApiChain(@NonNull List<LyricsApi> list) {
        mApiList = new ArrayList<>(list);
    }

    @Override
    public Lyrics getLyrics(String artistName, String songName) throws Exception {
        Lyrics result = null;
        Exception error = null;

        int index = 0;
        while (index < mApiList.size() && result == null) {
            LyricsApi api = mApiList.get(index);
            try {
                result = api.getLyrics(artistName, songName);
            } catch (Exception e) {
                error = e;
            }
            index++;
        }

        if (result != null) {
            return result;
        }

//        if (error == null) {
//            error = new NullPointerException();
//        }

        error = new NullPointerException();

        throw error;
    }
}
