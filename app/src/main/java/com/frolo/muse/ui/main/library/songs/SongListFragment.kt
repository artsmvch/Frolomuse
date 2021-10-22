package com.frolo.muse.ui.main.library.songs

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.glide.GlideAlbumArtHelper
import com.frolo.muse.glide.observe
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.main.library.base.SimpleMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter


class SongListFragment: SimpleMediaCollectionFragment<Song>() {

    override val viewModel: SongListViewModel by viewModel()

    override val adapter: SongAdapter<Song> by lazy { SongAdapter<Song>(Glide.with(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        GlideAlbumArtHelper.get().observe(this) {
            adapter.forceResubmit()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observerViewModel(viewLifecycleOwner)
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

    private fun observerViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            isPlaying.observeNonNull(owner) { isPlaying ->
                adapter.setPlayingState(isPlaying)
            }

            playingPosition.observeNonNull(owner) { playingPosition ->
                val isPlaying = isPlaying.value ?: false
                adapter.setPlayingPositionAndState(playingPosition, isPlaying)
            }
        }
    }
}