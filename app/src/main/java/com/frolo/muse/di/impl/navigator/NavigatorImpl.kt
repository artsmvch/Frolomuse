package com.frolo.muse.di.impl.navigator

import android.content.Context
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.model.media.*
import com.frolo.muse.ui.*
import com.frolo.muse.ui.base.FragmentNavigator
import com.frolo.muse.ui.main.audiofx.params.PlaybackParamsDialog
import com.frolo.muse.ui.main.audiofx.preset.SavePresetDialog
import com.frolo.muse.ui.main.editor.album.AlbumEditorFragment
import com.frolo.muse.ui.main.editor.playlist.PlaylistEditorFragment
import com.frolo.muse.ui.main.editor.song.SongEditorDialog
import com.frolo.muse.ui.main.library.albums.album.AlbumFragment
import com.frolo.muse.ui.main.library.artists.artist.ArtistFragment
import com.frolo.muse.ui.main.library.genres.genre.GenreFragment
import com.frolo.muse.ui.main.library.playlists.addmedia.AddMediaToPlaylistFragment
import com.frolo.muse.ui.main.library.playlists.create.SavePlaylistDialog
import com.frolo.muse.ui.main.library.playlists.playlist.PlaylistFragment
import com.frolo.muse.ui.main.library.playlists.playlist.addsong.AddSongToPlaylistFragment
import com.frolo.muse.ui.main.player.lyrics.LyricsDialogFragment
import com.frolo.muse.ui.main.player.poster.PosterDialog
import java.io.File


class NavigatorImpl<T> constructor(
    private val root: T
) : Navigator where T: Context, T: FragmentNavigator {

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
        // how to view a song???
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
        val fragment = AlbumEditorFragment.newInstance(album)
        root.pushDialog(fragment)
    }

    override fun editPlaylist(playlist: Playlist) {
        val fragment = PlaylistEditorFragment.newInstance(playlist)
        root.pushDialog(fragment)
    }

    override fun addMediaItemsToPlaylist(items: ArrayList<out Media>) {
        val fragment = AddMediaToPlaylistFragment.newInstance(items)
        root.pushDialog(fragment)
    }

    override fun addSongsToPlaylist(playlist: Playlist) {
        val fragment = AddSongToPlaylistFragment.newInstance(playlist)
        root.pushFragment(fragment)
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

    override fun goBack() {
        root.pop()
    }

}