package com.frolo.muse.model.media;

import androidx.annotation.Nullable;

import java.util.Objects;


public class AlbumArtConfig {
    private final Long id;
    private final String data;

    public AlbumArtConfig(
            @Nullable Long id,
            @Nullable String data) {
        this.id = id;
        this.data = data;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    @Nullable
    public String getData() {
        return data;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof AlbumArtConfig)) {
            return false;
        }

        AlbumArtConfig other = (AlbumArtConfig) obj;

        return Objects.equals(id, other.id)
                && Objects.equals(data, other.data);
    }
}
