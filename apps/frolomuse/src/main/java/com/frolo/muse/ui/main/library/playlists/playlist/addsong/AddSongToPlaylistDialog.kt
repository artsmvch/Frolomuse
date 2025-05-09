package com.frolo.muse.ui.main.library.playlists.playlist.addsong

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.R
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.databinding.DialogAddSongToPlaylistBinding
import com.frolo.muse.di.activityComponent
import com.frolo.music.model.Playlist
import com.frolo.music.model.Song
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.views.Anim


class AddSongToPlaylistDialog: BaseDialogFragment() {
    private var _binding: DialogAddSongToPlaylistBinding? = null
    private val binding: DialogAddSongToPlaylistBinding get() = _binding!!

    private val viewModel: AddSongToPlaylistViewModel by lazy {
        val playlist = requireArguments().getSerializable(ARG_PLAYLIST) as Playlist
        val vmFactory = AddSongToPlaylistVMFactory(activityComponent, activityComponent, playlist)
        ViewModelProviders.of(this, vmFactory)
                .get(AddSongToPlaylistViewModel::class.java)
    }

    private val adapter by lazy {
        SongSelectorAdapter(provideThumbnailLoader()).apply {
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
            _binding = DialogAddSongToPlaylistBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setupDialogSizeRelativelyToScreen(dialog = this, widthPercent = 19f / 20f)
            loadUI(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUI(dialog: Dialog) = with(binding) {
        rvList.apply {
            adapter = this@AddSongToPlaylistDialog.adapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            addLinearItemMargins()
        }

        btnAddToPlaylist.setOnClickListener {
            viewModel.onAddButtonClicked()
        }

        svQuery.apply {
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

        btnCancel.setOnClickListener {
            dialog.cancel()
        }

        includeProgressOverlay.root.setOnTouchListener { _, _ -> true }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        error.observeNonNull(owner) { err ->
            postError(err)
        }

        targetPlaylist.observe(owner) { playlist ->
            dialog?.apply {
                binding.tvHint.text = getString(R.string.add_song_to_s_playlist_hint, playlist?.name.orEmpty())
            }
        }

        selectableSongQuery.observeNonNull(owner) { songQuery ->
            adapter.submit(songQuery.allItems, songQuery.selection)
        }

        selectedItems.observeNonNull(owner) { selectedItems ->
            adapter.submitSelection(selectedItems)
            dialog?.apply {
                binding.tvSelectionInfo.text =
                        resources.getQuantityString(R.plurals.s_songs_selected, selectedItems.count(), selectedItems.count())
            }
        }

        placeholderVisible.observeNonNull(owner) { isVisible ->
            dialog?.apply {
                binding.layoutListPlaceholder.root.visibility =
                    if (isVisible) View.VISIBLE else View.GONE
            }
        }

        addToPlaylistButtonEnabled.observeNonNull(owner) { enabled ->
            dialog?.apply {
                binding.btnAddToPlaylist.isEnabled = enabled
                binding.btnAddToPlaylist.alpha = if (enabled) 1f else 0.3f
            }
        }

        songsAddedToPlaylistEvent.observeNonNull(owner) {
        }

        isAddingSongsToPlaylist.observeNonNull(owner) { isAdding ->
            dialog?.apply {
                if (isAdding) {
                    Anim.fadeIn(binding.includeProgressOverlay.root)
                } else {
                    Anim.fadeOut(binding.includeProgressOverlay.root)
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