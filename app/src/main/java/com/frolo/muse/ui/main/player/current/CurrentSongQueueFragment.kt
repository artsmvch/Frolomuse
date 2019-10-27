package com.frolo.muse.ui.main.player.current

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.adapter.SimpleItemTouchHelperCallback
import com.frolo.muse.ui.main.library.base.SwappableSongAdapter
import com.frolo.muse.ui.main.decorateAsLinear
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.ui.main.library.playlists.create.PlaylistCreateEvent
import com.frolo.muse.views.showBackArrow
import com.frolo.muse.arch.observe
import kotlinx.android.synthetic.main.fragment_base_list.*
import kotlinx.android.synthetic.main.fragment_current_playlist.*


class CurrentSongQueueFragment: AbsMediaCollectionFragment<Song>() {

    companion object {

        // Factory
        fun newInstance() = CurrentSongQueueFragment()
    }

    override val viewModel: CurrentSongQueueViewModel by viewModel()

    private var isDragging: Boolean = false
    private lateinit var itemTouchHelper: ItemTouchHelper
    private val onDragListener = object : SwappableSongAdapter.OnDragListener {
        override fun onTouchDragView(holder: RecyclerView.ViewHolder) {
            isDragging = true
            itemTouchHelper.startDrag(holder)
        }
        override fun onItemDismissed(position: Int) {
            val item = adapter.getItemAt(position)
            viewModel.onItemPositionDismissed(item, position)
        }
        override fun onItemMoved(fromPosition: Int, toPosition: Int) {
            viewModel.onItemMoved(fromPosition, toPosition)
        }
        override fun onFinishInteracting() {
            isDragging = false
            view?.also {
                // Here, we are sure that the view is created and no NPE will occur
                rv_list.apply {
                    removeCallbacks(onFinishInteractingCallback)
                    post(onFinishInteractingCallback)
                }
            }
        }
    }

    private val adapter: SongAdapter by lazy {
        SwappableSongAdapter(Glide.with(this), onDragListener)
    }

    private val adapterListener = object : BaseAdapter.Listener<Song> {
        override fun onItemClick(item: Song, position: Int) {
            viewModel.onItemPositionClicked(item, position)
        }
        override fun onItemLongClick(item: Song, position: Int) {
            viewModel.onItemLongClicked(item)
        }
        override fun onOptionsMenuClick(item: Song, position: Int) {
            viewModel.onOptionsMenuClicked(item)
        }
    }

    private val onFinishInteractingCallback = Runnable {
        viewModel.onFinishedDragging()
    }

    private lateinit var playlistCreateEvent: PlaylistCreateEvent

    override fun onAttach(context: Context) {
        super.onAttach(context)
        playlistCreateEvent = PlaylistCreateEvent.register(context) { playlist ->
            toastLongMessage(getString(R.string.saved))
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
        return inflater.inflate(R.layout.fragment_current_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(tb_actions as Toolbar)
            supportActionBar?.showBackArrow()
            supportActionBar?.title = getString(R.string.current_playing)
        }

        rv_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CurrentSongQueueFragment.adapter
            decorateAsLinear()
        }

        itemTouchHelper = SimpleItemTouchHelperCallback(
                adapter as SwappableSongAdapter
        ).let { callback ->
            ItemTouchHelper(callback).apply {
                attachToRecyclerView(rv_list)
            }
        }

        isDragging = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
    }

    override fun onStart() {
        super.onStart()
        adapter.listener = adapterListener
        viewModel.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_current_playlist, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_create_playlist) {
            viewModel.onSaveOptionSelected()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        adapter.listener = null
        viewModel.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        rv_list.removeCallbacks(onFinishInteractingCallback)
        super.onDestroyView()
    }

    override fun onDetach() {
        playlistCreateEvent.unregister(requireContext())
        super.onDetach()
    }

    override fun onSubmitList(list: List<Song>) {
        if (!isDragging) {
            // a little dirty bullshit.
            // we don't want to retrieve values from the view model ourselves.
            val playingPosition = viewModel.playingPosition.value ?: -1
            val isPlaying = viewModel.isPlaying.value ?: false
            adapter.submit(list, playingPosition, isPlaying)
        }
    }

    override fun onSubmitSelectedItems(selectedItems: Set<Song>) {
        adapter.submitSelection(selectedItems)
    }

    override fun onSetLoading(loading: Boolean) {
        layout_list_placeholder.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onSetPlaceholderVisible(visible: Boolean) {
        layout_list_placeholder.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onDisplayError(err: Throwable) {
        toastError(err)
    }

    private fun observeViewModel(owner: LifecycleOwner) {
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