package com.frolo.muse.ui.main.library.artists

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.LifecycleOwner
import com.frolo.muse.R
import com.frolo.muse.model.media.Artist
import com.frolo.muse.ui.main.library.base.SimpleMediaCollectionFragment


class ArtistListFragment: SimpleMediaCollectionFragment<Artist>() {

    override val viewModel: ArtistListViewModel by viewModel()

    override val adapter by lazy {
        ArtistAdapter().apply {
            setHasStableIds(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
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

    private fun observeViewModel(owner: LifecycleOwner) {
    }
}