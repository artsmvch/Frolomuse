package com.frolo.muse.ui.main.library.playlists.playlist

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.removeCallbacksSafely
import com.frolo.muse.ui.base.adapter.SimpleItemTouchHelperCallback
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.library.base.DragSongAdapter
import com.frolo.muse.ui.main.decorateAsLinear
import com.frolo.muse.ui.main.library.base.AbsSongCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.toPx
import com.frolo.muse.ui.base.NoClipping
import com.frolo.muse.views.Slider
import com.frolo.muse.views.showBackArrow
import kotlinx.android.synthetic.main.fragment_playlist.*
import kotlinx.android.synthetic.main.include_backdrop_front_list.*


class PlaylistFragment: AbsSongCollectionFragment<Song>(), NoClipping {

    companion object {
        private const val ARG_PLAYLIST = "playlist"

        // Factory
        fun newInstance(playlist: Playlist) = PlaylistFragment()
                .withArg(ARG_PLAYLIST, playlist)
    }

    override val viewModel: PlaylistViewModel by lazy {
        val playlist = requireArguments().getSerializable(ARG_PLAYLIST) as Playlist
        val vmFactory = PlaylistVMFactory(requireApp().appComponent, playlist)
        ViewModelProviders.of(this, vmFactory)
                .get(PlaylistViewModel::class.java)
    }

    private lateinit var itemTouchHelper: ItemTouchHelper

    private val onDragListener = object : DragSongAdapter.OnDragListener {
        override fun onTouchDragView(holder: RecyclerView.ViewHolder) {
            itemTouchHelper.startDrag(holder)
        }

        override fun onItemMoved(fromPosition: Int, toPosition: Int) {
            view?.apply {
                removeCallbacksSafely(onDragEndedCallback)
                val callback = Runnable { dispatchItemMoved(fromPosition, toPosition) }
                post(callback)
                onDragEndedCallback = callback
            }

        }

        override fun onItemDismissed(position: Int) {
            dispatchItemRemoved(adapter.getItemAt(position))
        }
    }

    private var onDragEndedCallback: Runnable? = null

    override val adapter: SongAdapter<Song> by lazy {
        DragSongAdapter(Glide.with(this), onDragListener).apply {
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
    ): View = inflater.inflate(R.layout.fragment_playlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(tb_actions as Toolbar)
            supportActionBar?.apply {
                subtitle = getString(R.string.playlist)
                showBackArrow()
            }
        }

        val callback = SimpleItemTouchHelperCallback(adapter as DragSongAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rv_list)
        itemTouchHelper = touchHelper

        rv_list.apply {
            layoutManager = LinearLayoutManager(context)
            setPadding(0, 0, 0, 64f.toPx(context).toInt())
            clipToPadding = false

            val slider = object : Slider() {
                override fun onSlideUp() {
                    fab_add_song.show()
                }
                override fun onSlideDown() {
                    fab_add_song.hide()
                }
            }
            addOnScrollListener(slider)

            adapter = this@PlaylistFragment.adapter

            decorateAsLinear()
        }

        fab_add_song.setOnClickListener { viewModel.onAddSongButtonClicked() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_playlist, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_edit -> viewModel.onEditPlaylistOptionSelected()
            R.id.action_sort -> viewModel.onSortOrderOptionSelected()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        view?.removeCallbacks(onDragEndedCallback)
        onDragEndedCallback = null
        super.onDestroyView()
    }

    private fun showTitle(title: String) {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            this.title = title
        }
    }

    private fun dispatchItemRemoved(item: Song) {
        checkWritePermissionFor {
            viewModel.onItemRemoved(item)
        }
    }

    private fun dispatchItemMoved(fromPosition: Int, toPosition: Int) {
        checkWritePermissionFor {
            viewModel.onItemMoved(fromPosition, toPosition)
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

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        playlist.observeNonNull(owner) { item ->
            showTitle(item.name)
        }

        mediaItemCount.observeNonNull(owner) { count ->
            tv_title.text = requireContext().resources.getQuantityString(R.plurals.s_songs, count, count)
        }

        isSwappingEnabled.observeNonNull(owner) { isSwappingEnabled ->
            (rv_list.adapter as DragSongAdapter).also { adapter ->
                adapter.itemViewType = if (isSwappingEnabled) {
                    DragSongAdapter.VIEW_TYPE_SWAPPABLE
                } else {
                    DragSongAdapter.VIEW_TYPE_NORMAL
                }
            }
        }
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