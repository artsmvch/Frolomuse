package com.frolo.muse.ui.main.library.recent

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.arch.observe
import com.frolo.muse.model.media.Song
import com.frolo.muse.model.menu.RecentPeriodMenu
import com.frolo.muse.ui.main.AlbumArtUpdateHandler
import com.frolo.muse.ui.main.library.base.SimpleMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.ui.main.library.base.chooseRecentPeriod


class RecentlyAddedSongListFragment: SimpleMediaCollectionFragment<Song>() {

    companion object {
        //Factory
        fun newIntent() = RecentlyAddedSongListFragment()
    }

    override val viewModel: RecentlyAddedSongListViewModel by viewModel()

    override val adapter by lazy {
        SongAdapter(Glide.with(this)).apply {
            setHasStableIds(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        AlbumArtUpdateHandler.attach(this) { _, _ ->
            adapter.forceResubmit()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
        checkReadPermissionFor {
            //viewModel.onOpened()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_recently_added, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_recent_period) {
            viewModel.onRecentPeriodOptionSelected()
        }
        return true
    }

    private fun onShowRecentPeriodMenu(recentPeriodMenu: RecentPeriodMenu) {
        // first try find the menu item view in activity
        val anchorViewInFragment: View? = view?.findViewById(R.id.action_recent_period)
        // if null then try find the menu item is activity
        val anchorView: View? = anchorViewInFragment?: activity?.findViewById(R.id.action_recent_period)

        anchorView?.let { safeAnchorView ->
            val popup = safeAnchorView.
                    chooseRecentPeriod(recentPeriodMenu) { period ->
                        viewModel.onPeriodSelected(period)
                    }
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            isPlaying.observe(owner) { isPlaying ->
                adapter.setPlayingState(isPlaying)
            }

            playingPosition.observe(owner) { playingPosition ->
                val isPlaying = isPlaying.value ?: false
                adapter.setPlayingPositionAndState(playingPosition, isPlaying)
            }

            openRecentPeriodMenuEvent.observe(owner) { recentPeriodMenu ->
                onShowRecentPeriodMenu(recentPeriodMenu)
            }

        }
    }
}