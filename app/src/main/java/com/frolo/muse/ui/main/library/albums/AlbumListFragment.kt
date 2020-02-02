package com.frolo.muse.ui.main.library.albums

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.glide.GlideAlbumArtHelper
import com.frolo.muse.glide.observe
import com.frolo.muse.model.media.Album
import com.frolo.muse.repository.Preferences
import com.frolo.muse.ui.main.decorateAsGrid
import com.frolo.muse.ui.main.decorateAsLinear
import com.frolo.muse.ui.main.library.base.SimpleMediaCollectionFragment
import kotlinx.android.synthetic.main.fragment_base_list.*


class AlbumListFragment: SimpleMediaCollectionFragment<Album>() {

    private val preferences: Preferences by prefs()

    override val viewModel: AlbumListViewModel by viewModel()

    override val adapter by lazy {
        AlbumAdapter(Glide.with(this)).apply {
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
        inflater.inflate(R.menu.fragment_abs_media_collection, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_sort) {
            viewModel.onSortOrderOptionSelected()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onDecorateList(list: RecyclerView) {
        val gridEnabled = preferences.isAlbumGridEnabled

        adapter.itemViewType = if (gridEnabled) {
            AlbumAdapter.VIEW_TYPE_SMALL_ITEM
        } else {
            AlbumAdapter.VIEW_TYPE_BIG_ITEM
        }

        list.adapter = adapter

        if (gridEnabled) {
            list.layoutManager = GridLayoutManager(context, 3)
            rv_list.decorateAsGrid()
        } else {
            list.layoutManager = LinearLayoutManager(context)
            rv_list.decorateAsLinear()
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) {
    }
}