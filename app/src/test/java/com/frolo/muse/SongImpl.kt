package com.frolo.muse

import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Song


internal class SongImpl constructor(
    private val id: Long,
    private val source: String,
    private val title: String,
    private val albumId: Long,
    private val album: String,
    private val artistId: Long,
    private val artist: String,
    private val genre: String,
    private val duration: Int,
    private val year: Int,
    private val trackNumber: Int
): Song {
    override fun getArtistId() = artistId

    override fun getKind() = Media.SONG

    override fun getDuration() = duration

    override fun getArtist() = artist

    override fun getSource() = source

    override fun getId() = id

    override fun getAlbumId() = albumId

    override fun getGenre() = genre

    override fun getTitle() = title

    override fun getAlbum() = album

    override fun getYear() = year

    override fun getTrackNumber(): Int = trackNumber

}
