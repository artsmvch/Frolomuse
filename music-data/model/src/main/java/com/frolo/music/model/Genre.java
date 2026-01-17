package com.frolo.music.model;

import androidx.annotation.NonNull;

import java.io.Serializable;


public final class Genre implements Media, Serializable {
    private final MediaId mediaId;
    private final String name;

    public Genre(long id, String name) {
        this.mediaId = MediaId.createLocal(Media.GENRE, id);
        this.name = name != null ? name : "";
    }

    @Override
    public MediaId getMediaId() {
        return mediaId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof Genre)) return false;
        Genre another = (Genre) obj;
        return mediaId.equals(another.mediaId)
                && name.equals(another.name);
    }

    @Override
    public int hashCode() {
        return mediaId.hashCode();
    }

    @NonNull
    public String getName() {
        return name;
    }
}
