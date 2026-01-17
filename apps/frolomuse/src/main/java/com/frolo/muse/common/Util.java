package com.frolo.muse.common;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

import com.frolo.music.model.MediaId;
import com.frolo.player.AudioMetadata;
import com.frolo.player.AudioSource;
import com.frolo.player.AudioType;
import com.frolo.player.data.MediaStoreRow;
import com.frolo.music.model.Media;
import com.frolo.music.model.Song;
import com.frolo.music.model.SongType;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public final class Util {

    private static final Uri CONTENT_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    @NotNull
    static SongType toSongType(@NotNull AudioType type) {
        switch (type) {
            case MUSIC:         return SongType.MUSIC;
            case PODCAST:       return SongType.PODCAST;
            case RINGTONE:      return SongType.RINGTONE;
            case ALARM:         return SongType.ALARM;
            case NOTIFICATION:  return SongType.NOTIFICATION;
            case AUDIOBOOK:     return SongType.AUDIOBOOK;
            default:            return SongType.MUSIC;
        }
    }

    @NotNull
    static AudioType toAudioType(@NotNull SongType type) {
        switch (type) {
            case MUSIC:         return AudioType.MUSIC;
            case PODCAST:       return AudioType.PODCAST;
            case RINGTONE:      return AudioType.RINGTONE;
            case ALARM:         return AudioType.ALARM;
            case NOTIFICATION:  return AudioType.NOTIFICATION;
            case AUDIOBOOK:     return AudioType.AUDIOBOOK;
            default:            return AudioType.MUSIC;
        }
    }

    private static final class AudioSourceFromSongDelegate
            implements Song, Media, AudioSource, AudioMetadata, MediaStoreRow {

        private final Song mDelegate;

        AudioSourceFromSongDelegate(Song delegate) {
            mDelegate = delegate;
        }

        @Override
        public String getURI() {
            return mDelegate.getMediaId().getURI();
        }

        @NotNull
        @Override
        public AudioMetadata getMetadata() {
            return this;
        }

        @Override
        public AudioType getAudioType() {
            return toAudioType(mDelegate.getSongType());
        }

        @Override
        public SongType getSongType() {
            return mDelegate.getSongType();
        }

        @Override
        public String getSource() {
            return mDelegate.getSource();
        }

        @Override
        public String getTitle() {
            return mDelegate.getTitle();
        }

        @Override
        public long getArtistId() {
            return mDelegate.getArtistId();
        }

        @Override
        public String getArtist() {
            return mDelegate.getArtist();
        }

        @Override
        public long getAlbumId() {
            return mDelegate.getAlbumId();
        }

        @Override
        public String getAlbum() {
            return mDelegate.getAlbum();
        }

        @Override
        public String getGenre() {
            return mDelegate.getGenre();
        }

        @Override
        public int getDuration() {
            return mDelegate.getDuration();
        }

        @Override
        public int getYear() {
            return mDelegate.getYear();
        }

        @Override
        public int getTrackNumber() {
            return mDelegate.getTrackNumber();
        }

        @Override
        public MediaId getMediaId() {
            return mDelegate.getMediaId();
        }

        @NotNull
        @Override
        public Uri getUri() {
            try {
                // Primary approach: parse the URI from MediaId's string representation
                // This handles custom URI formats like those for MY_FILE or other non-standard sources
                return Uri.parse(mDelegate.getMediaId().getURI());
            } catch (Exception ignored) {
                // Fallback: construct standard MediaStore URI for local audio files
                // This ensures we always return a valid URI for standard local audio content
                return ContentUris.withAppendedId(CONTENT_URI, mDelegate.getMediaId().getSourceId());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (!(o instanceof AudioSourceFromSongDelegate)) return false;
            final AudioSourceFromSongDelegate other = (AudioSourceFromSongDelegate) o;
            return Objects.equals(mDelegate, other.mDelegate);
        }

    }

    @NotNull
    public static AudioSource createAudioSource(@NotNull Song song) {
        return new AudioSourceFromSongDelegate(song);
    }

    private Util() {
    }

}
