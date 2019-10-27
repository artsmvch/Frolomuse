package com.frolo.muse.model.media;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;


public class Song implements Media, Serializable {

    private final long id;
    private final String source;
    private final String title;
    private final long albumId;
    private final String album;
    private final long artistId;
    private final String artist;
    private final String genre;
    private final int duration;
    private final int year;

    public Song(
            long id,
            String source,
            String title,
            long albumId,
            String album,
            long artistId,
            String artist,
            String genre,
            int duration,
            int year) {
        this.id = id;
        this.source = source;
        this.title = title != null ? title : "";
        this.albumId = albumId;
        this.album = album != null ? album : "";
        this.artistId = artistId;
        this.artist = artist != null ? artist : "";
        this.genre = genre != null ? genre : "";
        this.duration = duration;
        this.year = year;
    }

    public String getSource() {
        return source;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj != null && obj instanceof Song) {
            Song another = (Song) obj;
            return id == another.id
                    && Objects.equals(source, another.source)
                    && Objects.equals(title, another.title)
                    && albumId == another.albumId
                    && Objects.equals(album, another.album)
                    && artistId == another.artistId
                    && Objects.equals(artist, another.artist)
                    && Objects.equals(genre, another.genre)
                    && duration == another.duration
                    && year == another.year;
        } else return false;
    }

    @Override
    public int hashCode() {
        return (int) getId();
    }

    @Override
    public String toString() {
        return source;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getKind() {
        return SONG;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getArtist() {
        return artist;
    }

    @NonNull
    public String getAlbum() {
        return album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public int getDuration() {
        return duration;
    }

    public int getYear() {
        return year;
    }

    @NonNull
    public String getGenre() {
        return genre;
    }

    public long getArtistId() {
        return artistId;
    }
}
