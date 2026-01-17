package com.frolo.player.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.frolo.player.AudioMetadata;
import com.frolo.player.AudioSource;
import com.frolo.player.AudioType;

import java.util.Objects;


public final class AudioSources {

    private static final Uri CONTENT_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    private static final class SimpleAudioMetadata implements AudioMetadata {

        final AudioType audioType;
        final String title;
        final long albumId;
        final String album;
        final long artistId;
        final String artist;
        final String genre;
        final int duration;
        final int year;
        final int trackNumber;

        SimpleAudioMetadata(
            AudioType audioType,
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
            this.audioType = audioType;
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
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (obj != null && obj instanceof SimpleAudioMetadata) {
                SimpleAudioMetadata another = (SimpleAudioMetadata) obj;
                return audioType == another.audioType
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
        public AudioType getAudioType() {
            return audioType;
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

    private static final class SimpleAudioSource implements AudioSource, MediaStoreRow {

        final String uri;
        final AudioMetadata metadata;

        SimpleAudioSource(String uri, AudioMetadata metadata) {
            this.uri = uri;
            this.metadata = metadata;
        }

        @Override
        public String getURI() {
            return uri;
        }

        @NonNull
        @Override
        public AudioMetadata getMetadata() {
            return metadata;
        }

        @NonNull
        @Override
        public Uri getUri() {
            return Uri.parse(uri);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (!(o instanceof SimpleAudioSource)) return false;
            SimpleAudioSource that = (SimpleAudioSource) o;
            return Objects.equals(uri, that.uri) &&
                    Objects.equals(metadata, that.metadata);
        }

    }

    @NonNull
    public static AudioSource createAudioSource(@NonNull String uri, @NonNull AudioMetadata metadata) {
        return new SimpleAudioSource(uri, metadata);
    }

    @NonNull
    public static AudioSource copyAudioSource(@NonNull AudioSource other) {
        return createAudioSource(other.getURI(), copyMetadata(other.getMetadata()));
    }

    @NonNull
    public static AudioMetadata createMetadata(
        AudioType audioType,
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
        return new SimpleAudioMetadata(audioType, title, albumId, album,
                artistId, artist, genre, duration, year, trackNumber);
    }

    @NonNull
    public static AudioMetadata copyMetadata(@NonNull AudioMetadata metadata) {
        return createMetadata(
            metadata.getAudioType(),
            metadata.getTitle(),
            metadata.getAlbumId(),
            metadata.getAlbum(),
            metadata.getArtistId(),
            metadata.getArtist(),
            metadata.getGenre(),
            metadata.getDuration(),
            metadata.getYear(),
            metadata.getTrackNumber()
        );
    }

    public static boolean areSourcesTheSame(@NonNull AudioSource item1, @NonNull AudioSource item2) {
        return Objects.equals(item1.getURI(), item2.getURI());
    }

    private AudioSources() {
    }

}
