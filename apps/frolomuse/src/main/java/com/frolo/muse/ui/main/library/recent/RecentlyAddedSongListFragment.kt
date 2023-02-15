package com.frolo.muse.ui.main.library.recent

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.frolo.muse.R
import com.frolo.ui.StyleUtils
import com.frolo.arch.support.observeNonNull
import com.frolo.core.ui.glide.GlideAlbumArtHelper
import com.frolo.core.ui.glide.observe
import com.frolo.core.ui.setIconTint
import com.frolo.music.model.Song
import com.frolo.muse.model.menu.RecentPeriodMenu
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.main.library.base.SimpleMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.ui.main.library.base.showRecentPeriodPopup


class RecentlyAddedSongListFragment: SimpleMediaCollectionFragment<Song>() {

    override val viewModel: RecentlyAddedSongListViewModel by viewModel()

    override val adapter: SongAdapter<Song> by lazy { SongAdapter<Song>(provideThumbnailLoader()) }

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
            menu.setIconTint(StyleUtils.resolveColor(safeContext, R.attr.iconTintMuted))
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
            adapter.setPlaying(isPlaying)
        }

        playingPosition.observeNonNull(owner) { playingPosition ->
            val isPlaying = isPlaying.value ?: false
            adapter.setPlayState(playingPosition, isPlaying)
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