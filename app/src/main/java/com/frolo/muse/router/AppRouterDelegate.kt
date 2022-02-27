package com.frolo.muse.router

import com.frolo.music.model.*
import java.io.File


abstract class AppRouterDelegate : AppRouter {

    protected abstract fun delegate(action: (AppRouter) -> Unit)

    override fun goToStore() {
        delegate { it.goToStore() }
    }

    override fun helpWithTranslations() {
        delegate { it.helpWithTranslations() }
    }

    override fun contactDeveloper() {
        delegate { it.contactDeveloper() }
    }

    override fun shareSongs(songs: List<Song>) {
        delegate { it.shareSongs(songs) }
    }

    override fun openSong(song: Song) {
        delegate { it.openSong(song) }
    }

    override fun openAlbum(album: Album) {
        delegate { it.openAlbum(album) }
    }

    override fun openArtist(artist: Artist) {
        delegate { it.openArtist(artist) }
    }

    override fun openGenre(genre: Genre) {
        delegate { it.openGenre(genre) }
    }

    override fun openPlaylist(playlist: Playlist) {
        delegate { it.openPlaylist(playlist) }
    }

    override fun openMyFile(myFile: MyFile) {
        delegate { it.openMyFile(myFile) }
    }

    override fun viewLyrics(song: Song) {
        delegate { it.viewLyrics(song) }
    }

    override fun viewPoster(song: Song) {
        delegate { it.viewPoster(song) }
    }

    override fun sharePoster(song: Song, file: File) {
        delegate { it.sharePoster(song, file) }
    }

    override fun openRingCutter(song: Song) {
        delegate { it.openRingCutter(song) }
    }

    override fun editSong(song: Song) {
        delegate { it.editSong(song) }
    }

    override fun editAlbum(album: Album) {
        delegate { it.editAlbum(album) }
    }

    override fun editPlaylist(playlist: Playlist) {
        delegate { it.editPlaylist(playlist) }
    }

    override fun addMediaItemsToPlaylist(items: ArrayList<out Media>) {
        delegate { it.addMediaItemsToPlaylist(items) }
    }

    override fun addSongsToPlaylist(playlist: Playlist) {
        delegate { it.addSongsToPlaylist(playlist) }
    }

    override fun createPlaylist() {
        delegate { it.createPlaylist() }
    }

    override fun createPlaylist(songs: ArrayList<Song>) {
        delegate { it.createPlaylist(songs) }
    }

    override fun openCurrentPlaying() {
        delegate { it.openCurrentPlaying() }
    }

    override fun openPlayer() {
        delegate { it.openPlayer() }
    }

    override fun openAudioFx() {
        delegate { it.openAudioFx() }
    }

    override fun openPlaybackParams() {
        delegate { it.openPlaybackParams() }
    }

    override fun savePreset(bandLevels: ShortArray) {
        delegate { it.savePreset(bandLevels) }
    }

    override fun openSettings() {
        delegate { it.openSettings() }
    }

    override fun openPlaybackFadingParams() {
        delegate { it.openPlaybackFadingParams() }
    }

    override fun openThemeChooser() {
        delegate { it.openThemeChooser() }
    }

    override fun offerToBuyPremium(allowTrialActivation: Boolean) {
        delegate { it.offerToBuyPremium(allowTrialActivation) }
    }

    override fun openDonations() {
        delegate { it.openDonations() }
    }

    override fun goBack() {
        delegate { it.goBack() }
    }
}