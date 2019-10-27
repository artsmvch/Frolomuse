package com.frolo.muse.ui.main.editor.playlist

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.ui.base.serializableArg
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.library.base.inputname.AbsInputNameDialog


class PlaylistEditorFragment : AbsInputNameDialog() {
    companion object {
        private const val ARG_PLAYLIST = "playlist"

        // Factory
        fun newInstance(playlist: Playlist) = PlaylistEditorFragment()
                .withArg(ARG_PLAYLIST, playlist)
    }

    private val viewModel: PlaylistEditorViewModel by lazy {
        val vmFactory = PlaylistEditorVMFactory(requireApp().appComponent, playlist)
        ViewModelProviders.of(this, vmFactory)
                .get(PlaylistEditorViewModel::class.java)
    }

    private val playlist: Playlist by serializableArg(ARG_PLAYLIST)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            isLoadingUpdate.observe(owner) { isLoading ->
                setIsLoading(isLoading)
            }

            updatedPlaylist.observe(owner) { playlist ->
                onPlaylistUpdated(playlist)
            }

            inputError.observe(owner) { err ->
                displayInputError(err)
            }

            error.observe(owner) { err ->
                displayError(err)
            }
        }
    }

    override fun onGetTitle(): String {
        return getString(R.string.edit_name_of_playlist)
    }

    override fun onGetInitialText(): String? {
        return playlist.name
    }

    override fun onSaveButtonClick(name: String) {
        checkWritePermissionFor {
            viewModel.onSaveClicked(name)
        }
    }

    private fun onPlaylistUpdated(newPlaylist: Playlist) {
        dismiss()
    }
}
