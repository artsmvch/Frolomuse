package com.frolo.muse.model.media;


public class SongWithPlayCount {
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
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof SongWithPlayCount)) return false;
        SongWithPlayCount another = (SongWithPlayCount) obj;
        return playCount == another.playCount && song.equals(another.song);
    }
}
