package com.frolo.muse.ui.main.library.recent

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.glide.GlideAlbumArtHelper
import com.frolo.muse.glide.observe
import com.frolo.muse.model.media.Song
import com.frolo.muse.model.menu.RecentPeriodMenu
import com.frolo.muse.setIconTint
import com.frolo.muse.ui.main.library.base.SimpleMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.ui.main.library.base.showRecentPeriodPopup


class RecentlyAddedSongListFragment: SimpleMediaCollectionFragment<Song>() {

    override val viewModel: RecentlyAddedSongListViewModel by viewModel()

    override val adapter: SongAdapter<Song> by lazy {
        SongAdapter<Song>(Glide.with(this)).apply {
            setHasStableIds(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        GlideAlbumArtHelper.get().observe(this) {
            adapter.forceResubmit()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_recently_added, menu)
        context?.also { safeContext ->
            menu.setIconTint(StyleUtil.readColorAttrValue(safeContext, R.attr.iconImageTint))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_recent_period) {
            viewModel.onRecentPeriodOptionSelected()
        }
        return true
    }

    private fun onShowRecentPeriodMenu(recentPeriodMenu: RecentPeriodMenu) {
        // First trying to find the corresponding view in the fragment view
        val anchorViewInFragment: View? = view?.findViewById(R.id.action_recent_period)
        // If null, then trying to find the view in the host activity
        val anchorView: View? = anchorViewInFragment?: activity?.findViewById(R.id.action_recent_period)

        anchorView?.let { safeAnchorView ->
            safeAnchorView.showRecentPeriodPopup(recentPeriodMenu) { period ->
                viewModel.onPeriodSelected(period)
            }
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        isPlaying.observeNonNull(owner) { isPlaying ->
            adapter.setPlayingState(isPlaying)
        }

        playingPosition.observeNonNull(owner) { playingPosition ->
            val isPlaying = isPlaying.value ?: false
            adapter.setPlayingPositionAndState(playingPosition, isPlaying)
        }

        openRecentPeriodMenuEvent.observeNonNull(owner) { recentPeriodMenu ->
            onShowRecentPeriodMenu(recentPeriodMenu)
        }
    }

    companion object {
        //Factory
        fun newIntent() = RecentlyAddedSongListFragment()
    }

}