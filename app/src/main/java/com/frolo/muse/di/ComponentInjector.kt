package com.frolo.muse.di

import com.frolo.muse.engine.service.PlayerService
import com.frolo.muse.firebase.SimpleFirebaseMessagingService
import com.frolo.muse.ui.base.BaseActivity
import com.frolo.muse.ui.main.audiofx.preset.SavePresetVMFactory
import com.frolo.muse.ui.main.editor.album.AlbumEditorVMFactory
import com.frolo.muse.ui.main.editor.playlist.PlaylistEditorVMFactory
import com.frolo.muse.ui.main.editor.song.SongEditorVMFactory
import com.frolo.muse.ui.main.library.albums.album.AlbumVMFactory
import com.frolo.muse.ui.main.library.artists.artist.ArtistVMFactory
import com.frolo.muse.ui.main.library.artists.artist.albums.AlbumsOfArtistVMFactory
import com.frolo.muse.ui.main.library.artists.artist.songs.SongsOfArtistVMFactory
import com.frolo.muse.ui.main.library.buckets.files.AudioBucketVMFactory
import com.frolo.muse.ui.main.library.genres.genre.GenreVMFactory
import com.frolo.muse.ui.main.library.playlists.addmedia.AddMediaToPlaylistVMFactory
import com.frolo.muse.ui.main.library.playlists.create.CreatePlaylistVMFactory
import com.frolo.muse.ui.main.library.playlists.playlist.PlaylistVMFactory
import com.frolo.muse.ui.main.library.playlists.playlist.addsong.AddSongToPlaylistVMFactory
import com.frolo.muse.ui.main.player.lyrics.LyricsVMFactory
import com.frolo.muse.ui.main.player.poster.PosterVMFactory
import com.frolo.muse.ui.main.settings.premium.BuyPremiumVMFactory


/**
 * Responsible for dependency injection. This is preferred over [ComponentProvider].
 */
interface ComponentInjector {

    // Services
    fun inject(service: PlayerService)
    fun inject(service: SimpleFirebaseMessagingService)

    // Activities
    fun inject(activity: BaseActivity)

    // ViewModel factories
    fun inject(vmf: AlbumVMFactory)
    fun inject(vmf: ArtistVMFactory)
    fun inject(vmf: SongsOfArtistVMFactory)
    fun inject(vmf: AlbumsOfArtistVMFactory)
    fun inject(vmf: PlaylistVMFactory)
    fun inject(vmf: GenreVMFactory)
    fun inject(vmf: SongEditorVMFactory)
    fun inject(vmf: AlbumEditorVMFactory)
    fun inject(vmf: PlaylistEditorVMFactory)
    fun inject(vmf: PosterVMFactory)
    fun inject(vmf: AddMediaToPlaylistVMFactory)
    fun inject(vmf: AddSongToPlaylistVMFactory)
    fun inject(vmf: SavePresetVMFactory)
    fun inject(vmf: LyricsVMFactory)
    fun inject(vmf: CreatePlaylistVMFactory)
    fun inject(vmf: AudioBucketVMFactory)
    fun inject(vmf: BuyPremiumVMFactory)

}