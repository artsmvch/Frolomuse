package com.frolo.muse.engine;


import java.io.Serializable;

/**
 * Represents metadata of an {@link AudioSource}.
 * This contains methods for getting information such as title, artist, album, etc.
 */
public interface AudioMetadata extends Serializable {

    String getTitle();

    long getArtistId();

    String getArtist();

    long getAlbumId();

    String getAlbum();

    String getGenre();

    int getDuration();

    int getYear();

    int getTrackNumber();

}
