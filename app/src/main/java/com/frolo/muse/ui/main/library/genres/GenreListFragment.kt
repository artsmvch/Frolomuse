package com.frolo.muse.ui.main.library.genres

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.frolo.muse.R
import com.frolo.muse.model.media.Genre
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.SimpleMediaCollectionFragment


class GenreListFragment: SimpleMediaCollectionFragment<Genre>() {

    companion object {
        // Factory
        fun newInstance() = GenreListFragment()
    }

    override val viewModel: GenreListViewModel by viewModel()

    override val adapter by lazy { GenreAdapter(provideThumbnailLoader()) }

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
}