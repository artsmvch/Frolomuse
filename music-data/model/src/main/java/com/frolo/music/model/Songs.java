package com.frolo.music.model;

import java.io.Serializable;
import java.util.Objects;


public final class Songs {

    private static class SimpleSong implements Song, Serializable {

        final MediaId mediaId;
        final SongType songType;
        final String source;
        final String title;
        final long albumId;
        final String album;
        final long artistId;
        final String artist;
        final String genre;
        final int duration;
        final int year;
        final int trackNumber;

        SimpleSong(
            long id,
            SongType songType,
            String source,
            String title,
            long albumId,
            String album,
            long artistId,
            String artist,
            String genre,
            int duration,
            int year,
            int trackNumber
        ) {
            this.mediaId = MediaId.createLocal(Media.SONG, id);
            this.songType = songType;
            this.source = source;
            this.title = title != null ? title : "";
            this.albumId = albumId;
            this.album = album != null ? album : "";
            this.artistId = artistId;
            this.artist = artist != null ? artist : "";
            this.genre = genre != null ? genre : "";
            this.duration = duration;
            this.year = year;
            this.trackNumber = trackNumber;
        }

        @Override
        public SongType getSongType() {
            return songType;
        }

        public String getSource() {
            return source;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (obj != null && obj instanceof SimpleSong) {
                SimpleSong another = (SimpleSong) obj;
                return mediaId.equals(another.mediaId)
                        && Objects.equals(source, another.source)
                        && songType == another.songType
                        && Objects.equals(title, another.title)
                        && albumId == another.albumId
                        && Objects.equals(album, another.album)
                        && artistId == another.artistId
                        && Objects.equals(artist, another.artist)
                        && Objects.equals(genre, another.genre)
                        && duration == another.duration
                        && year == another.year
                        && trackNumber == another.trackNumber;
            } else return false;
        }

        @Override
        public int hashCode() {
            return mediaId.hashCode();
        }

        @Override
        public String toString() {
            return source;
        }

        @Override
        public MediaId getMediaId() {
            return mediaId;
        }

        public String getTitle() {
            return title;
        }

        public String getArtist() {
            return artist;
        }

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

        public String getGenre() {
            return genre;
        }

        public long getArtistId() {
            return artistId;
        }

        @Override
        public int getTrackNumber() {
            return trackNumber;
        }
    }

    /**
     * Creates an instance of type Song.
     * @param id song ID
     * @param songType song type
     * @param source source
     * @param title title
     * @param albumId album ID
     * @param album album
     * @param artistId artist ID
     * @param artist artist
     * @param genre genre
     * @param duration duration
     * @param year year
     * @param trackNumber track number
     * @return an instance of type Song
     */
    public static Song create(
        long id,
        SongType songType,
        String source,
        String title,
        long albumId,
        String album,
        long artistId,
        String artist,
        String genre,
        int duration,
        int year,
        int trackNumber
    ) {
        return new SimpleSong(id, songType, source, title, albumId, album,
                artistId, artist, genre, duration, year, trackNumber);
    }

    /**
     * Creates an instance of type Song fully copied from the given <code>src</code>.
     * NOTE: if <code>src</code> is null then this also returns null.
     * @param src to copy fields from
     * @return an instance of type Song fully copied from the given src param
     */
    public static Song create(Song src) {
        if (src == null) return null;

        return create(
            src.getMediaId().getSourceId(),
            src.getSongType(),
            src.getSource(),
            src.getTitle(),
            src.getAlbumId(),
            src.getAlbum(),
            src.getArtistId(),
            src.getArtist(),
            src.getGenre(),
            src.getDuration(),
            src.getYear(),
            src.getTrackNumber()
        );
    }

    /**
     * Creates an instance of type Song fully copied from the given <code>src</code>.
     * Actually, this simply delegates the call to {@link Songs#create(Song)}.
     * @param src to copy fields from
     * @return an instance of type Song fully copied from the given src param
     */
    public static Song copy(Song src) {
        return create(src);
    }

    private Songs() {
    }

}
