package com.frolo.muse.ui.main.player.current

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.view.doOnLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.arch.support.observeNonNull
import com.frolo.music.model.Song
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.base.adapter.SimpleItemTouchHelperCallback
import com.frolo.muse.ui.main.addLinearItemMargins
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.DragSongAdapter
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.ui.main.library.playlists.create.PlaylistCreateEvent
import com.frolo.muse.ui.main.provideMainSheetStateViewModel
import com.frolo.muse.ui.smoothScrollToTop
import com.frolo.ui.Screen
import com.frolo.ui.ViewUtils
import kotlinx.android.synthetic.main.fragment_base_list.*
import kotlinx.android.synthetic.main.fragment_curr_song_queue.*


/**
 * This fragment shows the queue of songs currently being played by the player.
 */
class CurrSongQueueFragment: AbsMediaCollectionFragment<Song>() {

    interface OnCloseIconClickListener {
        fun onCloseIconClick(fragment: CurrSongQueueFragment)
    }

    override val viewModel: CurrSongQueueViewModel by viewModel()

    private lateinit var itemTouchHelper: ItemTouchHelper

    private val onDragListener = object : DragSongAdapter.OnDragListener {
        override fun onTouchDragView(holder: RecyclerView.ViewHolder) {
            itemTouchHelper.startDrag(holder)
        }

        override fun onItemMoved(fromPosition: Int, toPosition: Int) {
            val callback = Runnable {
                viewModel.onItemMoved(fromPosition, toPosition)
            }
            postOnUi("dispatch_item_moved", callback)
        }

        override fun onItemDismissed(position: Int) {
            val item = adapter.getItemAt(position)
            viewModel.onItemPositionDismissed(item, position)
        }
    }

    private val adapter: SongAdapter<Song> by lazy {
        DragSongAdapter(provideThumbnailLoader(), onDragListener)
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

    private lateinit var playlistCreateEvent: PlaylistCreateEvent

    private val onCloseIconClickListener: OnCloseIconClickListener?
        get() = parentFragment as? OnCloseIconClickListener

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
    ): View = inflater.inflate(R.layout.fragment_curr_song_queue, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Intercepting all touches to prevent their processing in the lower view layers
        view.setOnTouchListener { _, _ -> true }

        imv_close.setOnClickListener {
            onCloseIconClickListener?.onCloseIconClick(this)
        }

        btn_save_as_playlist.setOnClickListener {
            viewModel.onSaveAsPlaylistOptionSelected()
        }

        rv_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CurrSongQueueFragment.adapter
            addLinearItemMargins()
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

        ll_header.doOnLayout {
            val width = it.measuredWidth
            // Just to make sure that the button does not occupy all the space in the header layout
            if (width > 0) {
                btn_save_as_playlist.maxWidth = (width / 2.4f).toInt()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewLifecycleOwner)
        observeMainSheetsState(viewLifecycleOwner)
    }

    override fun onStart() {
        super.onStart()
        adapter.listener = adapterListener
    }

    override fun onStop() {
        adapter.listener = null
        super.onStop()
    }

    override fun onDetach() {
        playlistCreateEvent.unregister(requireContext())
        super.onDetach()
    }

    override fun onSubmitList(list: List<Song>) {
        adapter.submitAndRetainPlayState(list)
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
            adapter.setPlaying(isPlaying)
        }

        playingPosition.observeNonNull(owner) { playingPosition ->
            val isPlaying = isPlaying.value ?: false
            adapter.setPlayState(playingPosition, isPlaying)
        }

        saveAsPlaylistOptionEnabled.observeNonNull(owner) { enabled ->
            btn_save_as_playlist.apply {
                isEnabled = enabled
                alpha = if (enabled) 1f else 0.5f
            }
        }

        scrollToPositionEvent.observeNonNull(owner) { position ->
            postScrollToPosition(position)
        }

        scrollToPositionIfNotVisibleToUserEvent.observeNonNull(owner) { position ->
            postScrollToPositionIfNotVisibleToUser(position)
        }
    }

    private fun observeMainSheetsState(owner: LifecycleOwner) = with(provideMainSheetStateViewModel()) {
        slideState.observeNonNull(owner) { slideState ->
            val factor = (slideState.queueSheetSlideOffset * 5 - 4f).coerceIn(0f, 1f)
            tv_title.translationX = -Screen.dpFloat(36f) * (1f - factor)
            imv_close.apply {
                scaleX = factor
                scaleY = factor
            }
        }
    }

    override fun scrollToTop() {
        rv_list?.smoothScrollToTop()
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
        val callback = Runnable {
            scrollToPosition(position)
        }
        postOnUi("scroll_to_position", callback)
    }

    private fun postScrollToPositionIfNotVisibleToUser(position: Int) {
        val percentOfAreaVisibleToUser = ViewUtils.getPercentOfAreaVisibleToUser(rv_list)
        if (percentOfAreaVisibleToUser < 0.05) { // the view must be visible to the user by a maximum of 5%
            val callback = Runnable {
                scrollToPosition(position)
            }
            postOnUi("scroll_to_position_if_not_visible_to_user", callback)
        }
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
    private fun smoothlyScrollToPosition(position: Int) {
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

    companion object {

        private const val SCROLL_THRESHOLD_IN_INCH = 0.3f

        private const val TIME_FOR_SCROLLING = 1000L // 1 second

        // Factory
        fun newInstance() = CurrSongQueueFragment()

        private fun getScrollThresholdInPx(context: Context): Int {
            return (SCROLL_THRESHOLD_IN_INCH * context.resources.displayMetrics.densityDpi).toInt()
        }
    }
}