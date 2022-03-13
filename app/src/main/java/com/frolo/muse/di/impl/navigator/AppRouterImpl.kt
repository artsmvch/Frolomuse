package com.frolo.muse.di.impl.navigator

import com.frolo.muse.router.AppRouter
import com.frolo.muse.ui.*
import com.frolo.muse.ui.main.MainActivity
import com.frolo.muse.ui.main.audiofx.params.PlaybackParamsDialog
import com.frolo.muse.ui.main.audiofx.preset.SavePresetDialog
import com.frolo.muse.ui.main.editor.album.AlbumEditorDialog
import com.frolo.muse.ui.main.editor.playlist.PlaylistEditorDialog
import com.frolo.muse.ui.main.editor.song.SongEditorDialog
import com.frolo.muse.ui.main.library.albums.album.AlbumFragment
import com.frolo.muse.ui.main.library.artists.artist.ArtistFragment
import com.frolo.muse.ui.main.library.genres.genre.GenreFragment
import com.frolo.muse.ui.main.library.playlists.addmedia.AddMediaToPlaylistDialog
import com.frolo.muse.ui.main.library.playlists.create.SavePlaylistDialog
import com.frolo.muse.ui.main.library.playlists.playlist.PlaylistFragment
import com.frolo.muse.ui.main.library.playlists.playlist.addsong.AddSongToPlaylistDialog
import com.frolo.muse.ui.main.player.lyrics.LyricsDialogFragment
import com.frolo.muse.ui.main.player.poster.PosterDialog
import com.frolo.muse.ui.main.settings.donations.DonationsFragment
import com.frolo.muse.ui.main.settings.playback.PlaybackFadingDialog
import com.frolo.muse.ui.main.settings.premium.BuyPremiumDialog
import com.frolo.muse.ui.main.settings.theme.ThemeChooserFragment
import com.frolo.music.model.*
import com.frolo.threads.ThreadStrictMode
import java.io.File


class AppRouterImpl constructor(
    private val root: MainActivity
) : AppRouter {

    private fun checkThread() {
        ThreadStrictMode.assertMain()
    }

    override fun goToStore() {
        checkThread()
        root.goToStore()
    }

    override fun helpWithTranslations() {
        checkThread()
        root.helpWithTranslations()
    }

    override fun contactDeveloper() {
        checkThread()
        root.contactDeveloper()
    }

    override fun shareSongs(songs: List<Song>) {
        checkThread()
        root.share(songs)
    }

    override fun openSong(song: Song) {
        checkThread()
        // How are we supposed to open this?
    }

    override fun openAlbum(album: Album) {
        checkThread()
        val fragment = AlbumFragment.newInstance(album)
        root.pushFragment(fragment)
    }

    override fun openArtist(artist: Artist) {
        checkThread()
        val fragment = ArtistFragment.newInstance(artist)
        root.pushFragment(fragment)
    }

    override fun openGenre(genre: Genre) {
        checkThread()
        val fragment = GenreFragment.newInstance(genre)
        root.pushFragment(fragment)
    }

    override fun openPlaylist(playlist: Playlist) {
        checkThread()
        val fragment = PlaylistFragment.newInstance(playlist)
        root.pushFragment(fragment)
    }

    override fun openMyFile(myFile: MyFile) {
        checkThread()
    }

    override fun viewLyrics(song: Song) {
        checkThread()
        val fragment = LyricsDialogFragment.newInstance(song)
        root.pushDialog(fragment)
    }

    override fun viewPoster(song: Song) {
        checkThread()
        val fragment = PosterDialog.newInstance(song)
        root.pushDialog(fragment)
    }

    override fun sharePoster(song: Song, file: File) {
        checkThread()
        root.sharePoster(song, file)
    }

    override fun openRingCutter(song: Song) {
        checkThread()
        val source = song.source
        if (source == null) {
            // It's an error
        } else {
            root.openRingCutter(source)
        }
    }

    override fun editSong(song: Song) {
        checkThread()
        val fragment = SongEditorDialog.newInstance(song)
        root.pushDialog(fragment)
    }

    override fun editAlbum(album: Album) {
        checkThread()
        val fragment = AlbumEditorDialog.newInstance(album)
        root.pushDialog(fragment)
    }

    override fun editPlaylist(playlist: Playlist) {
        checkThread()
        val fragment = PlaylistEditorDialog.newInstance(playlist)
        root.pushDialog(fragment)
    }

    override fun addMediaItemsToPlaylist(items: ArrayList<out Media>) {
        checkThread()
        val fragment = AddMediaToPlaylistDialog.newInstance(items)
        root.pushDialog(fragment)
    }

    override fun addSongsToPlaylist(playlist: Playlist) {
        checkThread()
        val dialog = AddSongToPlaylistDialog.newInstance(playlist)
        root.pushDialog(dialog)
    }

    override fun createPlaylist() {
        checkThread()
        val fragment = SavePlaylistDialog.newInstance()
        root.pushDialog(fragment)
    }

    override fun createPlaylist(songs: ArrayList<Song>) {
        checkThread()
        val fragment = SavePlaylistDialog.newInstance(songs)
        root.pushDialog(fragment)
    }

    override fun openCurrentPlaying() {
        checkThread()
//        val fragment = CurrentSongQueueFragment.newInstance()
//        root.pushFragment(fragment)
    }

    override fun openPlayer() {
        checkThread()
        root.expandSlidingPlayer()
    }

    override fun openAudioFx() {
        checkThread()
    }

    override fun openPlaybackParams() {
        checkThread()
        val fragment = PlaybackParamsDialog.newInstance()
        root.pushDialog(fragment)
    }

    override fun savePreset(bandLevels: ShortArray) {
        checkThread()
        val fragment = SavePresetDialog.newInstance(bandLevels)
        root.pushDialog(fragment)
    }

    override fun openSettings() {
        checkThread()
    }

    override fun openPlaybackFadingParams() {
        checkThread()
        val dialog = PlaybackFadingDialog.newInstance()
        root.pushDialog(dialog)
    }

    override fun openThemeChooser() {
        checkThread()
        val fragment = ThemeChooserFragment.newInstance()
        root.pushFragment(fragment)
    }

    override fun offerToBuyPremium(allowTrialActivation: Boolean) {
        checkThread()
        val dialog = BuyPremiumDialog.newInstance(allowTrialActivation)
        root.pushDialog(dialog)
    }

    override fun openDonations() {
        checkThread()
        val fragment = DonationsFragment.newInstance()
        root.pushFragment(fragment)
    }

    override fun goBack() {
        checkThread()
        root.pop()
    }

}