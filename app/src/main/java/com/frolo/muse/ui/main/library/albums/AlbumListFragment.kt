package com.frolo.muse.ui.main.library.albums

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.muse.Screen
import com.frolo.muse.glide.GlideAlbumArtHelper
import com.frolo.muse.glide.observe
import com.frolo.muse.model.media.Album
import com.frolo.muse.repository.Preferences
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.ShotLayoutAnimationController
import com.frolo.muse.ui.main.addGridItemMargins
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.main.library.base.SimpleMediaCollectionFragment
import com.frolo.muse.views.recyclerview.FlexibleGridLayoutManager


class AlbumListFragment: SimpleMediaCollectionFragment<Album>() {

    private val preferences: Preferences by prefs()

    override val viewModel: AlbumListViewModel by viewModel()

    override val adapter by lazy { AlbumAdapter(provideThumbnailLoader()) }

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

    override fun onDecorateListView(listView: RecyclerView) {
        val isGridEnabled = preferences.isAlbumGridEnabled

        adapter.itemViewType = if (isGridEnabled) {
            AlbumAdapter.VIEW_TYPE_SMALL_ITEM
        } else {
            AlbumAdapter.VIEW_TYPE_BIG_ITEM
        }

        listView.adapter = adapter

        val context = listView.context
        if (isGridEnabled) {
            val minSpanCount = 2
            val minItemWidth = Screen.dp(context, 120)
            listView.layoutManager = FlexibleGridLayoutManager(
                context = context,
                orientation = RecyclerView.VERTICAL,
                reverseLayout = false,
                minSpanCount = minSpanCount,
                preferredItemSize = minItemWidth
            )
            listView.addGridItemMargins()
        } else {
            listView.layoutManager = LinearLayoutManager(context)
            listView.addLinearItemMargins()
        }
        listView.layoutAnimation = ShotLayoutAnimationController()
    }

    private fun observeViewModel(owner: LifecycleOwner) {
    }
}