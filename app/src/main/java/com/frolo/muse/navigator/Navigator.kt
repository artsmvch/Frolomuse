package com.frolo.muse.navigator

import com.frolo.billing.ProductId
import com.frolo.muse.model.media.*
import java.io.File


interface Navigator {
    fun goToStore()

    fun helpWithTranslations()

    fun contactDeveloper()

    fun shareSongs(songs: List<Song>)

    fun openSong(song: Song)

    fun openAlbum(album: Album)

    fun openArtist(artist: Artist)

    fun openGenre(genre: Genre)

    fun openPlaylist(playlist: Playlist)

    fun openMyFile(myFile: MyFile)

    fun viewLyrics(song: Song)

    fun viewPoster(song: Song)

    fun sharePoster(song: Song, file: File)

    fun openRingCutter(song: Song)

    fun editSong(song: Song)

    fun editAlbum(album: Album)

    fun editPlaylist(playlist: Playlist)

    fun addMediaItemsToPlaylist(items: ArrayList<out Media>)
    
    fun addSongsToPlaylist(playlist: Playlist)

    fun createPlaylist()

    fun createPlaylist(songs: ArrayList<Song>)
    
    fun openCurrentPlaying()

    fun openPlayer()

    fun openAudioFx()

    fun openPlaybackParams()

    fun savePreset(bandLevels: ShortArray)

    fun openSettings()

    fun openPlaybackFadingParams()

    fun openThemeChooser()

    fun offerToBuyPremium(allowTrialActivation: Boolean)

    fun launchBillingFlow(productId: ProductId)

    fun openDonations()

    fun goBack()
}