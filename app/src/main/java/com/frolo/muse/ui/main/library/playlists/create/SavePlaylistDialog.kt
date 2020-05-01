package com.frolo.muse.ui.main.library.playlists.create

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.withNullableArg
import com.frolo.muse.ui.main.library.base.inputname.AbsInputNameDialog


class SavePlaylistDialog : AbsInputNameDialog() {

    companion object {
        private const val ARG_SONGS = "songs"

        // Factory
        fun newInstance(songs: ArrayList<Song>? = null) = SavePlaylistDialog()
                .withNullableArg(ARG_SONGS, songs)
    }

    private val viewModel: CreatePlaylistViewModel by lazy {
        @Suppress("UNCHECKED_CAST")
        val songsToAdd = requireArguments().getSerializable(ARG_SONGS) as? List<Song>
        val vmFactory = CreatePlaylistVMFactory(requireApp().appComponent, songsToAdd)
        ViewModelProviders.of(this, vmFactory)
                .get(CreatePlaylistViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        error.observeNonNull(owner) { err ->
            displayError(err)
        }

        creationError.observeNonNull(owner) { err ->
            displayInputError(err)
        }

        isLoading.observeNonNull(owner) { isLoading ->
            setIsLoading(isLoading)
        }

        playlistCreatedEvent.observeNonNull(owner) { playlist ->
            onPlaylistCreated(playlist)
        }
    }

    private fun onPlaylistCreated(playlist: Playlist) {
        postLongMessage(R.string.saved)
        PlaylistCreateEvent.dispatch(requireContext(), playlist)
        dismiss()
    }

    override fun onGetTitle() = getString(R.string.save_playlist)

    override fun onGetHint() = getString(R.string.playlist_name)

    override fun onSaveButtonClick(name: String) {
        checkWritePermissionFor {
            viewModel.onSaveButtonClicked(name)
        }
    }
}
