package com.frolo.muse.ui.main.library.mostplayed

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.frolo.arch.support.observeNonNull
import com.frolo.core.ui.glide.GlideAlbumArtHelper
import com.frolo.core.ui.glide.observe
import com.frolo.music.model.SongWithPlayCount
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.main.library.base.SimpleMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter


class MostPlayedFragment : SimpleMediaCollectionFragment<SongWithPlayCount>() {

    override val viewModel: MostPlayedViewModel by viewModel()

    override val adapter: SongAdapter<SongWithPlayCount> by lazy {
        SongWithPlayCountAdapter(provideThumbnailLoader())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlideAlbumArtHelper.get().observe(this) {
            adapter.forceResubmit()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        isPlaying.observeNonNull(owner) { isPlaying ->
            adapter.setPlaying(isPlaying)
        }

        playingPosition.observeNonNull(owner) { playingPosition ->
            val isPlaying = isPlaying.value ?: false
            adapter.setPlayState(playingPosition, isPlaying)
        }

    }

}