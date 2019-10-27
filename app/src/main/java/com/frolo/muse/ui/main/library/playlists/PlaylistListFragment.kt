package com.frolo.muse.ui.main.library.playlists

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.ui.main.library.FabCallback
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.SimpleMediaCollectionFragment
import com.frolo.muse.ui.toPx
import com.google.android.material.floatingactionbutton.FloatingActionButton


class PlaylistListFragment: SimpleMediaCollectionFragment<Playlist>(),
        FabCallback {

    companion object {
        // Factory
        fun newInstance() = PlaylistListFragment()
    }

    override val viewModel: PlaylistListViewModel by viewModel()

    override val adapter: BaseAdapter<Playlist, *> by lazy {
        PlaylistAdapter().apply {
            setHasStableIds(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_abs_media_collection, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_sort) {
            viewModel.onSortOrderOptionSelected()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onDecorateList(list: RecyclerView) {
        super.onDecorateList(list)
        list.apply {
            clipToPadding = false
            setPadding(0, 0, 0, 72f.toPx(context).toInt())
        }
    }

    override fun isUsingFab() = true

    override fun decorateFab(fab: FloatingActionButton) {
        fab.setImageResource(R.drawable.ic_plus)
    }

    override fun handleClickOnFab() {
        viewModel.onCreatePlaylistButtonClicked()
    }
}