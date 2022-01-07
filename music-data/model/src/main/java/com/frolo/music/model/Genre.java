package com.frolo.music.model;

import androidx.annotation.NonNull;

import java.io.Serializable;


public final class Genre implements Media, Serializable {
    private final long id;
    private final String name;

    public Genre(long id, String name) {
        this.id = id;
        this.name = name != null ? name : "";
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getKind() {
        return GENRE;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof Genre)) return false;
        Genre another = (Genre) obj;
        return id == another.id
                && name.equals(another.name);
    }

    @Override
    public int hashCode() {
        return (int) getId();
    }

    @NonNull
    public String getName() {
        return name;
    }
}
