package com.frolo.music.model;

import androidx.annotation.NonNull;

import java.io.Serializable;


public final  class Album implements Media, Serializable {
    private final long id;
    private final String name;
    private final String artist;
    private final int numberOfSongs;

    public Album(long id, String name, String artist, int numberOfSongs) {
        this.id = id;
        this.name = name != null ? name : "";
        this.artist = artist != null ? artist : "";
        this.numberOfSongs = numberOfSongs;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Album)) {
            return false;
        }
        Album another = (Album) obj;
        return id == another.id
                && name.equals(another.name)
                && artist.equals(another.artist)
                && numberOfSongs == another.numberOfSongs;
    }

    @Override
    public int hashCode() {
        return (int) getId();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getKind() {
        return ALBUM;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getArtist() {
        return artist;
    }

    public int getNumberOfSongs() {
        return numberOfSongs;
    }
}
