package com.frolo.muse.ui.main.library.artists.artist.songs

import android.os.Bundle
import android.view.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.muse.databinding.FragmentBaseListBinding
import com.frolo.muse.di.activityComponent
import com.frolo.music.model.Artist
import com.frolo.music.model.Song
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.base.FragmentContentInsetsListener
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.main.library.base.AbsSongCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.ui.smoothScrollToTop


class SongsOfArtistFragment: AbsSongCollectionFragment<Song>(), FragmentContentInsetsListener {
    private var _binding: FragmentBaseListBinding? = null
    private val binding: FragmentBaseListBinding get() = _binding!!

    override val viewModel: SongsOfArtistViewModel by lazy {
        val artist = requireArguments().getSerializable(ARG_ARTIST) as Artist
        val vmFactory = SongsOfArtistVMFactory(activityComponent, activityComponent, artist)
        ViewModelProviders.of(this, vmFactory)
            .get(SongsOfArtistViewModel::class.java)
    }

    override val adapter: SongAdapter<Song> by lazy { SongOfArtistAdapter(provideThumbnailLoader()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBaseListBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SongsOfArtistFragment.adapter
            addLinearItemMargins()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onSetLoading(loading: Boolean) {
        binding.pbLoading.root.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onSetPlaceholderVisible(visible: Boolean) {
        binding.layoutListPlaceholder.root.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onDisplayError(err: Throwable) {
        toastError(err)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
    }

    override fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                binding.rvList.setPadding(left, top, right, bottom)
                binding.rvList.clipToPadding = false
                safeView.clipToPadding = false
            }
        }
    }

    override fun scrollToTop() {
        binding.rvList.smoothScrollToTop()
    }

    fun onSortOrderActionSelected() {
        viewModel.onSortOrderOptionSelected()
    }

    companion object {
        private const val ARG_ARTIST = "artist"

        fun newInstance(artist: Artist) = SongsOfArtistFragment()
                .withArg(ARG_ARTIST, artist)
    }

}