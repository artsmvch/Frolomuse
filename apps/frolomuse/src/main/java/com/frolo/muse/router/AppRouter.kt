package com.frolo.muse.router

import com.frolo.music.model.*
import java.io.File


/**
 * Router to all screens in the app.
 */
interface AppRouter {
    fun goToStore()

    fun helpWithTranslations()

    fun contactDeveloper()

    fun shareSongs(songs: List<Song>)

    fun openLibrary()

    fun openSearch()

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

    fun openVisualizer()

    fun openPlaybackParams()

    fun savePreset(bandLevels: ShortArray)

    fun openSettings()

    fun openPlaybackFadingParams()

    fun openThemeChooser()

    fun offerToBuyPremium(allowTrialActivation: Boolean)

    fun openDonations()

    fun ignoreBatteryOptimizationSettings()

    fun goBack()

    fun interface Provider {
        fun getRouter(): AppRouter
    }
}