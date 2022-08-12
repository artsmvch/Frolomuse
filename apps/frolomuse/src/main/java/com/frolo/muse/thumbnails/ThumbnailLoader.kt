package com.frolo.muse.thumbnails

import android.widget.ImageView
import com.frolo.music.model.*


interface ThumbnailLoader {
    fun loadSongThumbnail(song: Song, target: ImageView)
    fun loadAlbumThumbnail(album: Album, target: ImageView)
    fun loadRawAlbumThumbnail(album: Album, target: ImageView)
    fun loadArtistThumbnail(artist: Artist, target: ImageView)
    fun loadGenreThumbnail(genre: Genre, target: ImageView)
    fun loadPlaylistThumbnail(playlist: Playlist, target: ImageView)
    fun loadMyFileThumbnail(file: MyFile, target: ImageView)
    fun loadMediaFileThumbnail(file: MediaFile, target: ImageView)
}