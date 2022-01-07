package com.frolo.muse.thumbnails

import android.widget.ImageView
import com.frolo.music.model.*


object StubThumbnailLoader : ThumbnailLoader {
    override fun loadSongThumbnail(song: Song, target: ImageView) {
    }

    override fun loadAlbumThumbnail(album: Album, target: ImageView) {
    }

    override fun loadRawAlbumThumbnail(album: Album, target: ImageView) {
    }

    override fun loadArtistThumbnail(artist: Artist, target: ImageView) {
    }

    override fun loadGenreThumbnail(genre: Genre, target: ImageView) {
    }

    override fun loadPlaylistThumbnail(playlist: Playlist, target: ImageView) {
    }

    override fun loadMyFileThumbnail(file: MyFile, target: ImageView) {
    }

    override fun loadMediaFileThumbnail(file: MediaFile, target: ImageView) {
    }
}
