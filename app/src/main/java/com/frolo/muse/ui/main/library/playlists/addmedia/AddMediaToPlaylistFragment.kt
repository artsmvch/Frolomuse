package com.frolo.muse.ui.main.library.playlists.addmedia

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.playlists.addmedia.adapter.SimplePlaylistAdapter
import com.frolo.muse.ui.main.library.playlists.create.PlaylistCreateEvent
import com.frolo.muse.views.Anim
import kotlinx.android.synthetic.main.dialog_add_media_to_playlist.*


class AddMediaToPlaylistFragment : BaseDialogFragment() {

    companion object {
        private const val ARG_MEDIA_LIST = "media_list"

        // Factory
        fun <E : Media> newInstance(items: ArrayList<E>) = AddMediaToPlaylistFragment()
                .withArg(ARG_MEDIA_LIST, items)
    }

    private val viewModel: AddMediaToPlaylistViewModel by lazy {
        @Suppress("UNCHECKED_CAST")
        val mediaList = requireArguments()
                .getSerializable(ARG_MEDIA_LIST) as ArrayList<Media>
        val vmFactory = AddMediaTiPlaylistVMFactory(requireApp().appComponent, mediaList)
        ViewModelProviders.of(this, vmFactory)
                .get(AddMediaToPlaylistViewModel::class.java)
    }

    private val adapter by lazy {
        SimplePlaylistAdapter().apply {
            listener = object : BaseAdapter.Listener<Playlist> {
                override fun onItemClick(item: Playlist, position: Int) {
                    checkReadWritePermissionsFor {
                        viewModel.onPlaylistSelected(item)
                    }
                }
                override fun onItemLongClick(item: Playlist, position: Int) = Unit
                override fun onOptionsMenuClick(item: Playlist, position: Int) = Unit
            }
        }
    }

    private lateinit var playlistCreateEvent: PlaylistCreateEvent

    override fun onAttach(context: Context) {
        super.onAttach(context)
        playlistCreateEvent = PlaylistCreateEvent.register(context) { playlist ->
            adapter.add(playlist)
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

            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            setupDialogSize(this, 6 * width / 7, 5 * height / 7)

            initUI(this)
        }
    }

    override fun onDetach() {
        playlistCreateEvent.unregister(requireContext())
        super.onDetach()
    }

    private fun initUI(dialog: Dialog) {
        dialog.apply {
            rv_list.layoutManager = LinearLayoutManager(context)
            rv_list.adapter = adapter

            inc_progress_overlay.setOnClickListener { }

            imv_close.setOnClickListener { dismiss() }
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            isLoading.observe(owner) { isLoading ->
                onSetLoading(isLoading)
            }

            placeholderVisible.observe(owner) { visible ->
                onSetPlaceholderVisible(visible)
            }

            playlists.observe(owner) { list ->
                onSubmitList(list)
            }

            error.observe(owner) { err ->
                onDisplayError(err)
            }

            isAddingItemsToPlaylist.observe(owner) { isAdding ->
                onSetAddingItemsToPlaylist(isAdding)
            }

            itemsAddedToPlaylistEvent.observe(owner) {
                onItemsAddedToPlaylist()
            }
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
                Anim.fadeIn(inc_progress_overlay)
            } else {
                Anim.fadeOut(inc_progress_overlay)
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
}
