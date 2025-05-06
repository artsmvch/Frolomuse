package com.frolo.muse.ui.main.library.artists.artist.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.frolo.core.ui.glide.GlideAlbumArtHelper
import com.frolo.core.ui.glide.observe
import com.frolo.muse.databinding.FragmentAlbumsOfArtistBinding
import com.frolo.muse.di.activityComponent
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.smoothScrollToTop
import com.frolo.music.model.Album
import com.frolo.music.model.Artist
import com.frolo.ui.Screen


class AlbumsOfArtistFragment : AbsMediaCollectionFragment<Album>() {
    private var _binding: FragmentAlbumsOfArtistBinding? = null
    private val binding: FragmentAlbumsOfArtistBinding get() = _binding!!

    override val viewModel: AlbumsOfArtistViewModel by lazy {
        val artist = requireArguments().getSerializable(ARG_ARTIST) as Artist
        val vmFactory = AlbumsOfArtistVMFactory(activityComponent, activityComponent, artist)
        ViewModelProviders.of(this, vmFactory)
                .get(AlbumsOfArtistViewModel::class.java)
    }

    private val adapter: BaseAdapter<Album, *> by lazy { AlbumOfArtistAdapter(Glide.with(this)) }

    private val adapterListener = object : BaseAdapter.Listener<Album> {
        override fun onItemClick(item: Album, position: Int) {
            viewModel.onItemClicked(item)
        }

        override fun onItemLongClick(item: Album, position: Int) {
            viewModel.onItemLongClicked(item)
        }

        override fun onOptionsMenuClick(item: Album, position: Int) {
            viewModel.onOptionsMenuClicked(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlideAlbumArtHelper.get().observe(this) {
            adapter.forceResubmit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlbumsOfArtistBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvList.apply {
            adapter = this@AlbumsOfArtistFragment.adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.listener = adapterListener
    }

    override fun onStop() {
        super.onStop()
        adapter.listener = null
    }

    override fun onSetLoading(loading: Boolean) {
        binding.pbLoading.root.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onSubmitList(list: List<Album>) {
        val leftPadding: Int = if (list.size <= 3) {
            Screen.dp(binding.rvList.context, 40f)
        } else {
            Screen.dp(binding.rvList.context, 8f)
        }
        binding.rvList.updatePadding(left = leftPadding)
        adapter.submit(list)
    }

    override fun onSubmitSelectedItems(selectedItems: Set<Album>) {
        adapter.submitSelection(selectedItems)
    }

    override fun onSetPlaceholderVisible(visible: Boolean) {
        // no placeholder
    }

    override fun onDisplayError(err: Throwable) {
        toastError(err)
    }

    override fun scrollToTop() {
        binding.rvList.smoothScrollToTop()
    }

    companion object {
        private const val ARG_ARTIST = "artist"

        fun newInstance(artist: Artist) = AlbumsOfArtistFragment()
                .withArg(ARG_ARTIST, artist)
    }

}