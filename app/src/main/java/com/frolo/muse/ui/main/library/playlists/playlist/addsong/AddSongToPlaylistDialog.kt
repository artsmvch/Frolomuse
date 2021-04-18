package com.frolo.muse.ui.main.library.playlists.playlist.addsong

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.views.Anim
import kotlinx.android.synthetic.main.dialog_add_song_to_playlist.*


class AddSongToPlaylistDialog: BaseDialogFragment() {

    private val viewModel: AddSongToPlaylistViewModel by lazy {
        val playlist = requireArguments().getSerializable(ARG_PLAYLIST) as Playlist
        val vmFactory = AddSongToPlaylistVMFactory(requireFrolomuseApp().appComponent, playlist)
        ViewModelProviders.of(this, vmFactory)
                .get(AddSongToPlaylistViewModel::class.java)
    }

    private val adapter by lazy {
        SongSelectorAdapter(Glide.with(this)).apply {
            setHasStableIds(true)
            listener = object : BaseAdapter.Listener<Song> {
                override fun onItemClick(item: Song, position: Int) {
                    viewModel.onItemClicked(item)
                }

                override fun onItemLongClick(item: Song, position: Int) {
                    viewModel.onItemLongClicked(item)
                }

                override fun onOptionsMenuClick(item: Song, position: Int) = Unit
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setContentView(R.layout.dialog_add_song_to_playlist)
            setupDialogSizeRelativelyToScreen(dialog = this, widthPercent = 19f / 20f)
            loadUI(this)
        }
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        rv_list.apply {
            adapter = this@AddSongToPlaylistDialog.adapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            addLinearItemMargins()
        }

        btn_add_to_playlist.setOnClickListener {
            checkWritePermissionFor {
                viewModel.onAddButtonClicked()
            }
        }

        sv_query.apply {
            setIconifiedByDefault(false)
            isIconified = false
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    checkReadPermissionFor {
                        viewModel.onQuerySubmitted(query)
                    }
                    return true
                }
                override fun onQueryTextChange(query: String): Boolean {
                    checkReadPermissionFor {
                        viewModel.onQueryTyped(query)
                    }
                    return true
                }
            })
            setQuery("", true)
            checkReadWritePermissionsFor {
                // call it anyway, cause setQuery(query, true) not working if query is null or EMPTY
                viewModel.onQueryTyped("")
            }

            // This prevents keyboard from opening on the start
            clearFocus()
        }

        btn_cancel.setOnClickListener {
            cancel()
        }

        include_progress_overlay.setOnTouchListener { _, _ -> true }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        error.observeNonNull(owner) { err ->
            postError(err)
        }

        targetPlaylist.observe(owner) { playlist ->
            dialog?.apply {
                tv_hint.text = getString(R.string.add_song_to_s_playlist_hint, playlist?.name.orEmpty())
            }
        }

        selectableSongQuery.observeNonNull(owner) { songQuery ->
            adapter.submit(songQuery.allItems, songQuery.selection)
        }

        selectedItems.observeNonNull(owner) { selectedItems ->
            adapter.submitSelection(selectedItems)
            dialog?.apply {
                tv_selection_info.text =
                        resources.getQuantityString(R.plurals.s_songs_selected, selectedItems.count(), selectedItems.count())
            }
        }

        placeholderVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                layout_list_placeholder.visibility = if (isVisible) View.VISIBLE else View.GONE
            }
        }

        addToPlaylistButtonEnabled.observeNonNull(owner) { enabled ->
            dialog?.apply {
                btn_add_to_playlist.isEnabled = enabled
                btn_add_to_playlist.alpha = if (enabled) 1f else 0.3f
            }
        }

        songsAddedToPlaylistEvent.observeNonNull(owner) {
        }

        isAddingSongsToPlaylist.observeNonNull(owner) { isAdding ->
            dialog?.apply {
                if (isAdding) {
                    Anim.fadeIn(include_progress_overlay)
                } else {
                    Anim.fadeOut(include_progress_overlay)
                }
            }
        }
    }

    companion object {
        private const val ARG_PLAYLIST = "playlist"

        // Factory
        fun newInstance(playlist: Playlist) = AddSongToPlaylistDialog()
                .withArg(ARG_PLAYLIST, playlist)
    }

}