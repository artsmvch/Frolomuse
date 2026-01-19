package com.frolo.muse.audius.model

import com.frolo.music.model.Media
import com.frolo.music.model.MediaId
import com.frolo.music.model.Song
import com.frolo.music.model.SongType
import com.frolo.player.AudioSource
import com.frolo.player.AudioMetadata
import com.frolo.player.AudioType
import java.io.Serializable

class AudiusSong(
    private val track: AudiusTrack
) : Song, AudioSource, Serializable {

    private val mediaId = MediaId.createAudius(Media.SONG, track.trackId.toLong())

    override fun getSongType() = SongType.MUSIC

    override fun getSource() = "https://api.audius.co/v1/tracks/${track.id}/stream"

    override fun getMediaId() = mediaId

    override fun getTitle() = track.title

    override fun getArtistId() = 0L // Audius tracks don't have artist IDs

    override fun getArtist() = "Unknown"

    override fun getAlbumId() = 0L // Audius tracks don't have album IDs

    override fun getAlbum() = "Unknown"

    override fun getGenre() = track.genre ?: ""

    override fun getDuration() = track.duration * 1000 // Convert to milliseconds

    override fun getYear() = 0 // Audius doesn't provide year in a simple format

    override fun getTrackNumber() = 0

    // Additional properties for Audius tracks
    fun getAudiusTrack() = track
    fun getArtworkUrl() = track.artwork?.`480x480` ?: track.artwork?.`150x150`
    fun getPermalink() = track.permalink
    fun getPlayCount() = track.playCount
    fun isStreamable() = track.isStreamable

    override fun getURI(): String = mediaId.uri

    // AudioSource implementation
    override fun getMetadata(): AudioMetadata = object : AudioMetadata {
        override fun getAudioType() = AudioType.MUSIC
        override fun getTitle() = track.title
        override fun getArtistId() = 0L // Audius tracks don't have artist IDs
        override fun getArtist() = "Unknown"
        override fun getAlbumId() = 0L // Audius tracks don't have album IDs
        override fun getAlbum() = "Unknown"
        override fun getGenre() = track.genre ?: ""
        override fun getDuration() = track.duration * 1000 // Convert to milliseconds
        override fun getYear() = 0 // Audius doesn't provide year in a simple format
        override fun getTrackNumber() = 0
    }
}
