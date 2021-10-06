package com.frolo.muse.navigator

import com.frolo.billing.ProductId
import com.frolo.muse.model.media.*
import java.io.File


class TestNavigator : Navigator {
    override fun goToStore() = Unit
    override fun helpWithTranslations() = Unit
    override fun contactDeveloper() = Unit
    override fun shareSongs(songs: List<Song>) = Unit
    override fun openSong(song: Song) = Unit
    override fun openAlbum(album: Album) = Unit
    override fun openArtist(artist: Artist) = Unit
    override fun openGenre(genre: Genre) = Unit
    override fun openPlaylist(playlist: Playlist) = Unit
    override fun openMyFile(myFile: MyFile) = Unit
    override fun viewLyrics(song: Song) = Unit
    override fun viewPoster(song: Song) = Unit
    override fun sharePoster(song: Song, file: File) = Unit
    override fun openRingCutter(song: Song) = Unit
    override fun editSong(song: Song) = Unit
    override fun editAlbum(album: Album) = Unit
    override fun editPlaylist(playlist: Playlist) = Unit
    override fun addMediaItemsToPlaylist(items: ArrayList<out Media>) = Unit
    override fun addSongsToPlaylist(playlist: Playlist) = Unit
    override fun createPlaylist() = Unit
    override fun createPlaylist(songs: ArrayList<Song>) = Unit
    override fun openCurrentPlaying() = Unit
    override fun openPlayer() = Unit
    override fun openAudioFx() = Unit
    override fun openPlaybackParams() = Unit
    override fun savePreset(bandLevels: ShortArray) = Unit
    override fun openSettings() = Unit
    override fun openPlaybackFadingParams() = Unit
    override fun openThemeChooser() = Unit
    override fun offerToBuyPremium(allowTrialActivation: Boolean) = Unit
    override fun launchBillingFlow(productId: ProductId) = Unit
    override fun goBack() = Unit
}