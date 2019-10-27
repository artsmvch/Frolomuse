package com.frolo.muse.ui.main.library.artists.artist.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Artist
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.toPx
import com.frolo.muse.views.MarginItemDecoration
import kotlinx.android.synthetic.main.fragment_albums_of_artist.*


class AlbumsOfArtistFragment : AbsMediaCollectionFragment<Album>() {

    companion object {
        private const val ARG_ARTIST = "artist"

        fun newInstance(artist: Artist) = AlbumsOfArtistFragment()
                .withArg(ARG_ARTIST, artist)
    }

    override val viewModel: AlbumsOfArtistViewModel by lazy {
        val artist = requireArguments().getSerializable(ARG_ARTIST) as Artist
        val vmFactory = AlbumsOfArtistVMFactory(requireApp().appComponent, artist)
        ViewModelProviders.of(this, vmFactory)
                .get(AlbumsOfArtistViewModel::class.java)
    }

    private val adapter: BaseAdapter<Album, *> by lazy {
        AlbumOfArtistAdapter(Glide.with(this)).apply {
            setHasStableIds(true)
        }
    }

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

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_albums_of_artist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // set up list
        rv_list.apply {
            adapter = this@AlbumsOfArtistFragment.adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            val m = 8f.toPx(context).toInt()
            addItemDecoration(MarginItemDecoration.createLinear(m, 0))

            val px = 8f.toPx(view.context).toInt()
            val py = 8f.toPx(view.context).toInt()
            setPadding(px, py, px, py)
            clipToPadding = false
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
        pb_loading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onSubmitList(list: List<Album>) {
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

}