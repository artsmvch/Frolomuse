package com.frolo.muse.ui.main.library.albums.album

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.frolo.muse.GlideManager
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.AlbumArtUpdateHandler
import com.frolo.muse.ui.main.decorateAsLinear
import com.frolo.muse.ui.main.library.base.AbsSongCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.views.showBackArrow
import kotlinx.android.synthetic.main.fragment_album.*


class AlbumFragment: AbsSongCollectionFragment<Song>() {
    companion object {

        private const val ARG_ALBUM = "album"

        // Factory
        fun newInstance(album: Album) = AlbumFragment()
                .withArg(ARG_ALBUM, album)
    }

    override val viewModel: AlbumViewModel by lazy {
        val album = requireArguments().getSerializable(ARG_ALBUM) as Album
        val vmFactory = AlbumVMFactory(requireApp().appComponent, album)
        ViewModelProviders.of(this, vmFactory)
                .get(AlbumViewModel::class.java)
    }

    override val adapter by lazy {
        SongAdapter<Song>(Glide.with(this)).apply {
            setHasStableIds(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        AlbumArtUpdateHandler.attach(this) { _, _ ->
            viewModel.albumId.value?.also { albumId ->
                loadAlbumArt(albumId)
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_album, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_album, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_edit -> {
                viewModel.onEditAlbumOptionSelected()
                true
            }
            R.id.action_sort -> {
                viewModel.onSortOrderOptionSelected()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initUI() {
        val context = requireContext()

        rv_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AlbumFragment.adapter
            decorateAsLinear()
        }

        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(tb_actions)
            supportActionBar?.showBackArrow()
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            mediaItemCount.observeNonNull(owner) { count ->
                tv_title.text = requireContext().resources.getQuantityString(R.plurals.s_songs, count, count)
            }

            title.observeNonNull(owner) { title ->
                ctl_toolbar.title = title
            }

            albumId.observeNonNull(owner) { albumId ->
                loadAlbumArt(albumId)
            }
        }
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

    private fun loadAlbumArt(albumId: Long) {
        val options = GlideManager.get()
                .requestOptions(albumId)
                .placeholder(R.drawable.vector_note_square)
                .error(R.drawable.vector_note_square)

        Glide.with(this@AlbumFragment)
                .load(GlideManager.albumArtUri(albumId))
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imv_album_art)
    }
}