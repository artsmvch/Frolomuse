package com.frolo.muse.di.impl.navigator

import com.frolo.muse.FrolomuseApp
import com.frolo.billing.ProductId
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.model.media.*
import com.frolo.muse.rx.subscribeSafely
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
import java.io.File


class NavigatorImpl(private val root: MainActivity) : Navigator {

    override fun goToStore() {
        root.goToStore()
    }

    override fun helpWithTranslations() {
        root.helpWithTranslations()
    }

    override fun contactDeveloper() {
        root.contactDeveloper()
    }

    override fun shareSongs(songs: List<Song>) {
        root.share(songs)
    }

    override fun openSong(song: Song) {
        // How are we supposed to open this?
    }

    override fun openAlbum(album: Album) {
        val fragment = AlbumFragment.newInstance(album)
        root.pushFragment(fragment)
    }

    override fun openArtist(artist: Artist) {
        val fragment = ArtistFragment.newInstance(artist)
        root.pushFragment(fragment)
    }

    override fun openGenre(genre: Genre) {
        val fragment = GenreFragment.newInstance(genre)
        root.pushFragment(fragment)
    }

    override fun openPlaylist(playlist: Playlist) {
        val fragment = PlaylistFragment.newInstance(playlist)
        root.pushFragment(fragment)
    }

    override fun openMyFile(myFile: MyFile) {
    }

    override fun viewLyrics(song: Song) {
        val fragment = LyricsDialogFragment.newInstance(song)
        root.pushDialog(fragment)
    }

    override fun viewPoster(song: Song) {
        val fragment = PosterDialog.newInstance(song)
        root.pushDialog(fragment)
    }

    override fun sharePoster(song: Song, file: File) {
        root.sharePoster(song, file)
    }

    override fun openRingCutter(song: Song) {
        val source = song.source
        if (source == null) {
            // It's an error
        } else {
            root.openRingCutter(source)
        }
    }

    override fun editSong(song: Song) {
        val fragment = SongEditorDialog.newInstance(song)
        root.pushDialog(fragment)
    }

    override fun editAlbum(album: Album) {
        val fragment = AlbumEditorDialog.newInstance(album)
        root.pushDialog(fragment)
    }

    override fun editPlaylist(playlist: Playlist) {
        val fragment = PlaylistEditorDialog.newInstance(playlist)
        root.pushDialog(fragment)
    }

    override fun addMediaItemsToPlaylist(items: ArrayList<out Media>) {
        val fragment = AddMediaToPlaylistDialog.newInstance(items)
        root.pushDialog(fragment)
    }

    override fun addSongsToPlaylist(playlist: Playlist) {
        val dialog = AddSongToPlaylistDialog.newInstance(playlist)
        root.pushDialog(dialog)
    }

    override fun createPlaylist() {
        val fragment = SavePlaylistDialog.newInstance()
        root.pushDialog(fragment)
    }

    override fun createPlaylist(songs: ArrayList<Song>) {
        val fragment = SavePlaylistDialog.newInstance(songs)
        root.pushDialog(fragment)
    }

    override fun openCurrentPlaying() {
//        val fragment = CurrentSongQueueFragment.newInstance()
//        root.pushFragment(fragment)
    }

    override fun openPlayer() {
        root.expandSlidingPlayer()
    }

    override fun openAudioFx() {
    }

    override fun openPlaybackParams() {
        val fragment = PlaybackParamsDialog.newInstance()
        root.pushDialog(fragment)
    }

    override fun savePreset(bandLevels: ShortArray) {
        val fragment = SavePresetDialog.newInstance(bandLevels)
        root.pushDialog(fragment)
    }

    override fun openSettings() {
    }

    override fun openPlaybackFadingParams() {
        val dialog = PlaybackFadingDialog.newInstance()
        root.pushDialog(dialog)
    }

    override fun openThemeChooser() {
        val fragment = ThemeChooserFragment.newInstance()
        root.pushFragment(fragment)
    }

    override fun offerToBuyPremium(allowTrialActivation: Boolean) {
        val dialog = BuyPremiumDialog.newInstance(allowTrialActivation)
        root.pushDialog(dialog)
    }

    override fun launchBillingFlow(productId: ProductId) {
        FrolomuseApp.from(root)
            .appComponent
            .provideBillingManager()
            .launchBillingFlow(productId)
            .subscribeSafely()
    }

    override fun openDonations() {
        val fragment = DonationsFragment.newInstance()
        root.pushFragment(fragment)
    }

    override fun goBack() {
        root.pop()
    }

}