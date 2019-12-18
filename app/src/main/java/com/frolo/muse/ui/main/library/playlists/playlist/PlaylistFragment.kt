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
import com.frolo.muse.arch.observe
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.adapter.SimpleItemTouchHelperCallback
import com.frolo.muse.ui.base.withArg
import com.frolo.muse.ui.main.library.base.SwappableSongAdapter
import com.frolo.muse.ui.main.decorateAsLinear
import com.frolo.muse.ui.main.library.base.AbsSongCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.ui.main.overrideAnimationDuration
import com.frolo.muse.ui.toPx
import com.frolo.muse.views.Slider
import com.frolo.muse.views.showBackArrow
import kotlinx.android.synthetic.main.fragment_playlist.*
import kotlinx.android.synthetic.main.include_backdrop_front_list.*


class PlaylistFragment: AbsSongCollectionFragment() {

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

    private var isDragging: Boolean = false
    private lateinit var itemTouchHelper: ItemTouchHelper
    private val onDragListener = object : SwappableSongAdapter.OnDragListener {
        override fun onTouchDragView(holder: RecyclerView.ViewHolder) {
            isDragging = true
            itemTouchHelper.startDrag(holder)
        }
        override fun onItemDismissed(position: Int) {
            performRemovingFromPlaylist(adapter.getItemAt(position))
        }
        override fun onItemMoved(fromPosition: Int, toPosition: Int) {
            performSwappingInPlaylist(fromPosition, toPosition)
        }
        override fun onFinishInteracting() {
            isDragging = false
            view?.also { safeView ->
                val oldCallback = finishInteractingCallback
                if (oldCallback != null) {
                    safeView.removeCallbacks(oldCallback)
                }
                val newCallback = Runnable {
                    viewModel.onFinishInteracting()
                }
                safeView.post(newCallback)
                finishInteractingCallback = newCallback
            }
        }
    }

    private var finishInteractingCallback: Runnable? = null

    override val adapter: SongAdapter by lazy {
        SwappableSongAdapter(Glide.with(this), onDragListener).apply {
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
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(tb_actions as Toolbar)
            supportActionBar?.apply {
                subtitle = getString(R.string.playlist)
                showBackArrow()
            }
        }

        val callback = SimpleItemTouchHelperCallback(adapter as SwappableSongAdapter)
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

        isDragging = false

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
        finishInteractingCallback?.also { safeCallback ->
            view?.removeCallbacks(safeCallback)
        }
        finishInteractingCallback = null
        super.onDestroyView()
    }

    private fun showTitle(title: String) {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            this.title = title
        }
    }

    private fun performRemovingFromPlaylist(item: Song) {
        checkWritePermissionFor {
            viewModel.onRemoveItem(item)
        }
    }

    private fun performSwappingInPlaylist(fromPosition: Int, toPosition: Int) {
        checkWritePermissionFor {
            viewModel.onSwapItems(fromPosition, toPosition)
        }
    }

    override fun onSubmitList(list: List<Song>) {
        // Disallow submitting lists while dragging
        if (!isDragging) {
            super.onSubmitList(list)
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

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            playlist.observe(owner) { item ->
                showTitle(item.name)
            }

            mediaItemCount.observe(owner) { count ->
                tv_title.text = requireContext().resources.getQuantityString(R.plurals.s_songs, count, count)
            }

            isSwappingEnabled.observe(owner) { isSwappingEnabled ->
                (rv_list.adapter as SwappableSongAdapter).also { adapter ->
                    adapter.itemViewType = if (isSwappingEnabled) {
                        SwappableSongAdapter.VIEW_TYPE_SWAPPABLE
                    } else {
                        SwappableSongAdapter.VIEW_TYPE_NORMAL
                    }
                }
            }
        }
    }
}