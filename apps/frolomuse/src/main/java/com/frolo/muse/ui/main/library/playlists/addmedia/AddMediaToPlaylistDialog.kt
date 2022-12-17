package com.frolo.muse.ui.main.library.playlists.addmedia

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.muse.R
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.di.activityComponent
import com.frolo.music.model.Media
import com.frolo.music.model.Playlist
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.library.playlists.addmedia.adapter.PlaylistSelectorAdapter
import com.frolo.muse.views.Anim
import kotlinx.android.synthetic.main.dialog_add_media_to_playlist.*


class AddMediaToPlaylistDialog : BaseDialogFragment() {

    private val viewModel: AddMediaToPlaylistViewModel by lazy {
        @Suppress("UNCHECKED_CAST")
        val mediaList = requireArguments()
                .getSerializable(ARG_MEDIA_LIST) as ArrayList<Media>
        val vmFactory = AddMediaToPlaylistVMFactory(activityComponent, activityComponent, mediaList)
        ViewModelProviders.of(this, vmFactory)
                .get(AddMediaToPlaylistViewModel::class.java)
    }

    private val adapter by lazy {
        PlaylistSelectorAdapter().apply {
            onCheckedPlaylistsChangeListener =
                PlaylistSelectorAdapter.OnCheckedPlaylistsChangeListener { checkedPlaylists ->
                    viewModel.onCheckedPlaylistsChanged(checkedPlaylists)
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkReadPermissionFor {
            //viewModel.onOpened()
        }

        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_add_media_to_playlist)
            setupDialogSizeRelativelyToScreen(dialog = this, widthPercent = 19f / 20f)
            loadUi(this)
        }
    }

    private fun loadUi(dialog: Dialog) = with(dialog) {
        rv_list.layoutManager = LinearLayoutManager(context)
        rv_list.adapter = adapter

        include_progress_overlay.setOnTouchListener { _, _ -> true }

        btn_add_to_playlist.setOnClickListener {
            viewModel.onAddButtonClicked()
        }

        btn_cancel.setOnClickListener {
            cancel()
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        isLoading.observeNonNull(owner) { isLoading ->
            onSetLoading(isLoading)
        }

        placeholderVisible.observeNonNull(owner) { visible ->
            onSetPlaceholderVisible(visible)
        }

        isAddButtonEnabled.observeNonNull(owner) { isEnabled ->
            dialog?.apply {
                btn_add_to_playlist.isEnabled = isEnabled
                btn_add_to_playlist.alpha = if (isEnabled) 1f else 0.3f
            }
        }

        playlists.observeNonNull(owner) { list ->
            onSubmitList(list)
        }

        error.observeNonNull(owner) { err ->
            onDisplayError(err)
        }

        isAddingItemsToPlaylist.observeNonNull(owner) { isAdding ->
            onSetAddingItemsToPlaylist(isAdding)
        }

        itemsAddedToPlaylistEvent.observeNonNull(owner) {
            onItemsAddedToPlaylist()
        }
    }

    private fun onSetLoading(isLoading: Boolean) {
        dialog?.apply {
            pb_loading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun onSetPlaceholderVisible(visible: Boolean) {
        dialog?.apply {
            layout_list_placeholder.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    private fun onSetAddingItemsToPlaylist(isAdding: Boolean) {
        dialog?.apply {
            if (isAdding) {
                Anim.fadeIn(include_progress_overlay)
            } else {
                Anim.fadeOut(include_progress_overlay)
            }
        }
    }

    private fun onSubmitList(list: List<Playlist>) {
        adapter.submit(list)
    }

    private fun onDisplayError(err: Throwable) {
        postError(err)
    }

    private fun onItemsAddedToPlaylist() {
        postLongMessage(R.string.added)
        dismiss()
    }

    companion object {
        private const val ARG_MEDIA_LIST = "media_list"

        // Factory
        fun <E : Media> newInstance(items: ArrayList<E>) = AddMediaToPlaylistDialog()
                .withArg(ARG_MEDIA_LIST, items)
    }

}
