package com.frolo.music.model;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.IntDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * A value object that uniquely identifies a media item across different data sources.
 * Encapsulates the data source, media kind, and source-specific identifier.
 */
public final class MediaId implements Serializable {

    @IntDef({SOURCE_LOCAL, SOURCE_AUDIUS})
    @Retention(RetentionPolicy.SOURCE)
    @interface Source { }

    /**
     * Local media storage (device storage)
     */
    public static final int SOURCE_LOCAL = 0;

    /**
     * Remote Audius service
     */
    public static final int SOURCE_AUDIUS = 1;

    @IntDef({Media.NONE, Media.SONG, Media.ALBUM, Media.ARTIST, Media.GENRE, Media.PLAYLIST, Media.MY_FILE, Media.MEDIA_FILE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Kind { }

    public static final long NO_ID = -1;

    private final @Source int source;
    private final @Kind int kind;
    private final long sourceId;

    public MediaId(@Source int source, @Kind int kind, long sourceId) {
        if (source < 0) {
            throw new IllegalArgumentException("Source must be non-negative");
        }
        if (kind < 0) {
            throw new IllegalArgumentException("Kind must be non-negative");
        }
        this.source = source;
        this.kind = kind;
        this.sourceId = sourceId;
    }

    /**
     * @return the data source of this media item
     */
    public @Source int getSource() {
        return source;
    }

    /**
     * @return the kind of media
     */
    public @Kind int getKind() {
        return kind;
    }

    /**
     * @return the source-specific identifier
     */
    public long getSourceId() {
        return sourceId;
    }

    /**
     * @return the uniform resource identifier (URI) for this media item
     */
    @NonNull
    public String getURI() {
        switch (source) {
            case SOURCE_LOCAL:
                Uri uri;
                switch (kind) {
                    case Media.SONG:
                    case Media.MEDIA_FILE:
                        uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, sourceId);
                        break;
                    case Media.ALBUM:
                        uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, sourceId);
                        break;
                    case Media.ARTIST:
                        uri = ContentUris.withAppendedId(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, sourceId);
                        break;
                    case Media.GENRE:
                        uri = ContentUris.withAppendedId(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, sourceId);
                        break;
                    case Media.PLAYLIST:
                        uri = ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, sourceId);
                        break;
                    case Media.MY_FILE:
                        // For MY_FILE, we don't have a standard MediaStore URI, return a custom URI
                        return "file://my_file/" + sourceId;
                    case Media.NONE:
                    default:
                        return "unknown://" + source + "/" + kind + "/" + sourceId;
                }
                return uri.toString();
            case SOURCE_AUDIUS:
                return "https://api.audius.co/v1/tracks/" + sourceId + "/stream";
            default:
                return "unknown://" + source + "/" + kind + "/" + sourceId;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaId mediaId = (MediaId) o;
        return source == mediaId.source && 
               kind == mediaId.kind && 
               sourceId == mediaId.sourceId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(source, kind, sourceId);
    }
    
    @NonNull
    @Override
    public String toString() {
        return "MediaId{" +
                "source=" + sourceToString(source) +
                ", kind=" + kindToString(kind) +
                ", sourceId=" + sourceId +
                '}';
    }
    
    private static String sourceToString(int source) {
        switch (source) {
            case SOURCE_LOCAL: return "LOCAL";
            case SOURCE_AUDIUS: return "AUDIUS";
            default: return "UNKNOWN(" + source + ")";
        }
    }

    private static String kindToString(int kind) {
        switch (kind) {
            case Media.NONE: return "NONE";
            case Media.SONG: return "SONG";
            case Media.ALBUM: return "ALBUM";
            case Media.ARTIST: return "ARTIST";
            case Media.GENRE: return "GENRE";
            case Media.PLAYLIST: return "PLAYLIST";
            case Media.MY_FILE: return "MY_FILE";
            case Media.MEDIA_FILE: return "MEDIA_FILE";
            default: return "UNKNOWN(" + kind + ")";
        }
    }

    /**
     * Creates a MediaId for local media
     */
    public static MediaId createLocal(@Kind int kind, long sourceId) {
        return new MediaId(SOURCE_LOCAL, kind, sourceId);
    }

    /**
     * Creates a MediaId for Audius media
     */
    public static MediaId createAudius(@Kind int kind, long sourceId) {
        return new MediaId(SOURCE_AUDIUS, kind, sourceId);
    }
}
