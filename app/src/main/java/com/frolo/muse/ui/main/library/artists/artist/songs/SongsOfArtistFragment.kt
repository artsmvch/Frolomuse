package com.frolo.muse.ui.main.library.artists.artist.songs

import android.os.Bundle
import android.view.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.model.media.Artist
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.decorateAsLinear
import com.frolo.muse.ui.main.library.base.AbsSongCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter
import kotlinx.android.synthetic.main.fragment_base_list.*


class SongsOfArtistFragment: AbsSongCollectionFragment<Song>(), NoClipping {

    companion object {
        private const val ARG_ARTIST = "artist"

        fun newInstance(artist: Artist) = SongsOfArtistFragment()
                .withArg(ARG_ARTIST, artist)
    }

    override val viewModel: SongsOfArtistViewModel by lazy {
        val artist = requireArguments().getSerializable(ARG_ARTIST) as Artist
        val vmFactory = SongsOfArtistVMFactory(requireApp().appComponent, artist)
        ViewModelProviders.of(this, vmFactory)
                .get(SongsOfArtistViewModel::class.java)
    }

    override val adapter: SongAdapter<Song> by lazy {
        SongOfArtistAdapter(Glide.with(this)).apply {
            setHasStableIds(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_base_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv_list.layoutManager = LinearLayoutManager(context)
        rv_list.adapter = adapter
        rv_list.decorateAsLinear()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_artist, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_sort) {
            viewModel.onSortOrderOptionSelected()
            true
        } else super.onOptionsItemSelected(item)
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

    override fun removeClipping(left: Int, top: Int, right: Int, bottom: Int) {
        view?.also { safeView ->
            if (safeView is ViewGroup) {
                rv_list.setPadding(left, top, right, bottom)
                rv_list.clipToPadding = false
                safeView.clipToPadding = false
            }
        }
    }

}