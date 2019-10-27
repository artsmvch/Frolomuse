package com.frolo.muse.model;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


public interface Library {
    int NONE = -1;
    int ALL_SONGS = 0;
    int ALBUMS = 1;
    int ARTISTS = 2;
    int GENRES = 3;
    int FAVOURITES = 4;
    int PLAYLISTS = 5;
    int FOLDERS = 6;
    int RECENTLY_ADDED = 7;
    int ALBUM = 8;
    int ARTIST = 9;
    int GENRE = 10;
    int PLAYLIST = 11;
    int MIXED = 12;

    @IntDef({ALL_SONGS, ALBUMS, ARTISTS, GENRES, FAVOURITES, PLAYLISTS,
            FOLDERS, RECENTLY_ADDED, ALBUM, ARTIST, GENRE, PLAYLIST, MIXED})
    @Target({ElementType.LOCAL_VARIABLE, ElementType.FIELD,
            ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Section { }
}
