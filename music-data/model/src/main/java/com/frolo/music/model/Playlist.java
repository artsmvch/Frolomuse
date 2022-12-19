package com.frolo.music.model;

import java.io.Serializable;
import java.util.Objects;


public final class Playlist implements Media, Serializable {
    public static class Identifier implements Serializable {
        private final long id;
        private final boolean isFromSharedStorage;

        Identifier(long id, boolean isFromSharedStorage) {
            this.id = id;
            this.isFromSharedStorage = isFromSharedStorage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Identifier identifier = (Identifier) o;
            return id == identifier.id && isFromSharedStorage == identifier.isFromSharedStorage;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, isFromSharedStorage);
        }
    }

    private final Identifier identifier;
    private final String source;
    private final String name;
    private final long dateAdded;
    private final long dateModified;

    public Playlist(long id, boolean isFromSharedStorage, String source, String name,
                    /* in seconds */ long dateAdded, /* in seconds */ long dateModified) {
        this.identifier = new Identifier(id, isFromSharedStorage);
        this.source = name != null ? source : "";
        this.name = name != null ? name : "";
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
    }

    public Playlist(Playlist toCopy) {
        this.identifier = toCopy.identifier;
        this.source = toCopy.source;
        this.name = toCopy.name;
        this.dateAdded = toCopy.dateAdded;
        this.dateModified = toCopy.dateModified;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * Indicates whether this playlist is from the shared playlist storage.
     * Shared playlist storage means that the playlists from there can be accessed
     * and managed by other applications.
     * @return true if this playlist is from the shared playlist storage
     */
    public boolean isFromSharedStorage() {
        return identifier.isFromSharedStorage;
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
        return identifier.id;
    }

    @Override
    public int getKind() {
        return PLAYLIST;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof Playlist)) return false;
        Playlist another = (Playlist) obj;
        return Objects.equals(identifier, another.identifier)
                && Objects.equals(name, another.name)
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
