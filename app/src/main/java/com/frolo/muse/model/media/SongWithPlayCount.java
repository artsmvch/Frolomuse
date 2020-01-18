package com.frolo.muse.model.media;


import java.io.Serializable;


public class SongWithPlayCount implements Song, Serializable {
    private final Song song;
    private final int playCount;

    public SongWithPlayCount(Song song, int playCount) {
        this.song = song;
        this.playCount = playCount;
    }

    public Song getSong() {
        return song;
    }

    public int getPlayCount() {
        return playCount;
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
        return playCount == another.playCount && song.equals(another.song);
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
}
