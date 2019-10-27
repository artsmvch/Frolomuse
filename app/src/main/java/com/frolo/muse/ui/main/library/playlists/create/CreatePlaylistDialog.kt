package com.frolo.muse.ui.main.library.playlists.create

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.withNullableArg
import com.frolo.muse.ui.main.library.base.inputname.AbsInputNameDialog


class CreatePlaylistDialog : AbsInputNameDialog() {

    companion object {
        private const val ARG_SONGS = "songs"

        // Factory
        fun newInstance(songs: ArrayList<Song>? = null) = CreatePlaylistDialog()
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

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            error.observe(owner) { err ->
                displayError(err)
            }

            creationError.observe(owner) { err ->
                displayInputError(err)
            }

            isLoading.observe(owner) { isLoading ->
                setIsLoading(isLoading)
            }

            playlistCreatedEvent.observe(owner) { playlist ->
                onPlaylistCreated(playlist)
            }
        }
    }

    private fun onPlaylistCreated(playlist: Playlist) {
        postLongMessage(R.string.saved)
        PlaylistCreateEvent.dispatch(requireContext(), playlist)
        dismiss()
    }

    override fun onGetTitle(): String {
        return getString(R.string.type_name_for_playlist)
    }

    override fun onSaveButtonClick(name: String) {
        checkWritePermissionFor {
            viewModel.onSaveButtonClicked(name)
        }
    }
}
