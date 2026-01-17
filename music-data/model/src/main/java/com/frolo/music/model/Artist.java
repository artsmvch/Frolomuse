package com.frolo.music.model;

import androidx.annotation.NonNull;

import java.io.Serializable;


public final class Artist implements Media, Serializable {
    private final MediaId mediaId;
    private final String name;
    private final int numberOfTracks;
    private final int numberOfAlbums;

    public Artist(long id, String name, int numberOfTracks, int numberOfAlbums) {
        this.mediaId = MediaId.createLocal(Media.ARTIST, id);
        this.name = name != null ? name : "";
        this.numberOfTracks = numberOfTracks;
        this.numberOfAlbums = numberOfAlbums;
    }

    @Override
    public MediaId getMediaId() {
        return mediaId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof Artist)) return false;
        Artist another = (Artist) obj;
        return mediaId.equals(another.mediaId)
                && name.equals(another.name)
                && numberOfTracks == another.numberOfTracks
                && numberOfAlbums == another.numberOfAlbums;
    }

    @Override
    public int hashCode() {
        return mediaId.hashCode();
    }

    @NonNull
    public String getName() {
        return name;
    }

    public int getNumberOfTracks() {
        return numberOfTracks;
    }

    public int getNumberOfAlbums() {
        return numberOfAlbums;
    }
}
