package com.frolo.muse.model.media;

import androidx.annotation.NonNull;

import java.io.Serializable;


public class Playlist implements Media, Serializable {
    private final long id;
    private final String name;
    private final long dateAdded;
    private final long dateModified;

    public Playlist(long id, String name, long dateAdded, long dateModified) {
        this.id = id;
        this.name = name != null ? name : "";
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
    }

    public Playlist(Playlist toCopy) {
        this.id = toCopy.id;
        this.name = toCopy.name;
        this.dateAdded = toCopy.dateAdded;
        this.dateModified = toCopy.dateModified;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getKind() {
        return Media.PLAYLIST;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof Playlist)) return false;
        Playlist another = (Playlist) obj;
        return id == another.id
                && name.equals(another.name)
                && dateAdded == another.dateAdded
                && dateModified == another.dateModified;
    }

    @Override
    public int hashCode() {
        return (int) getId();
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public long getDateModified() {
        return dateModified;
    }
}
