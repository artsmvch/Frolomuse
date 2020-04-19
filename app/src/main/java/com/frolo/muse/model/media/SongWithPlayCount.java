package com.frolo.muse.model.media;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;


public final class SongWithPlayCount implements Song, Serializable {
    private final Song song;
    private final int playCount;
    private final Long lastPlayTime;

    public SongWithPlayCount(Song song, int playCount, @Nullable Long lastPlayTime) {
        this.song = song;
        this.playCount = playCount;
        this.lastPlayTime = lastPlayTime;
    }

    public Song getSong() {
        return song;
    }

    public int getPlayCount() {
        return playCount;
    }

    @Nullable
    public Long getLastPlayTime() {
        return lastPlayTime;
    }

    public boolean hasLastPlayTime() {
        return lastPlayTime != null;
    }

    @Override
    public long getId() {
        return song.getId();
    }

    @Override
    public int getKind() {
        return song.getKind();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof SongWithPlayCount)) return false;
        SongWithPlayCount another = (SongWithPlayCount) obj;
        return playCount == another.playCount && Objects.equals(song, another.song);
    }

    @Override
    public String getSource() {
        return song.getSource();
    }

    @Override
    public String getTitle() {
        return song.getTitle();
    }

    @Override
    public String getArtist() {
        return song.getArtist();
    }

    @Override
    public String getAlbum() {
        return song.getAlbum();
    }

    @Override
    public long getAlbumId() {
        return song.getAlbumId();
    }

    @Override
    public int getDuration() {
        return song.getDuration();
    }

    @Override
    public int getYear() {
        return song.getYear();
    }

    @Override
    public String getGenre() {
        return song.getGenre();
    }

    @Override
    public long getArtistId() {
        return song.getArtistId();
    }

    @Override
    public int getTrackNumber() {
        return song.getTrackNumber();
    }
}
