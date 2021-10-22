package com.frolo.muse.ui.main.library.playlists

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.ui.main.library.FabCallback
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.SimpleMediaCollectionFragment
import com.frolo.muse.dp2px
import com.google.android.material.floatingactionbutton.FloatingActionButton


class PlaylistListFragment: SimpleMediaCollectionFragment<Playlist>(),
        FabCallback {

    override val viewModel: PlaylistListViewModel by viewModel()

    override val adapter: BaseAdapter<Playlist, *> by lazy { PlaylistAdapter()}

    private val additionalBottomPadding: Int by lazy {
        72f.dp2px(requireContext()).toInt()
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

    override fun onDecorateListView(listView: RecyclerView) {
        super.onDecorateListView(listView)
        listView.apply {
            clipToPadding = false
            //updatePadding(bottom = additionalBottomPadding)
        }
    }

    override fun isUsingFab() = true

    override fun decorateFab(fab: FloatingActionButton) {
        fab.setImageResource(R.drawable.ic_plus)
    }

    override fun handleClickOnFab() {
        viewModel.onCreatePlaylistButtonClicked()
    }

    override fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                safeView.clipToPadding = false
            }
        }

        requireListView().apply {
            clipToPadding = false
            updatePadding(bottom = bottom + additionalBottomPadding)
        }
    }

    companion object {

        // Factory
        fun newInstance() = PlaylistListFragment()

    }

}