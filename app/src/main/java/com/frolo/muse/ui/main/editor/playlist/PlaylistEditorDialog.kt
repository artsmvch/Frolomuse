package com.frolo.muse.ui.main.editor.playlist

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.ui.base.serializableArg
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.library.base.inputname.AbsInputNameDialog


class PlaylistEditorDialog : AbsInputNameDialog() {

    private val viewModel: PlaylistEditorViewModel by lazy {
        val vmFactory = PlaylistEditorVMFactory(requireFrolomuseApp().appComponent, playlist)
        ViewModelProviders.of(this, vmFactory)
                .get(PlaylistEditorViewModel::class.java)
    }

    private val playlist: Playlist by serializableArg(ARG_PLAYLIST)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        isLoadingUpdate.observeNonNull(owner) { isLoading ->
            setIsLoading(isLoading)
        }

        updatedPlaylist.observeNonNull(owner) { playlist ->
            onPlaylistUpdated(playlist)
        }

        inputError.observeNonNull(owner) { err ->
            displayInputError(err)
        }

        error.observeNonNull(owner) { err ->
            displayError(err)
        }
    }

    override fun onGetTitle() = getString(R.string.edit_playlist)

    override fun onGetHint() = getString(R.string.playlist_name)

    override fun onGetInitialText() = playlist.name

    override fun onSaveButtonClick(name: String) {
        checkWritePermissionFor {
            viewModel.onSaveClicked(name)
        }
    }

    private fun onPlaylistUpdated(newPlaylist: Playlist) {
        dismiss()
    }

    companion object {
        private const val ARG_PLAYLIST = "playlist"

        // Factory
        fun newInstance(playlist: Playlist) = PlaylistEditorDialog()
                .withArg(ARG_PLAYLIST, playlist)
    }

}
