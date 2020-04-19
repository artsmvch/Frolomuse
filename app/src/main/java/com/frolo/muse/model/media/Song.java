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

    int getDuration();

    int getYear();

    int getTrackNumber();

}
