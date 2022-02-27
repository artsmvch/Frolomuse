package com.frolo.muse.ui.main.library.artists.artist.songs

import android.os.Bundle
import android.view.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.muse.R
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
import kotlinx.android.synthetic.main.fragment_base_list.*


class SongsOfArtistFragment: AbsSongCollectionFragment<Song>(), FragmentContentInsetsListener {

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
    ): View = inflater.inflate(R.layout.fragment_base_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SongsOfArtistFragment.adapter
            addLinearItemMargins()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onSetLoading(loading: Boolean) {
        pb_loading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onSetPlaceholderVisible(visible: Boolean) {
        layout_list_placeholder.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onDisplayError(err: Throwable) {
        toastError(err)
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
    }

    override fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                rv_list.setPadding(left, top, right, bottom)
                rv_list.clipToPadding = false
                safeView.clipToPadding = false
            }
        }
    }

    override fun scrollToTop() {
        rv_list?.smoothScrollToTop()
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