package com.frolo.muse.ui.main.player.current

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.view.doOnLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.model.media.Song
import com.frolo.muse.removeCallbacksSafely
import com.frolo.muse.ui.base.adapter.SimpleItemTouchHelperCallback
import com.frolo.muse.ui.main.decorateAsLinear
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.DragSongAdapter
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.ui.main.library.playlists.create.PlaylistCreateEvent
import kotlinx.android.synthetic.main.fragment_base_list.*
import kotlinx.android.synthetic.main.fragment_current_playlist.*


/**
 * This fragment shows the queue of songs currently being played by the player.
 */
class CurrSongQueueFragment: AbsMediaCollectionFragment<Song>() {

    companion object {

        private const val SCROLL_THRESHOLD_IN_INCH = 0.3f

        private const val TIME_FOR_SCROLLING = 1000L // 1 second

        // Factory
        fun newInstance() = CurrSongQueueFragment()

        private fun getScrollThresholdInPx(context: Context): Int {
            return (SCROLL_THRESHOLD_IN_INCH * context.resources.displayMetrics.densityDpi).toInt()
        }
    }

    override val viewModel: CurrentSongQueueViewModel by viewModel()

    private lateinit var itemTouchHelper: ItemTouchHelper

    private val onDragListener = object : DragSongAdapter.OnDragListener {
        override fun onTouchDragView(holder: RecyclerView.ViewHolder) {
            itemTouchHelper.startDrag(holder)
        }

        override fun onItemMoved(fromPosition: Int, toPosition: Int) {
            view?.apply {
                removeCallbacksSafely(onDragEndedCallback)
                val callback = Runnable { viewModel.onItemMoved(fromPosition, toPosition) }
                post(callback)
                onDragEndedCallback = callback
            }
        }

        override fun onItemDismissed(position: Int) {
            val item = adapter.getItemAt(position)
            viewModel.onItemPositionDismissed(item, position)
        }
    }

    private val adapter: SongAdapter<Song> by lazy {
        DragSongAdapter(Glide.with(this), onDragListener)
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

    private var onDragEndedCallback: Runnable? = null

    private lateinit var playlistCreateEvent: PlaylistCreateEvent

    private var scrollToPositionCallback: Runnable? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        playlistCreateEvent = PlaylistCreateEvent.register(context) { playlist ->
            toastLongMessage(getString(R.string.saved))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_current_playlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Intercepting all touches to prevent their processing in the lower view layers
        view.setOnTouchListener { _, _ -> true }

        tb_actions.apply {
            setTitle(R.string.current_playing)

            inflateMenu(R.menu.fragment_current_playlist)

            setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.action_create_playlist) {
                    viewModel.onSaveOptionSelected()
                    true
                } else false
            }
        }

        rv_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CurrSongQueueFragment.adapter
            decorateAsLinear()
        }

        itemTouchHelper = SimpleItemTouchHelperCallback(
            adapter as DragSongAdapter
        ).let { callback ->
            ItemTouchHelper(callback).apply {
                attachToRecyclerView(rv_list)
            }
        }

        rv_list.doOnLayout {
            checkListChunkShown()
        }

        rv_list.doOnVerticalScroll(threshold = getScrollThresholdInPx(requireContext())) {
            viewModel.onScrolled()
        }
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

    override fun onStop() {
        adapter.listener = null
        viewModel.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        view?.removeCallbacksSafely(onDragEndedCallback)
        onDragEndedCallback = null
        view?.removeCallbacksSafely(scrollToPositionCallback)
        scrollToPositionCallback = null
        super.onDestroyView()
    }

    override fun onDetach() {
        playlistCreateEvent.unregister(requireContext())
        super.onDetach()
    }

    override fun onSubmitList(list: List<Song>) {
        // a little dirty bullshit.
        // we don't want to retrieve values from the view model ourselves.
        val playingPosition = viewModel.playingPosition.value ?: -1
        val isPlaying = viewModel.isPlaying.value ?: false
        adapter.submit(list, playingPosition, isPlaying)
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

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        isPlaying.observeNonNull(owner) { isPlaying ->
            adapter.setPlayingState(isPlaying)
        }

        playingPosition.observeNonNull(owner) { playingPosition ->
            val isPlaying = isPlaying.value ?: false
            adapter.setPlayingPositionAndState(playingPosition, isPlaying)
        }

        scrollToPositionEvent.observeNonNull(owner) { position ->
            postScrollToPosition(position)
        }
    }

    /**
     * Checks if there is some list's chunk shown.
     * Finds first and last visible positions in the list
     * and notifies the view model about them (if they're valid).
     *
     * NOTE: this method should be called only once when the list is laid out.
     */
    private fun checkListChunkShown() {
        val lm = rv_list.layoutManager as? LinearLayoutManager ?: return
        val firstPosition = lm.findFirstVisibleItemPosition()
        val lastPosition = lm.findLastVisibleItemPosition()
        if (firstPosition != RecyclerView.NO_POSITION && lastPosition != RecyclerView.NO_POSITION) {
            viewModel.onListChunkShown(firstPosition, lastPosition)
        }
    }

    private fun postScrollToPosition(position: Int) {
        val safeView = view ?: return
        safeView.removeCallbacks(scrollToPositionCallback)
        val r = Runnable { scrollToPosition(position) }
        scrollToPositionCallback = r
        safeView.post(r)
    }

    /**
     * Immediately scrolls the list to [position].
     */
    private fun scrollToPosition(position: Int) {
        (rv_list.layoutManager as? LinearLayoutManager)?.also { lm ->
            lm.scrollToPositionWithOffset(position, 0)
        }
    }

    /**
     * Smoothly scrolls the list to [position].
     */
    private fun smootlyScrollToPosition(position: Int) {
        val lm = rv_list.layoutManager as? LinearLayoutManager ?: return

        val anyChild = (if (lm.childCount > 0) lm.getChildAt(0) else null)
                ?: // no children in layout manager
                return

        // child height in pixels
        val childHeight = anyChild.measuredHeight

        // distance in pixels to go
        val distanceToGo: Int

        val firstPosition = lm.findFirstVisibleItemPosition()
        if (firstPosition == RecyclerView.NO_POSITION) {
            return
        }

        val lastPosition = lm.findLastVisibleItemPosition()
        if (lastPosition == RecyclerView.NO_POSITION) {
            return
        }

        distanceToGo = when {
            firstPosition > position -> childHeight * (firstPosition - position)

            lastPosition < position -> childHeight * (position - lastPosition)

            else -> return
        }

        lm.startSmoothScroll(
            obtainSmoothScroller(
                context = requireContext(),
                targetPosition = position,
                timeForScrolling = TIME_FOR_SCROLLING,
                distance = distanceToGo
            )
        )
    }
}