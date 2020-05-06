package com.frolo.muse.model.media;


import java.io.Serializable;


public interface Song extends Media, Serializable {

    String getSource();

    String getTitle();

    long getArtistId();

    String getArtist();

    long getAlbumId();

    String getAlbum();

    String getGenre();

    /**
     * Returns duration of the song in milliseconds.
     * @return duration of the song in milliseconds
     */
    int getDuration();

    int getYear();

    /**
     * Represents the track number of this song on the album, if any.
     * This number encodes both the track number and the disc number.
     * For multi-disc sets, this number will be 1xxx for tracks on the first disc, 2xxx for tracks on the second disc, etc.
     * @return the track number of this on the album
     */
    int getTrackNumber();

}
