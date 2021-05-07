package com.frolo.muse.navigator

import com.frolo.muse.billing.ProductId
import com.frolo.muse.model.media.*
import java.io.File


class NavigatorWrapper : Navigator {

    private var delegate: Navigator? = null

    fun attachBase(navigator: Navigator) {
        this.delegate = navigator
    }

    fun detachBase() {
        this.delegate = null
    }

    override fun goToStore() {
        delegate?.goToStore()
    }

    override fun helpWithTranslations() {
        delegate?.helpWithTranslations()
    }

    override fun contactDeveloper() {
        delegate?.contactDeveloper()
    }

    override fun shareSongs(songs: List<Song>) {
        delegate?.shareSongs(songs)
    }

    override fun openSong(song: Song) {
        delegate?.openSong(song)
    }

    override fun openAlbum(album: Album) {
        delegate?.openAlbum(album)
    }

    override fun openArtist(artist: Artist) {
        delegate?.openArtist(artist)
    }

    override fun openGenre(genre: Genre) {
        delegate?.openGenre(genre)
    }

    override fun openPlaylist(playlist: Playlist) {
        delegate?.openPlaylist(playlist)
    }

    override fun openMyFile(myFile: MyFile) {
        delegate?.openMyFile(myFile)
    }

    override fun viewLyrics(song: Song) {
        delegate?.viewLyrics(song)
    }

    override fun viewPoster(song: Song) {
        delegate?.viewPoster(song)
    }

    override fun sharePoster(song: Song, file: File) {
        delegate?.sharePoster(song, file)
    }

    override fun openRingCutter(song: Song) {
        delegate?.openRingCutter(song)
    }

    override fun editSong(song: Song) {
        delegate?.editSong(song)
    }

    override fun editAlbum(album: Album) {
        delegate?.editAlbum(album)
    }

    override fun editPlaylist(playlist: Playlist) {
        delegate?.editPlaylist(playlist)
    }

    override fun addMediaItemsToPlaylist(items: ArrayList<out Media>) {
        delegate?.addMediaItemsToPlaylist(items)
    }

    override fun addSongsToPlaylist(playlist: Playlist) {
        delegate?.addSongsToPlaylist(playlist)
    }

    override fun createPlaylist() {
        delegate?.createPlaylist()
    }

    override fun createPlaylist(songs: ArrayList<Song>) {
        delegate?.createPlaylist(songs)
    }

    override fun openCurrentPlaying() {
        delegate?.openCurrentPlaying()
    }

    override fun openPlayer() {
        delegate?.openPlayer()
    }

    override fun openAudioFx() {
        delegate?.openAudioFx()
    }

    override fun openPlaybackParams() {
        delegate?.openPlaybackParams()
    }

    override fun savePreset(bandLevels: ShortArray) {
        delegate?.savePreset(bandLevels)
    }

    override fun openSettings() {
        delegate?.openSettings()
    }

    override fun openThemeChooser() {
        delegate?.openThemeChooser()
    }

    override fun launchBillingFlow(productId: ProductId) {
        delegate?.launchBillingFlow(productId)
    }

    override fun goBack() {
        delegate?.goBack()
    }

}