package com.frolo.player;

import java.io.Serializable;

/**
 * Represents metadata of an {@link AudioSource}.
 * This contains methods for getting information such as title, artist, album, etc.
 */
public interface AudioMetadata extends Serializable {

    AudioType getAudioType();

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
