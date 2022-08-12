package com.frolo.muse.ui.main.library.base

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.frolo.arch.support.observeNonNull
import com.frolo.core.ui.glide.GlideAlbumArtHelper
import com.frolo.core.ui.glide.observe
import com.frolo.music.model.Song


abstract class AbsSongCollectionFragment<T: Song> : AbsMediaCollectionFragment<T>() {

    abstract override val viewModel: AbsSongCollectionViewModel<T>

    abstract val adapter: SongAdapter<T>

    private val adapterListener = object : BaseAdapter.Listener<T> {
        override fun onItemClick(item: T, position: Int) {
            viewModel.onItemClicked(item)
        }

        override fun onItemLongClick(item: T, position: Int) {
            viewModel.onItemLongClicked(item)
        }

        override fun onOptionsMenuClick(item: T, position: Int) {
            viewModel.onOptionsMenuClicked(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlideAlbumArtHelper.get().observe(this) {
            adapter.forceResubmit()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observerViewModel(viewLifecycleOwner)
    }

    override fun onStart() {
        super.onStart()
        adapter.listener = adapterListener
    }

    override fun onStop() {
        super.onStop()
        adapter.listener = null
    }

    override fun onSubmitList(list: List<T>) {
        adapter.submitAndRetainPlayState(list)
    }

    override fun onSubmitSelectedItems(selectedItems: Set<T>) {
        adapter.submitSelection(selectedItems)
    }

    private fun observerViewModel(owner: LifecycleOwner) = with(viewModel) {
        isPlaying.observeNonNull(owner) { isPlaying ->
            adapter.setPlaying(isPlaying)
        }

        playingPosition.observeNonNull(owner) { playingPosition ->
            val isPlaying = isPlaying.value ?: false
            adapter.setPlayState(playingPosition, isPlaying)
        }
    }

}