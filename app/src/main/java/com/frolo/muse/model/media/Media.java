package com.frolo.muse.model.media;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface Media {
    @IntDef({SONG, ALBUM, ARTIST, GENRE, PLAYLIST, MY_FILE, MEDIA_FILE})
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

    long NO_ID = -1;

    /**
     * Should return an id that identifies the media object
     * @return non-negative number
     */
    long getId();

    /**
     * Should return one of {@link Kind}
     * @return type of the media
     */
    @Kind int getKind();

    boolean equals(Object other);
}
