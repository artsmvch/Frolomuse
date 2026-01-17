package com.frolo.music.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public interface Media {
    @IntDef({NONE, SONG, ALBUM, ARTIST, GENRE, PLAYLIST, MY_FILE, MEDIA_FILE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Kind { }
    int NONE = -1;
    int SONG = 0;
    int ALBUM = 1;
    int ARTIST = 2;
    int GENRE = 3;
    int PLAYLIST = 4;
    @Deprecated
    int MY_FILE = 5;
    int MEDIA_FILE = 6;

    /**
     * Should return a unique identifier that encapsulates all information required
     * to uniquely identify a media item across different data sources.
     * @return a MediaId that uniquely identifies this media item
     */
    MediaId getMediaId();

    boolean equals(Object other);
}
