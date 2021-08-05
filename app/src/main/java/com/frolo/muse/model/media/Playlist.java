package com.frolo.muse.model.media;

import java.io.Serializable;
import java.util.Objects;


public class Playlist implements Media, Serializable {

    private final long id;
    private final boolean isFromSharedStorage;
    private final String source;
    private final String name;
    private final long dateAdded;
    private final long dateModified;

    public Playlist(long id, boolean isFromSharedStorage, String source, String name,
                    /* in seconds */ long dateAdded, /* in seconds */ long dateModified) {
        this.id = id;
        this.isFromSharedStorage = isFromSharedStorage;
        this.source = name != null ? source : "";
        this.name = name != null ? name : "";
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
    }

    public Playlist(Playlist toCopy) {
        this.id = toCopy.id;
        this.isFromSharedStorage = toCopy.isFromSharedStorage;
        this.source = toCopy.source;
        this.name = toCopy.name;
        this.dateAdded = toCopy.dateAdded;
        this.dateModified = toCopy.dateModified;
    }

    /**
     * Indicates whether this playlist is from the shared playlist storage.
     * Shared playlist storage means that the playlists from there can be accessed
     * and managed by other applications.
     * @return true if this playlist is from the shared playlist storage
     */
    public boolean isFromSharedStorage() {
        return isFromSharedStorage;
    }

    /**
     * Returns playlist file.
     * @return playlist file.
     */
    public String getSource() {
        return source;
    }

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
                && Objects.equals(name, another.name)
                && isFromSharedStorage == another.isFromSharedStorage
                && Objects.equals(source, another.source)
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

    public long getDateAddedMillis() {
        return dateAdded * 1000;
    }

    public long getDateModified() {
        return dateModified;
    }

    public long getDateModifiedMillis() {
        return dateModified * 1000;
    }
}
