package com.frolo.muse.ui.main.library.playlists.playlist.addsong

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.SelectableSongQuery
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.BaseFragment
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.decorateAsLinear
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.views.showBackArrow
import kotlinx.android.synthetic.main.fragment_add_song_to_playlist.*


class AddSongToPlaylistFragment: BaseFragment() {

    companion object {
        private const val ARG_PLAYLIST = "playlist"

        // Factory
        fun newInstance(playlist: Playlist) = AddSongToPlaylistFragment()
                .withArg(ARG_PLAYLIST, playlist)
    }

    private val viewModel: AddSongToPlaylistViewModel by lazy {
        val playlist = requireArguments().getSerializable(ARG_PLAYLIST) as Playlist
        val vmFactory = AddSongToPlaylistVMFactory(requireApp().appComponent, playlist)
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

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_song_to_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).apply {
            setSupportActionBar(tb_actions as Toolbar)
            supportActionBar?.showBackArrow()
        }

        rv_list.apply {
            adapter = this@AddSongToPlaylistFragment.adapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            decorateAsLinear()
        }

        btn_add_to_playlist.setOnClickListener {
            checkWritePermissionFor {
                viewModel.onAddButtonClicked()
            }
        }

        sv_query.apply {
            setOnCloseListener {
                viewModel.onCloseSearchViewButtonClicked()
                true
            }
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
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            error.observe(owner) { err ->
                toastError(err)
            }

            selectableSongQuery.observe(owner) { songQuery ->
                onSubmitSongList(songQuery)
            }

            selectedItems.observe(owner) { selectedItems ->
                onSubmitSelection(selectedItems)
            }

            placeholderVisible.observe(owner) { isVisible ->
                onSetPlaceholderVisible(isVisible)
            }

            songsAddedToPlaylistEvent.observe(owner) {
            }

            isAddingSongsToPlaylist.observe(owner) { isAddingSongsToPlaylist ->
                onAddingSongsToPlaylistState(isAddingSongsToPlaylist)
            }
        }
    }

    private fun onSubmitSongList(songQuery: SelectableSongQuery) {
        adapter.submit(songQuery.allItems, songQuery.selection)
    }

    private fun onSubmitSelection(selectedItems: Set<Song>) {
        adapter.submitSelection(selectedItems)
    }

    private fun onSetPlaceholderVisible(isVisible: Boolean) {
        layout_list_placeholder.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun onAddingSongsToPlaylistState(isAdding: Boolean) {
        if (isAdding) {
            showProgressDialog()
        } else {
            hideProgressDialog()
        }
    }
}