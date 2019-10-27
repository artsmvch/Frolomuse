package com.frolo.muse.ui.main.library.base

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.frolo.muse.arch.observe
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.main.AlbumArtUpdateHandler


abstract class AbsSongCollectionFragment : AbsMediaCollectionFragment<Song>() {

    abstract override val viewModel: AbsSongCollectionViewModel
    abstract val adapter: SongAdapter
    private val adapterListener = object : BaseAdapter.Listener<Song> {
        override fun onItemClick(item: Song, position: Int) {
            viewModel.onItemClicked(item)
        }
        override fun onItemLongClick(item: Song, position: Int) {
            viewModel.onItemLongClicked(item)
        }
        override fun onOptionsMenuClick(item: Song, position: Int) {
            viewModel.onOptionsMenuClicked(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlbumArtUpdateHandler.attach(this) { _, _ ->
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

    override fun onSubmitList(list: List<Song>) {
        // a little dirty bullshit.
        // we don't want to retrieve values from the view model ourselves.
        val playingPosition = viewModel.playingPosition.value ?: -1
        val isPlaying = viewModel.isPlaying.value ?: false
        adapter.submit(list, playingPosition, isPlaying)
    }

    override fun onSubmitSelectedItems(selectedItems: Set<Song>) {
        adapter.submitSelection(selectedItems)
    }

    private fun observerViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            isPlaying.observe(owner) { isPlaying ->
                adapter.setPlayingState(isPlaying)
            }

            playingPosition.observe(owner) { playingPosition ->
                val isPlaying = isPlaying.value ?: false
                adapter.setPlayingPositionAndState(playingPosition, isPlaying)
            }
        }
    }
}