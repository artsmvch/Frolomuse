package com.frolo.muse.navigator

import com.frolo.muse.model.media.*
import java.io.File


class NavigatorWrapper : Navigator {
    private var origin: Navigator? = null

    fun attachOrigin(origin: Navigator) {
        this.origin = origin
    }

    fun detachOrigin() {
        this.origin = null
    }

    override fun goToStore() {
        origin?.goToStore()
    }

    override fun helpWithTranslations() {
        origin?.helpWithTranslations()
    }

    override fun contactDeveloper() {
        origin?.contactDeveloper()
    }

    override fun shareSongs(songs: List<Song>) {
        origin?.shareSongs(songs)
    }

    override fun openSong(song: Song) {
        origin?.openSong(song)
    }

    override fun openAlbum(album: Album) {
        origin?.openAlbum(album)
    }

    override fun openArtist(artist: Artist) {
        origin?.openArtist(artist)
    }

    override fun openGenre(genre: Genre) {
        origin?.openGenre(genre)
    }

    override fun openPlaylist(playlist: Playlist) {
        origin?.openPlaylist(playlist)
    }

    override fun openMyFile(myFile: MyFile) {
        origin?.openMyFile(myFile)
    }

    override fun viewLyrics(song: Song) {
        origin?.viewLyrics(song)
    }

    override fun viewPoster(song: Song) {
        origin?.viewPoster(song)
    }

    override fun sharePoster(song: Song, file: File) {
        origin?.sharePoster(song, file)
    }

    override fun openRingCutter(song: Song) {
        origin?.openRingCutter(song)
    }

    override fun editSong(song: Song) {
        origin?.editSong(song)
    }

    override fun editAlbum(album: Album) {
        origin?.editAlbum(album)
    }

    override fun editPlaylist(playlist: Playlist) {
        origin?.editPlaylist(playlist)
    }

    override fun addMediaItemsToPlaylist(items: ArrayList<out Media>) {
        origin?.addMediaItemsToPlaylist(items)
    }

    override fun addSongsToPlaylist(playlist: Playlist) {
        origin?.addSongsToPlaylist(playlist)
    }

    override fun createPlaylist() {
        origin?.createPlaylist()
    }

    override fun createPlaylist(songs: ArrayList<Song>) {
        origin?.createPlaylist(songs)
    }

    override fun openCurrentPlaying() {
        origin?.openCurrentPlaying()
    }

    override fun openPlayer() {
        origin?.openPlayer()
    }

    override fun openAudioFx() {
        origin?.openAudioFx()
    }

    override fun openPlaybackParams() {
        origin?.openPlaybackParams()
    }

    override fun savePreset(bandLevels: ShortArray) {
        origin?.savePreset(bandLevels)
    }

    override fun openSettings() {
        origin?.openSettings()
    }

    override fun goBack() {
        origin?.goBack()
    }

}